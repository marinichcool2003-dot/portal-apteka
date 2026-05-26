package com.apteka.portal.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.apteka.portal.components.AvatarClientService;
import com.apteka.portal.components.ClientSecurityService;
import com.apteka.portal.components.PasswordValidator;
import com.apteka.portal.dtos.request.ClientRequestDTO;
import com.apteka.portal.dtos.request.ClientUpdateRequestDTO;
import com.apteka.portal.dtos.request.FullClientUpdateRequestDTO;
import com.apteka.portal.dtos.response.ClientResponseDTO;
import com.apteka.portal.dtos.response.ClientWithStatsDTO;
import com.apteka.portal.dtos.response.TaskStatsDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.repository.ClientRepository;
import com.apteka.portal.repository.TaskRepository;
import com.apteka.portal.repository.UserGroupRepository;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {
    @Mock
    private AuthService authService;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private AvatarClientService avatarClientService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserGroupRepository userGroupRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private ClientSecurityService clientSecurityService;
    @Mock
    private PasswordValidator passwordValidator;

    @InjectMocks
    private ClientService clientService;

    @Test
    void getWithNumberOfTask() {
        UUID clientId = UUID.randomUUID();
        Client client = new Client();
        client.setId(clientId);
        UserGroup userGroup = TestData.defaulUserGroup();
        AppUserDetails currentUser = TestData.mockJustSenior();
        TaskStatsDTO stats = new TaskStatsDTO(clientId, 10L, 5L, 2L, 1L, 2L);

        when(userGroupRepository.existsById(userGroup.getId())).thenReturn(true);
        when(clientRepository.findByUserGroupId(userGroup.getId())).thenReturn(List.of(client));
        when(taskRepository.getClientTaskStatsBatch(anyList())).thenReturn(List.of(stats));

        List<ClientWithStatsDTO> result = clientService.getWithNumberOfTask(userGroup.getId(), currentUser);

        assertEquals(1, result.size());
        assertEquals(clientId, result.get(0).client().id());
        assertEquals(10L, result.get(0).stats().totalCount());

        verify(clientSecurityService).validateHasElevatedPrivelegesInGroup(any(), eq(userGroup.getId()));
        verify(userGroupRepository, times(1)).existsById(userGroup.getId());
    }

    @Test
    void create_Success() throws IOException {

        ClientRequestDTO dto = new ClientRequestDTO(
                "  user_login@farmp.ru  ",
                "StrongPass123!",
                "Гетманцев Даниил",
                Set.of("USER", "SENIOR"),
                1);

        UserGroup userGroup = TestData.defaulUserGroup();
        AppUserDetails currentUser = TestData.mockJustAdmin();
        Client savedClient = Client.builder()
                .id(UUID.randomUUID())
                .login("user_login")
                .password("hashed_password")
                .fullName("Гетманцев Даниил")
                .roles(Set.of(UserRole.USER, UserRole.SENIOR))
                .avatarURL("/uploads/avatars/clients/default.png")
                .userGroup(userGroup)
                .build();

        when(userGroupRepository.findById(userGroup.getId())).thenReturn(Optional.of(userGroup));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        when(clientRepository.save(any(Client.class))).thenReturn(savedClient);

        ClientResponseDTO result = clientService.create(dto, currentUser);

        assertEquals(savedClient.getLogin(), result.login());
        assertEquals(savedClient.getFullName(), result.fullName());
        assertEquals(savedClient.getAvatarURL(), result.avatarURL());

        verify(clientSecurityService).validateCanCreateClient(currentUser, userGroup.getId());
        verify(clientSecurityService).canGiveRoleToClient(anySet(), eq(currentUser), eq(userGroup));
        verify(clientRepository).save(any(Client.class));
        verify(userGroupRepository, times(1)).findById(userGroup.getId());
        verify(passwordEncoder, times(1)).encode(eq(dto.password()));
    }

    @Test
    void shouldUpdateYourselfSuccessfully() throws Exception {
        UUID clientId = UUID.randomUUID();
        ClientUpdateRequestDTO dto = new ClientUpdateRequestDTO(
                "newLogin@farmp.ru",
                "newPassword123!",
                new MockMultipartFile("avatar", "img.png", "image/png", new byte[] { 1, 2, 3 }));

        Client existingClient = new Client();
        existingClient.setId(clientId);
        existingClient.setLogin("oldLogin@farmp.ru");

        AppUserDetails mockUser = mock(AppUserDetails.class);
        when(mockUser.getClientId()).thenReturn(clientId);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));
        when(clientRepository.existsByLogin(dto.login())).thenReturn(false);

        ClientResponseDTO result = clientService.updateYourself(clientId, dto, mockUser);

        assertEquals("newLogin@farmp.ru", result.login());
        verify(authService).invalidateAllSession("oldLogin@farmp.ru");
    }

    @Test
    void shouldFullUpdateAsAdmin() throws Exception {
        UUID clientId = UUID.randomUUID();
        UserGroup oldGroup = TestData.defaulUserGroup();
        UserGroup newGroup = TestData.newDefaulUserGroup();
        AppUserDetails currentUser = TestData.mockJustAdmin();
        MockMultipartFile avatar = new MockMultipartFile(
                "avatar", "face.jpg", "image/jpeg", new byte[] { 0, 1, 2 });
        FullClientUpdateRequestDTO dto = new FullClientUpdateRequestDTO(
                "admin_new_login@farmp.ru",
                "new_Pass_123!",
                avatar,
                "Иванов Иван Иванович",
                newGroup.getId());

        Client existingClient = new Client();
        existingClient.setId(clientId);
        existingClient.setLogin("old_login@farmp.ru");
        existingClient.setUserGroup(oldGroup);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));
        TaskStatsDTO stats = new TaskStatsDTO(clientId, 0L, 0L, 344L, 14L, 0L);
        when(taskRepository.getClientTaskStatsBatch(anyList())).thenReturn(List.of(stats));
        when(userGroupRepository.findById(newGroup.getId())).thenReturn(Optional.of(newGroup));

        ClientResponseDTO result = clientService.fullUpdate(clientId, dto, currentUser);

        assertEquals("Иванов Иван Иванович", result.fullName());
        assertEquals("admin_new_login@farmp.ru", result.login());
        assertEquals(newGroup.getId(), result.userGroup().id());

        verify(authService).invalidateAllSession("old_login@farmp.ru");
        verify(userGroupRepository, times(1)).findById(newGroup.getId());

    }

    @Test
    void shouldThrowExceptionWhenTasksAreOpenDuringGroupChange() throws Exception {
        UUID clientId = UUID.randomUUID();
        UserGroup oldGroup = TestData.defaulUserGroup();
        UserGroup newGroup = TestData.newDefaulUserGroup();
        AppUserDetails currentUser = TestData.mockJustAdmin();

        FullClientUpdateRequestDTO dto = new FullClientUpdateRequestDTO(
                "login@farmp.ru", "pass!123ffD", null, "Full Name", newGroup.getId());

        Client existingClient = new Client();
        existingClient.setId(clientId);
        existingClient.setLogin("old_login@farmp.ru");
        existingClient.setPassword("encoded_pass");
        existingClient.setUserGroup(oldGroup);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));

        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        TaskStatsDTO statsWithOpenTasks = new TaskStatsDTO(clientId, 20L, 5L, 10L, 5L, 0L);
        when(taskRepository.getClientTaskStatsBatch(anyList())).thenReturn(List.of(statsWithOpenTasks));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            clientService.fullUpdate(clientId, dto, currentUser);
        });

        assertEquals("У пользователя еще имеются открытые задачи", exception.getMessage());
    }
}
