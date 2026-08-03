package com.apteka.portal.services;

import com.apteka.portal.components.AvatarService;
import com.apteka.portal.components.cache.SafeCacheService;
import com.apteka.portal.components.servicesecurity.UserGroupSecurityService;
import com.apteka.portal.components.validators.PhoneNumberValidator;
import com.apteka.portal.components.validators.TypeNameValidator;
import com.apteka.portal.controllers.SseController;
import com.apteka.portal.dtos.request.usergroup.UserGroupRequestDTO;
import com.apteka.portal.dtos.request.usergroup.UserGroupUpdateRequestDTO;
import com.apteka.portal.dtos.response.usergroup.UserGroupResponseDTO;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.apteka.portal.dtos.response.usergroup.UserGroupShortResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.apteka.portal.exceptions.DuplicateGroupUserException;
import com.apteka.portal.exceptions.GroupUserNotFoundException;
import com.apteka.portal.exceptions.InvalidGroupUserException;
import com.apteka.portal.models.AccountAction;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.CacheNames;
import com.apteka.portal.models.SseEventNames;
import com.apteka.portal.models.SseSignalTypes;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.repository.UserGroupRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserGroupService {
    private final UserGroupSecurityService userGroupSecurityService;
    private final UserGroupRepository userGroupRepository;
    private final TypeNameValidator typeNameValidator;
    private final PhoneNumberValidator phoneNumberValidator;
    private final SafeCacheService safeCacheService;
    private final AvatarService avatarUserGroupService;
    private final SseController sseController;

    @Value("${app.default.avatars.upload.dir}")
    private String uploadAvatarDir;

    @Value("${app.default.avatars.upload.picture.group}")
    private String uploadAvatarPictureName;

    @Cacheable(value = CacheNames.USER_GROUPS_LIST, key = "'active_user_groups'", condition = "#isActive", sync = true)
    @Transactional(readOnly = true)
    public List<UserGroupResponseDTO> findByActive(AppUserDetails currentUser, Boolean isActive) {
        if (Boolean.FALSE.equals(isActive)) {
            userGroupSecurityService.canSelectNonActive(currentUser);
        }
        return userGroupRepository.findByIsActive(isActive).stream()
                .map(UserGroupResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserGroupResponseDTO getOne(Integer id, AppUserDetails currentUser, Boolean isActive) {
        Integer currentUserGroupId = Optional.ofNullable(currentUser.getUserGroup())
                .map(UserGroup::getId)
                .orElseThrow(() -> new GroupUserNotFoundException("Группа текущего пользователя не привязана!"));

        if (!currentUserGroupId.equals(id)) {
            if (Boolean.FALSE.equals(isActive)) {
                userGroupSecurityService.canSelectNonActive(currentUser);
            }
            boolean isVisible = userGroupRepository.isGroupVisibleToAnother(currentUserGroupId, id, isActive);
            if (!currentUser.hasRole(UserRole.ADMIN) &&
                    !currentUser.hasAnyAction(AccountAction.CAN_SELECT_ALL_ACTIVE_GROUPS,
                            AccountAction.CAN_SELECT_ALL_NON_ACTIVE_GROUPS)
                    &&
                    !isVisible) {
                throw new GroupUserNotFoundException("У вас нет прав на просмотр этой группы или она не существует");
            }
        }

        if (Boolean.TRUE.equals(isActive)) {
            var cachedDto = safeCacheService.get(CacheNames.USER_GROUP, id, UserGroupResponseDTO.class);
            if (cachedDto.isPresent()) {
                return cachedDto.get();
            }
        }

        UserGroup group = userGroupRepository.findByIdAndIsActive(id, isActive)
                .orElseThrow(() -> new GroupUserNotFoundException(id));

        if (!group.isActive()) {
            userGroupSecurityService.canSelectNonActive(currentUser);
        }

        UserGroupResponseDTO response = UserGroupResponseDTO.from(group);

        if (Boolean.TRUE.equals(isActive) && group.isActive()) {
            safeCacheService.put(CacheNames.USER_GROUP, id, response);
        }

        return response;
    }

    @Transactional
    public List<UserGroupShortResponseDTO> getVisibleGroups(Integer id, AppUserDetails currentUser) {
        Integer currentUserGroupId = Optional.ofNullable(currentUser.getUserGroup())
                .map(UserGroup::getId)
                .orElseThrow(() -> new GroupUserNotFoundException("Группа текущего пользователя не привязана!"));

        if (!currentUserGroupId.equals(id)) {

            boolean isVisible = userGroupRepository.isGroupVisibleToAnother(currentUserGroupId, id, true);
            if (!currentUser.hasRole(UserRole.ADMIN) &&
                    !currentUser.hasAnyAction(AccountAction.CAN_SELECT_ALL_ACTIVE_GROUPS,
                            AccountAction.CAN_SELECT_ALL_NON_ACTIVE_GROUPS)
                    &&
                    !isVisible) {
                throw new GroupUserNotFoundException("У вас нет прав на просмотр этой группы или она не существует");
            }
        }

        UserGroup group = userGroupRepository.findByIdAndIsActive(id, true)
                .orElseThrow(() -> new GroupUserNotFoundException(id));

        return userGroupRepository.findVisibleGroupsIncludingSelf(id)
                .stream().map(UserGroupShortResponseDTO::from).toList();
    }

    @Cacheable(value = CacheNames.USER_GROUPS_VISIBLE, key = "#currentUser.userGroup != null ? #currentUser.userGroup.id : 'anonymous'", sync = true)
    public List<UserGroupResponseDTO> getWithVisible(AppUserDetails currentUser) {
        Integer userGroupId = Optional.ofNullable(currentUser.getUserGroup()).map(UserGroup::getId)
                .orElseThrow(() -> new GroupUserNotFoundException(
                        "Группа пользователя не найдена или не привязана к конкретному пользователю!"));
        return userGroupRepository.findVisibleGroupsIncludingSelf(userGroupId).stream()
                .map(UserGroupResponseDTO::from).toList();
    }

    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_GROUPS_LIST, allEntries = true),
            @CacheEvict(value = CacheNames.USER_GROUPS_VISIBLE, allEntries = true)
    })
    @Transactional
    public UserGroupResponseDTO create(UserGroupRequestDTO dto, AppUserDetails currentUser) throws IOException {
        if (!StringUtils.hasText(dto.name()))
            throw new InvalidGroupUserException("Группа пользователя не может быть пустой!");
        String cleanName = typeNameValidator.getCleanName(dto.name());
        validateNameGroup(cleanName, null);
        UserGroup.UserGroupBuilder savedGroupBuilder = UserGroup.builder().name(cleanName);

        if (StringUtils.hasText(dto.phoneNumber())) {
            String cleanPhoneNumber = phoneNumberValidator.getCleanPhoneNumber(dto.phoneNumber());
            savedGroupBuilder.phoneNumber(cleanPhoneNumber);
        }

        if (StringUtils.hasText(dto.internalNumber())) {
            String cleanInternalnumber = phoneNumberValidator.getCleanInternalNumber(dto.internalNumber());
            savedGroupBuilder.internalNumber(cleanInternalnumber);
        }

        if (StringUtils.hasText(dto.extensionNumber())) {
            String cleanExtensionNumber = phoneNumberValidator.getCleanExtensionNumber(dto.extensionNumber());
            savedGroupBuilder.extensionNumber(cleanExtensionNumber);
        }

        savedGroupBuilder.avatarUrl(uploadAvatarDir.concat(uploadAvatarPictureName));

        if (dto.visibleGroups() != null && dto.visibleGroups().size() > 0) {
            Set<UserGroup> visibleGroups = userGroupRepository.findAllByIdIn(dto.visibleGroups())
                    .stream().collect(Collectors.toSet());
            if (visibleGroups.size() != dto.visibleGroups().size()) {
                throw new GroupUserNotFoundException(
                        "Одна или несколько групп из тех которые вы задали не существует!");
            }
            savedGroupBuilder.visibleGroups(visibleGroups);
        }
        UserGroup saved = userGroupRepository.save(
                savedGroupBuilder
                        .isActive(true)
                        .build());

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    safeCacheService.put(CacheNames.USER_GROUP, saved.getId(), UserGroupResponseDTO.from(saved));
                    sseController.broadcastNotification(SseEventNames.REFRESH_USER_GROUPS, SseSignalTypes.CREATED);
                }
            });
        }

        return UserGroupResponseDTO.from(saved);
    }

    @Transactional
    public UserGroupResponseDTO update(Integer id, UserGroupUpdateRequestDTO dto, AppUserDetails currentUser)
            throws IOException {

        UserGroup upGroup = userGroupRepository.findById(id)
                .orElseThrow(() -> new GroupUserNotFoundException(id));

        userGroupSecurityService.validateCanUpdateUserGroup(currentUser, upGroup);

        boolean hasChange = false;

        if (StringUtils.hasText(dto.name())) {
            String cleanName = typeNameValidator.getCleanName(dto.name());
            if (!Objects.equals(cleanName, upGroup.getName())) {
                validateNameGroup(cleanName, id);
                upGroup.setName(cleanName);
                hasChange = true;
            }
        }

        if (StringUtils.hasText(dto.phoneNumber())) {
            String cleanPhoneNumber = phoneNumberValidator.getCleanPhoneNumber(dto.phoneNumber());
            if (!Objects.equals(cleanPhoneNumber, upGroup.getPhoneNumber())) {
                upGroup.setPhoneNumber(cleanPhoneNumber);
                hasChange = true;
            }
        }

        if (StringUtils.hasText(dto.internalNumber())) {
            String cleanInternalnumber = phoneNumberValidator.getCleanInternalNumber(dto.internalNumber());
            if (!Objects.equals(cleanInternalnumber, upGroup.getInternalNumber())) {
                upGroup.setInternalNumber(cleanInternalnumber);
                hasChange = true;
            }
        }

        if (StringUtils.hasText(dto.extensionNumber())) {
            String cleanExtensionNumber = phoneNumberValidator.getCleanExtensionNumber(dto.extensionNumber());
            if (!Objects.equals(cleanExtensionNumber, upGroup.getExtensionNumber())) {
                upGroup.setExtensionNumber(cleanExtensionNumber);
                hasChange = true;
            }
        }

        if (dto.avatar() != null && !dto.avatar().isEmpty()) {
            uploadAvatar(upGroup, dto.avatar());
            hasChange = true;
        }

        if (dto.visibleGroups() != null && dto.visibleGroups().size() > 0) {
            List<UserGroup> requestVisibleGroups = userGroupRepository.findAllByIdIn(dto.visibleGroups());
            if (!upGroup.getVisibleGroups().containsAll(requestVisibleGroups)) {
                upGroup.getVisibleGroups().addAll(requestVisibleGroups);
                hasChange = true;
            }
        }

        UserGroupResponseDTO response = UserGroupResponseDTO.from(upGroup);

        if (hasChange) {
            upGroup.setUpdatedBy(currentUser.getDisplayName());
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status == STATUS_ROLLED_BACK) {
                            avatarUserGroupService.deleteUserGroupAvatarIfExists(id);
                        } else if (status == STATUS_COMMITTED) {
                            if (response.isActive()) {
                                safeCacheService.put(CacheNames.USER_GROUP, id, response);
                            } else {
                                safeCacheService.evict(CacheNames.USER_GROUP, id);
                            }
                            safeCacheService.clear(CacheNames.USER_GROUPS_LIST);
                            safeCacheService.clear(CacheNames.USER_GROUPS_VISIBLE);

                            var signal = new SseEventNames.EntityUpdateSignalDTO(id, SseSignalTypes.UPDATED);
                            sseController.broadcastNotification(SseEventNames.REFRESH_USER_GROUPS, signal);
                        }
                    }
                });
            }
        }

        return response;
    }

    @Transactional
    public void safeDelete(Integer id, AppUserDetails currentUser) {
        UserGroup deletedGroup = userGroupRepository.findById(id)
                .orElseThrow(() -> new GroupUserNotFoundException(id));

        userGroupSecurityService.validateCanSafeDelete(currentUser, deletedGroup);

        deletedGroup.setActive(false);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    safeCacheService.evict(CacheNames.USER_GROUP, id);
                    safeCacheService.clear(CacheNames.USER_GROUPS_LIST);
                    safeCacheService.clear(CacheNames.USER_GROUPS_VISIBLE);
                    safeCacheService.evict(CacheNames.GROUP_USER_STATUS, id);
                    var signal = new SseEventNames.EntityUpdateSignalDTO(id, SseSignalTypes.UPDATED);
                    sseController.broadcastNotification(SseEventNames.REFRESH_USER_GROUPS, signal);
                }
            });
        }
    }

    @Transactional
    public void restoreAfterSafeDelete(Integer id, AppUserDetails currentUser) {
        UserGroup restoreGroup = userGroupRepository.findById(id)
                .orElseThrow(() -> new GroupUserNotFoundException(id));
        userGroupSecurityService.validateCanRestoreAfterSafeDelete(currentUser, restoreGroup);

        restoreGroup.setActive(true);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    UserGroup restored = userGroupRepository.findById(id).orElse(null);
                    if (restored != null && restored.isActive()) {
                        safeCacheService.put(CacheNames.USER_GROUP, id, UserGroupResponseDTO.from(restored));
                    }
                    safeCacheService.clear(CacheNames.USER_GROUPS_LIST);
                    safeCacheService.clear(CacheNames.USER_GROUPS_VISIBLE);
                    safeCacheService.evict(CacheNames.GROUP_USER_STATUS, id);
                    var signal = new SseEventNames.EntityUpdateSignalDTO(id, SseSignalTypes.UPDATED);
                    sseController.broadcastNotification(SseEventNames.REFRESH_USER_GROUPS, signal);
                }
            });
        }
    }

    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_GROUP, key = "#id"),
            @CacheEvict(value = CacheNames.USER_GROUPS_LIST, allEntries = true),
            @CacheEvict(value = CacheNames.USER_GROUPS_VISIBLE, allEntries = true),
            @CacheEvict(value = CacheNames.GROUP_TASKS_BY_GROUP, allEntries = true),
            @CacheEvict(value = CacheNames.GROUP_TASK, allEntries = true),
            @CacheEvict(value = CacheNames.WORK_TYPES_BY_GROUP, allEntries = true)
    })
    @Transactional
    public void permanentDelete(Integer id, AppUserDetails currentUser, Boolean confirm) {
        UserGroup deletedGroup = userGroupRepository.findById(id)
                .orElseThrow(() -> new GroupUserNotFoundException(id));
        userGroupSecurityService.validateCanPermanentDelete(currentUser, deletedGroup, confirm);
        userGroupRepository.delete(deletedGroup);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    avatarUserGroupService.deleteUserGroupAvatarIfExists(id);
                    sseController.broadcastNotification(SseEventNames.REFRESH_USER_GROUPS, SseSignalTypes.DELETED);
                }
            });
        }
    }

    private void uploadAvatar(UserGroup userGroup, MultipartFile avatar) throws IOException {
        String avatarUrl = avatarUserGroupService.uploadUserGroupAvatar(avatar, userGroup.getId());
        userGroup.setAvatarUrl(avatarUrl);
    }

    private void validateNameGroup(String name, Integer currentId) {

        userGroupRepository.findByName(name).ifPresent(existingGroup -> {
            if (!Objects.equals(existingGroup.getId(), currentId)) {
                throw new DuplicateGroupUserException();
            }
        });
    }
}
