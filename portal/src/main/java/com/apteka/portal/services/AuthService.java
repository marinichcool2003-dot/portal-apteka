package com.apteka.portal.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.components.validators.PasswordValidator;
import com.apteka.portal.dtos.mail.EmailContent;
import com.apteka.portal.dtos.request.LoginRequestDTO;
import com.apteka.portal.dtos.request.RefreshRequestDTO;
import com.apteka.portal.dtos.request.auth.AptekaCodeLoginConfirmDTO;
import com.apteka.portal.dtos.request.auth.AptekaCodeLoginRequestDTO;
import com.apteka.portal.dtos.request.auth.EmployeePasswordResetConfirmDTO;
import com.apteka.portal.dtos.request.auth.EmployeePasswordResetRequestDTO;
import com.apteka.portal.dtos.response.AuthResponseDTO;
import com.apteka.portal.dtos.response.auth.OtpRequestResponseDTO;
import com.apteka.portal.exceptions.AlreadyHaveThisPasswordException;
import com.apteka.portal.exceptions.OtpCooldownException;
import com.apteka.portal.models.Account;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.RefreshToken;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.repository.AccountRepository;
import com.apteka.portal.services.OtpService.OtpPurpose;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final AccountRepository accountRepository;
    private final OtpService otpService;
    private final MailService mailService;
    private final EmailTemplateService emailTemplateService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;

    @Value("${app.otp.ttl-seconds:600}")
    private int otpTtlSeconds;

    public AuthResponseDTO login(LoginRequestDTO dto) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(dto.login(), dto.password()));
        AppUserDetails user = (AppUserDetails) authentication.getPrincipal();

        String accessToken = jwtService.generateAccessToken(user);

        RefreshToken refreshToken = refreshTokenService.createWithSession(user.getUsername(), dto.rememberMe());

        return new AuthResponseDTO(
                accessToken,
                refreshToken.getToken(),
                dto.rememberMe());
    }

    public AuthResponseDTO refresh(RefreshRequestDTO dto) {
        RefreshToken token = refreshTokenService.verify(dto.refreshToken());

        AppUserDetails user = (AppUserDetails) userDetailsService.loadUserByUsername(token.getUsername());

        String newAccessToken = jwtService.generateAccessToken(user);

        refreshTokenService.deleteByRefreshTokenFast(token.getToken());

        RefreshToken newRefreshToken = refreshTokenService.createWithSession(token.getUsername(), token.isRememberMe());
        return new AuthResponseDTO(
                newAccessToken,
                newRefreshToken.getToken(),
                newRefreshToken.isRememberMe());
    }

    public void logout(String refreshToken) {
        refreshTokenService.deleteByRefreshTokenFast(refreshToken);
    }

    public void invalidateAllSession(String username) {
        refreshTokenService.deleteByUser(username);
    }

    public OtpRequestResponseDTO requestEmployeePasswordReset(EmployeePasswordResetRequestDTO dto) {
        accountRepository.findByLoginOrEmail(dto.loginOrEmail())
                .filter(this::isEmployeeAccount)
                .filter(Account::isActive)
                .ifPresent(account -> sendEmployeeResetOtp(account));

        return new OtpRequestResponseDTO(OtpRequestResponseDTO.GENERIC_MESSAGE);
    }

    @Transactional
    public void confirmEmployeePasswordReset(EmployeePasswordResetConfirmDTO dto) {
        Account account = accountRepository.findByLoginOrEmail(dto.loginOrEmail())
                .filter(this::isEmployeeAccount)
                .filter(Account::isActive)
                .orElseThrow(com.apteka.portal.exceptions.InvalidOtpException::new);

        otpService.verify(OtpPurpose.EMPLOYEE_RESET, account.getLogin(), dto.code());

        passwordValidator.validatePassword(dto.newPassword(), true);
        if (passwordEncoder.matches(dto.newPassword(), account.getPassword())) {
            throw new AlreadyHaveThisPasswordException();
        }

        account.setPassword(passwordEncoder.encode(dto.newPassword()));
        invalidateAllSession(account.getLogin());
    }

    public OtpRequestResponseDTO requestAptekaCodeLogin(AptekaCodeLoginRequestDTO dto) {
        accountRepository.findByLoginOrEmail(dto.loginOrEmail())
                .filter(this::isAptekaAccount)
                .filter(Account::isActive)
                .ifPresent(account -> sendAptekaLoginOtp(account));

        return new OtpRequestResponseDTO(OtpRequestResponseDTO.GENERIC_MESSAGE);
    }

    public AuthResponseDTO confirmAptekaCodeLogin(AptekaCodeLoginConfirmDTO dto) {
        Account account = accountRepository.findByLoginOrEmail(dto.loginOrEmail())
                .filter(this::isAptekaAccount)
                .filter(Account::isActive)
                .orElseThrow(com.apteka.portal.exceptions.InvalidOtpException::new);

        otpService.verify(OtpPurpose.APTEKA_LOGIN, account.getLogin(), dto.code());

        AppUserDetails user = (AppUserDetails) userDetailsService.loadUserByUsername(account.getLogin());
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.createWithSession(user.getUsername(), false);

        return new AuthResponseDTO(accessToken, refreshToken.getToken(), false);
    }

    private void sendEmployeeResetOtp(Account account) {
        try {
            String code = otpService.generateAndStore(OtpPurpose.EMPLOYEE_RESET, account.getLogin());
            EmailContent content = emailTemplateService.renderOtp(
                    "Код для сброса пароля", code, otpTtlSeconds / 60);
            mailService.sendAsync(account.getEmail(), content, null);
        } catch (OtpCooldownException e) {
            log.debug("OTP cooldown for employee reset: {}", account.getLogin());
        }
    }

    private void sendAptekaLoginOtp(Account account) {
        try {
            String code = otpService.generateAndStore(OtpPurpose.APTEKA_LOGIN, account.getLogin());
            EmailContent content = emailTemplateService.renderOtp(
                    "Код для входа в систему", code, otpTtlSeconds / 60);
            mailService.sendAsync(account.getEmail(), content, null);
        } catch (OtpCooldownException e) {
            log.debug("OTP cooldown for apteka login: {}", account.getLogin());
        }
    }

    private boolean isEmployeeAccount(Account account) {
        return account.getClient() != null && account.getUserRole() != UserRole.APTEKA;
    }

    private boolean isAptekaAccount(Account account) {
        return account.getApteka() != null && account.getUserRole() == UserRole.APTEKA;
    }
}
