package com.apteka.portal.services;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.apteka.portal.dtos.request.LoginRequestDTO;
import com.apteka.portal.dtos.request.RefreshRequestDTO;
import com.apteka.portal.dtos.response.AuthResponseDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.RefreshToken;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;

    public AuthResponseDTO login(LoginRequestDTO dto) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(dto.login(), dto.password()));
        AppUserDetails user = (AppUserDetails) authentication.getPrincipal();

        String accessToken = jwtService.generateAccessToken(user);

        RefreshToken refreshToken = refreshTokenService.create(user.getUsername());

        return new AuthResponseDTO(
                accessToken,
                refreshToken.getToken());
    }

    public AuthResponseDTO refresh(RefreshRequestDTO dto) {
        RefreshToken token = refreshTokenService.verify(dto.refreshToken());

        AppUserDetails user = (AppUserDetails) userDetailsService.loadUserByUsername(token.getUsername());

        String newAccessToken = jwtService.generateAccessToken(user);

        refreshTokenService.deleteByUser(token.getUsername());

        RefreshToken newRefreshToken = refreshTokenService.create(token.getUsername());
        return new AuthResponseDTO(
                newAccessToken,
                newRefreshToken.getToken());
    }

    public void logout(String username) {
        refreshTokenService.deleteByUser(username);
    }

    public void invalidateAllSession(String username) {
        refreshTokenService.deleteByUser(username);
    }
}
