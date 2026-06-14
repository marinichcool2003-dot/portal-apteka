package com.apteka.portal.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.apteka.portal.components.AvatarClientService;
import com.apteka.portal.components.servicesecurity.ClientSecurityService;
import com.apteka.portal.components.validators.FullNameValidator;
import com.apteka.portal.components.validators.LoginValidator;
import com.apteka.portal.components.validators.PasswordValidator;
import com.apteka.portal.controllers.SseController;
import com.apteka.portal.dtos.request.ClientRequestDTO;
import com.apteka.portal.dtos.request.ClientUpdateRequestDTO;
import com.apteka.portal.dtos.request.FullClientUpdateRequestDTO;
import com.apteka.portal.dtos.response.AssignedStatsDTO;
import com.apteka.portal.dtos.response.ClientResponseDTO;
import com.apteka.portal.dtos.response.ClientWithStatsDTO;
import com.apteka.portal.dtos.response.CreatedStatsDTO;
import com.apteka.portal.dtos.response.TaskStatsDTO;
import com.apteka.portal.exceptions.AlreadyHaveThisPasswordException;
import com.apteka.portal.exceptions.ClientNotFoundException;
import com.apteka.portal.exceptions.DublicateClientLoginException;
import com.apteka.portal.exceptions.GroupUserNotFoundException;
import com.apteka.portal.exceptions.SelfDeleteException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.SseEventNames;
import com.apteka.portal.models.SseSignalTypes;
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
	@Mock
	private LoginValidator loginValidator;
	@Mock
	private FullNameValidator fullNameValidator;
	@Mock
	private SseController sseController;

	@InjectMocks
	private ClientService clientService;

	// ==================== getWithNumberOfTask ====================
	@Test
	void getWithNumberOfTask_Success() {
		UUID clientId = UUID.randomUUID();
		Client client = Client.builder()
				.id(clientId)
				.userGroup(TestData.defaulUserGroup())
				.build();

		UserGroup userGroup = TestData.defaulUserGroup();
		AppUserDetails currentUser = TestData.mockJustSenior();
		AssignedStatsDTO stats = new AssignedStatsDTO(clientId, 10L, 5L, 2L, 1L, 2L);

		when(userGroupRepository.existsById(userGroup.getId())).thenReturn(true);
		when(clientRepository.findByUserGroupId(userGroup.getId())).thenReturn(List.of(client));
		when(taskRepository.getClientAssignedStatsBatch(anyList())).thenReturn(List.of(stats));

		List<ClientWithStatsDTO> result = clientService.getWithNumberOfTask(userGroup.getId(), currentUser);

		assertEquals(1, result.size());
		assertEquals(clientId, result.get(0).client().id());
		assertEquals(10L, result.get(0).stats().totalCount());

		verify(clientSecurityService).validateHasElevatedPrivelegesInGroup(eq(currentUser),
				eq(userGroup.getId()));
	}

	@Test
	void getWithNumberOfTask_ReturnsEmptyList_WhenNoClients() {
		UserGroup userGroup = TestData.defaulUserGroup();
		AppUserDetails currentUser = TestData.mockJustSenior();

		when(userGroupRepository.existsById(userGroup.getId())).thenReturn(true);
		when(clientRepository.findByUserGroupId(userGroup.getId())).thenReturn(List.of());

		List<ClientWithStatsDTO> result = clientService.getWithNumberOfTask(userGroup.getId(), currentUser);

		assertTrue(result.isEmpty());
		verify(taskRepository, never()).getClientAssignedStatsBatch(anyList());
	}

	// ==================== create ====================
	@Test
	void create_Success() throws IOException {
		ClientRequestDTO dto = new ClientRequestDTO(
				"  user_login@farmp.ru  ",
				"StrongPass123!",
				"  Гетманцев   Даниил  ",
				Set.of("USER", "SENIOR"),
				1);

		UserGroup userGroup = TestData.defaulUserGroup();
		AppUserDetails currentUser = TestData.mockJustAdmin();

		when(userGroupRepository.findById(userGroup.getId())).thenReturn(Optional.of(userGroup));
		when(loginValidator.getCleanLogin(dto.login())).thenReturn("user_login@farmp.ru");
		when(fullNameValidator.getCleanFullName(dto.fullName())).thenReturn("Гетманцев Даниил");
		when(passwordEncoder.encode(dto.password())).thenReturn("hashed_password");
		when(clientRepository.existsByLogin("  user_login@farmp.ru  ")).thenReturn(false);
		when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

		ClientResponseDTO result = clientService.create(dto, currentUser);

		assertEquals("user_login@farmp.ru", result.login());
		assertEquals("Гетманцев Даниил", result.fullName());

		verify(clientSecurityService).validateCanCreateClient(currentUser, userGroup.getId());
		verify(clientSecurityService).canGiveRoleToClient(anySet(), eq(currentUser), eq(userGroup));
		verify(passwordValidator).validatePassword(dto.password(), true);
	}

	@Test
	void create_ThrowsException_WhenLoginDuplicate() throws IOException {
		ClientRequestDTO dto = new ClientRequestDTO(
				"existing@farmp.ru", "Pass123!", "Name", Set.of("USER"), 1);

		AppUserDetails currentUser = TestData.mockJustAdmin();

		when(clientRepository.existsByLogin("existing@farmp.ru")).thenReturn(true);

		assertThrows(DublicateClientLoginException.class,
				() -> clientService.create(dto, currentUser));

		verify(clientRepository, never()).save(any());
	}

	@Test
	void create_ThrowsException_WhenGroupNotFound() throws IOException {
		ClientRequestDTO dto = new ClientRequestDTO(
				"user@farmp.ru", "Pass123!", "Name", Set.of("USER"), 999);

		AppUserDetails currentUser = TestData.mockJustAdmin();

		when(userGroupRepository.findById(999)).thenReturn(Optional.empty());

		assertThrows(GroupUserNotFoundException.class,
				() -> clientService.create(dto, currentUser));
	}

	// ==================== updateYourself ====================
	@Test
	void updateYourself_Success() throws Exception {
		UUID clientId = UUID.randomUUID();
		ClientUpdateRequestDTO dto = new ClientUpdateRequestDTO(
				"newLogin@farmp.ru",
				"newPassword123!",
				null);

		Client existingClient = Client.builder()
				.id(clientId)
				.login("old@farmp.ru")
				.password("encoded_old")
				.fullName("Old Name")
				.userGroup(TestData.defaulUserGroup())
				.roles(Set.of(UserRole.USER))
				.avatarURL("/avatars/default.png")
				.build();

		// Создаём клиента для currentUser с ТЕМ ЖЕ clientId
		Client currentClient = Client.builder()
				.id(clientId) // Тот же ID что и у existingClient
				.login("user@farmp.ru")
				.fullName("Current User")
				.password("encoded")
				.roles(Set.of(UserRole.USER))
				.userGroup(TestData.defaulUserGroup())
				.avatarURL("/avatars/default.png")
				.build();

		AppUserDetails mockUser = new AppUserDetails(currentClient); // Убираем when(), используем реальный
																		// объект

		when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));
		when(loginValidator.getCleanLogin(dto.login())).thenReturn("newLogin@farmp.ru");
		when(clientRepository.existsByLogin("newLogin@farmp.ru")).thenReturn(false);
		when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
		when(passwordEncoder.encode(dto.password())).thenReturn("encoded_new");

		ClientResponseDTO result = clientService.updateYourself(dto, mockUser);

		assertEquals("newLogin@farmp.ru", result.login());
		verify(authService).invalidateAllSession("old@farmp.ru");
	}

	@Test
	void updateYourself_ThrowsException_WhenLoginDuplicate() throws Exception {
		UUID clientId = UUID.randomUUID();
		ClientUpdateRequestDTO dto = new ClientUpdateRequestDTO("existing@farmp.ru", null, null);

		Client existingClient = Client.builder()
				.id(clientId)
				.login("old@farmp.ru")
				.build();

		Client currentClient = Client.builder()
				.id(clientId)
				.login("user@farmp.ru")
				.fullName("Current User")
				.password("encoded")
				.roles(Set.of(UserRole.USER))
				.userGroup(TestData.defaulUserGroup())
				.avatarURL("/avatars/default.png")
				.build();

		AppUserDetails mockUser = new AppUserDetails(currentClient);

		when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));
		when(loginValidator.getCleanLogin(dto.login())).thenReturn("existing@farmp.ru");
		when(clientRepository.existsByLogin("existing@farmp.ru")).thenReturn(true);

		assertThrows(DublicateClientLoginException.class,
				() -> clientService.updateYourself(dto, mockUser));
	}

	@Test
	void updateYourself_ThrowsException_WhenSamePassword() throws Exception {
		UUID clientId = UUID.randomUUID();
		ClientUpdateRequestDTO dto = new ClientUpdateRequestDTO(null, "samePassword123!", null);

		Client existingClient = Client.builder()
				.id(clientId)
				.login("old@farmp.ru")
				.password("encoded_same")
				.fullName("Old Name")
				.userGroup(TestData.defaulUserGroup())
				.roles(Set.of(UserRole.USER))
				.avatarURL("/avatars/default.png")
				.build();

		Client currentClient = Client.builder()
				.id(clientId)
				.login("user@farmp.ru")
				.fullName("Current User")
				.password("encoded")
				.roles(Set.of(UserRole.USER))
				.userGroup(TestData.defaulUserGroup())
				.avatarURL("/avatars/default.png")
				.build();

		AppUserDetails mockUser = new AppUserDetails(currentClient); // Убираем when()

		when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));
		when(passwordEncoder.matches(dto.password(), existingClient.getPassword())).thenReturn(true);

		assertThrows(AlreadyHaveThisPasswordException.class,
				() -> clientService.updateYourself(dto, mockUser));
	}

	// ==================== fullUpdate ====================
	@Test
	void fullUpdate_Success_WithGroupChange() throws Exception {
		UUID clientId = UUID.randomUUID();
		UserGroup oldGroup = TestData.defaulUserGroup();
		UserGroup newGroup = TestData.newDefaulUserGroup();

		FullClientUpdateRequestDTO dto = new FullClientUpdateRequestDTO(
				"admin_new@farmp.ru", "newPass123!", null, "New Full Name", newGroup.getId());

		AppUserDetails admin = TestData.mockJustAdmin();

		Client existingClient = Client.builder()
				.id(clientId)
				.login("old@farmp.ru")
				.fullName("Old Name")
				.password("encoded_old")
				.userGroup(oldGroup)
				.roles(Set.of(UserRole.USER))
				.avatarURL("/avatars/default.png")
				.build();

		when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));
		when(loginValidator.getCleanLogin(dto.login())).thenReturn("admin_new@farmp.ru");
		when(fullNameValidator.getCleanFullName(dto.fullName())).thenReturn("New Full Name");
		when(clientRepository.existsByLogin("admin_new@farmp.ru")).thenReturn(false);
		when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
		when(passwordEncoder.encode(dto.password())).thenReturn("encoded_new");

		AssignedStatsDTO stats = new AssignedStatsDTO(clientId, 0L, 0L, 0L, 0L, 0L);
		when(taskRepository.getClientAssignedStatsBatch(anyList())).thenReturn(List.of(stats));
		when(userGroupRepository.findById(newGroup.getId())).thenReturn(Optional.of(newGroup));

		ClientResponseDTO result = clientService.fullUpdate(clientId, dto, admin);

		assertEquals("New Full Name", result.fullName());
		assertEquals(newGroup.getId(), result.userGroup().id());
		verify(authService).invalidateAllSession("old@farmp.ru");
	}

	@Test
	void fullUpdate_Success_WithoutGroupChange() throws Exception {
		UUID clientId = UUID.randomUUID();
		UserGroup oldGroup = TestData.defaulUserGroup();

		FullClientUpdateRequestDTO dto = new FullClientUpdateRequestDTO(
				"new@farmp.ru", "newPass123!", null, "New Name", null);

		AppUserDetails admin = TestData.mockJustAdmin();

		Client existingClient = Client.builder()
				.id(clientId)
				.login("old@farmp.ru")
				.fullName("Old Name")
				.password("encoded_old")
				.userGroup(oldGroup)
				.roles(Set.of(UserRole.USER))
				.avatarURL("/avatars/default.png")
				.build();

		when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));
		when(loginValidator.getCleanLogin(dto.login())).thenReturn("new@farmp.ru");
		when(fullNameValidator.getCleanFullName(dto.fullName())).thenReturn("New Name");
		when(clientRepository.existsByLogin("new@farmp.ru")).thenReturn(false);
		when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
		when(passwordEncoder.encode(dto.password())).thenReturn("encoded_new");

		ClientResponseDTO result = clientService.fullUpdate(clientId, dto, admin);

		assertEquals(oldGroup.getId(), result.userGroup().id());
		verify(taskRepository, never()).getClientAssignedStatsBatch(anyList());
	}

	@Test
	void fullUpdate_ThrowsException_WhenTasksAreOpen() throws Exception {
		UUID clientId = UUID.randomUUID();
		UserGroup oldGroup = TestData.defaulUserGroup();
		UserGroup newGroup = TestData.newDefaulUserGroup();

		FullClientUpdateRequestDTO dto = new FullClientUpdateRequestDTO(
				null, null, null, null, newGroup.getId());

		AppUserDetails admin = TestData.mockJustAdmin();

		Client existingClient = Client.builder()
				.id(clientId)
				.userGroup(oldGroup)
				.build();

		when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));

		AssignedStatsDTO statsWithOpenTasks = new AssignedStatsDTO(clientId, 10L, 5L, 0L, 0L, 5L);
		when(taskRepository.getClientAssignedStatsBatch(anyList())).thenReturn(List.of(statsWithOpenTasks));

		AccessDeniedException exception = assertThrows(AccessDeniedException.class,
				() -> clientService.fullUpdate(clientId, dto, admin));

		assertEquals("У пользователя еще имеются открытые задачи", exception.getMessage());
	}

	@Test
	void fullUpdate_ThrowsException_WhenNotAdmin() {
		UUID clientId = UUID.randomUUID();
		FullClientUpdateRequestDTO dto = new FullClientUpdateRequestDTO(null, null, null, null, null);
		AppUserDetails senior = TestData.mockJustSenior();

		assertThrows(AccessDeniedException.class,
				() -> clientService.fullUpdate(clientId, dto, senior));
	}

	// ==================== delete ====================
	@Test
	void delete_Success() {
		UUID clientId = UUID.randomUUID();
		UUID adminId = UUID.randomUUID();

		Client adminClient = Client.builder()
				.id(adminId)
				.login("admin@farmp.ru")
				.fullName("Admin User")
				.roles(Set.of(UserRole.ADMIN))
				.build();
		AppUserDetails admin = new AppUserDetails(adminClient);

		UserGroup testGroup = TestData.defaulUserGroup();
		Client clientForDelete = Client.builder()
				.id(clientId)
				.login("user@farmp.ru")
				.fullName("User for delete")
				.userGroup(testGroup)
				.build();

		when(clientRepository.findById(clientId)).thenReturn(Optional.of(clientForDelete));

		clientService.delete(clientId, admin);

		verify(clientRepository, times(1)).findById(clientId);
		verify(clientRepository, times(1)).delete(clientForDelete);

		var expectedSignal = new SseEventNames.AppUserDetailsSignalDTO(testGroup.getId(), SseSignalTypes.DELETED);
		verify(sseController, times(1)).broadcastNotification(SseEventNames.REFRESH_CLIENTS, expectedSignal);
	}

	@Test
	void delete_ThrowsSelfDeleteException() {
		UUID clientId = UUID.randomUUID();

		Client adminClient = Client.builder()
				.id(clientId)
				.login("admin@farmp.ru")
				.fullName("Admin User")
				.password("encoded")
				.roles(Set.of(UserRole.ADMIN))
				.userGroup(TestData.defaulUserGroup())
				.avatarURL("/avatars/default.png")
				.build();

		AppUserDetails admin = new AppUserDetails(adminClient);

		assertThrows(SelfDeleteException.class,
				() -> clientService.delete(clientId, admin));
	}

	@Test
	void delete_ThrowsException_WhenNotAdmin() {
		UUID clientId = UUID.randomUUID();
		AppUserDetails senior = TestData.mockJustSenior();

		assertThrows(AccessDeniedException.class,
				() -> clientService.delete(clientId, senior));
	}

	// ==================== addRole ====================
	@Test
	void addRole_Success() {
		UUID clientId = UUID.randomUUID();
		UserGroup group = TestData.defaulUserGroup();

		Client clientWithOneRole = Client.builder()
				.id(clientId)
				.roles(new HashSet<>(Set.of(UserRole.USER)))
				.userGroup(group)
				.build();

		when(clientRepository.findById(clientId)).thenReturn(Optional.of(clientWithOneRole));

		AppUserDetails admin = TestData.mockJustAdmin();
		ClientResponseDTO result = clientService.addRole(clientId, "SENIOR", admin);

		assertTrue(result.roles().contains(UserRole.SENIOR));
		verify(clientSecurityService).canGiveRoleToClient(anySet(), eq(admin), eq(group));
	}

	// ==================== removeRole ====================
	@Test
	void removeRole_Success() {
		UUID clientId = UUID.randomUUID();
		UserGroup group = TestData.defaulUserGroup();

		Client clientWithTwoRoles = Client.builder()
				.id(clientId)
				.roles(new HashSet<>(Set.of(UserRole.ADMIN, UserRole.USER)))
				.userGroup(group)
				.build();

		when(clientRepository.findById(clientId)).thenReturn(Optional.of(clientWithTwoRoles));

		AppUserDetails admin = TestData.mockJustAdmin();
		ClientResponseDTO result = clientService.removeRole(clientId, "USER", admin);

		assertEquals(1, result.roles().size());
		assertTrue(result.roles().contains(UserRole.ADMIN));
		verify(clientSecurityService).canRemoveRoles(anySet(), eq(admin), eq(group));
	}

	@Test
	void removeRole_ThrowsException_WhenLastRole() {
		UUID clientId = UUID.randomUUID();

		Client clientWithOneRole = Client.builder()
				.id(clientId)
				.roles(Set.of(UserRole.USER))
				.userGroup(TestData.defaulUserGroup())
				.build();

		when(clientRepository.findById(clientId)).thenReturn(Optional.of(clientWithOneRole));

		AppUserDetails admin = TestData.mockJustAdmin();
		assertThrows(AccessDeniedException.class,
				() -> clientService.removeRole(clientId, "USER", admin));
	}

	// ==================== getAll ====================
	@Test
	void getAll_Success_AsAdmin() {
		UUID clientId = UUID.randomUUID();
		AppUserDetails admin = TestData.mockJustAdmin();

		Client client = Client.builder()
				.id(clientId)
				.login("user@farmp.ru")
				.fullName("User Name")
				.roles(Set.of(UserRole.USER))
				.userGroup(TestData.defaulUserGroup())
				.avatarURL("/avatars/default.png")
				.build();

		when(clientRepository.findAll()).thenReturn(List.of(client));

		List<ClientResponseDTO> result = clientService.getAll(admin);

		assertEquals(1, result.size());
		verify(clientRepository).findAll();
	}

	@Test
	void getAll_ThrowsException_WhenNotAdmin() {
		AppUserDetails senior = TestData.mockJustSenior();
		assertThrows(AccessDeniedException.class,
				() -> clientService.getAll(senior));
	}

	// ==================== getOne ====================
	@Test
	void getOne_Success() {
		UUID clientId = UUID.randomUUID();
		AppUserDetails admin = TestData.mockJustAdmin();

		Client client = Client.builder()
				.id(clientId)
				.login("user@farmp.ru")
				.fullName("User Name")
				.userGroup(TestData.defaulUserGroup())
				.roles(Set.of(UserRole.USER))
				.build();

		when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

		ClientResponseDTO result = clientService.getOne(clientId, admin);

		assertEquals(clientId, result.id());
	}

	@Test
	void getOne_ThrowsException_WhenNotFound() {
		UUID clientId = UUID.randomUUID();
		AppUserDetails admin = TestData.mockJustAdmin();

		when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

		assertThrows(ClientNotFoundException.class,
				() -> clientService.getOne(clientId, admin));
	}

	// ==================== getByGroup ====================
	@Test
	void getByGroup_Success() {
		Integer groupId = 1;
		AppUserDetails admin = TestData.mockJustAdmin();

		Client client = Client.builder()
				.id(UUID.randomUUID())
				.userGroup(TestData.defaulUserGroup())
				.build();

		when(userGroupRepository.existsById(groupId)).thenReturn(true);
		when(clientRepository.findByUserGroupId(groupId)).thenReturn(List.of(client));

		List<ClientResponseDTO> result = clientService.getByGroup(groupId, admin);

		assertEquals(1, result.size());
	}

	@Test
	void getByGroup_ThrowsException_WhenGroupNotFound() {
		Integer invalidGroupId = 999;
		AppUserDetails admin = TestData.mockJustAdmin();

		when(userGroupRepository.existsById(invalidGroupId)).thenReturn(false);

		assertThrows(GroupUserNotFoundException.class,
				() -> clientService.getByGroup(invalidGroupId, admin));
	}

	// ==================== getMyStats ====================
	@Test
	void getMyStats_Success() {
		UUID clientId = UUID.randomUUID();

		// Создаём пользователя с нужным ID
		Client client = Client.builder()
				.id(clientId)
				.login("user@farmp.ru")
				.fullName("User Name")
				.password("encoded")
				.roles(Set.of(UserRole.USER))
				.userGroup(TestData.defaulUserGroup())
				.avatarURL("/avatars/default.png")
				.build();

		AppUserDetails user = new AppUserDetails(client);

		AssignedStatsDTO assignedStats = new AssignedStatsDTO(clientId, 5L, 2L, 1L, 1L, 1L);
		CreatedStatsDTO createdStats = new CreatedStatsDTO(clientId, 3L);

		when(taskRepository.getClientAssignedStatsBatch(List.of(clientId))).thenReturn(List.of(assignedStats));
		when(taskRepository.getClientCreatedStatsBatch(List.of(clientId))).thenReturn(List.of(createdStats));

		TaskStatsDTO result = clientService.getMyStats(user);

		assertEquals(5L, result.assignedStats().totalCount());
		assertEquals(3L, result.createdStats().openCreated());
	}

	@Test
	void getMyStats_ReturnsEmptyStats_WhenNoData() {
		UUID clientId = UUID.randomUUID();

		Client client = Client.builder()
				.id(clientId)
				.login("user@farmp.ru")
				.fullName("User Name")
				.password("encoded")
				.roles(Set.of(UserRole.USER))
				.userGroup(TestData.defaulUserGroup())
				.avatarURL("/avatars/default.png")
				.build();

		AppUserDetails user = new AppUserDetails(client);

		when(taskRepository.getClientAssignedStatsBatch(List.of(clientId))).thenReturn(List.of());
		when(taskRepository.getClientCreatedStatsBatch(List.of(clientId))).thenReturn(List.of());

		TaskStatsDTO result = clientService.getMyStats(user);

		assertEquals(0L, result.assignedStats().totalCount());
		assertEquals(0L, result.createdStats().openCreated());
	}
}