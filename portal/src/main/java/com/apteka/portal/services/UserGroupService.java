package com.apteka.portal.services;

import com.apteka.portal.components.AvatarService;
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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.CacheNames;
import com.apteka.portal.models.SseEventNames;
import com.apteka.portal.models.SseSignalTypes;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.repository.UserGroupRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserGroupService {
    private final UserGroupSecurityService userGroupSecurityService;
    private final UserGroupRepository userGroupRepository;
    private final TypeNameValidator typeNameValidator;
    private final PhoneNumberValidator phoneNumberValidator;
    private final CacheManager cacheManager;
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
            if (!isVisible) {
                throw new GroupUserNotFoundException("У вас нет прав на просмотр этой группы или она не существует");
            }
        }

        Cache cache = cacheManager.getCache(CacheNames.USER_GROUP);
        if (Boolean.TRUE.equals(isActive) && cache != null) {
            UserGroupResponseDTO cachedDto = cache.get(id, UserGroupResponseDTO.class);
            if (cachedDto != null) {
                return cachedDto;
            }
        }

        UserGroup group = userGroupRepository.findById(id)
                .orElseThrow(() -> new GroupUserNotFoundException(id));

        if (!group.isActive()) {
            userGroupSecurityService.canSelectNonActive(currentUser);
        }

        UserGroupResponseDTO response = UserGroupResponseDTO.from(group);

        if (Boolean.TRUE.equals(isActive) && group.isActive() && cache != null) {
            cache.put(id, response);
        }

        return response;
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

        Set<UserGroup> visibleGroups = userGroupRepository.findAllByIdIn(dto.visibleGroups())
                .stream().collect(Collectors.toSet());

        if (visibleGroups.size() != dto.visibleGroups().size()) {
            throw new GroupUserNotFoundException("Одна или несколько групп из тех которые вы задали не существует!");
        }

        UserGroup saved = savedGroupBuilder.isActive(true).visibleGroups(visibleGroups).build();
        userGroupRepository.save(saved);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
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

        if (dto.visibleGroups().size() > 0) {
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
                            var cache = cacheManager.getCache(CacheNames.USER_GROUP);
                            if (cache != null) {
                                if (response.isActive()) {
                                    cache.put(id, response);
                                } else {
                                    cache.evict(id);
                                }
                            }

                            var userGroupsVisibleCache = cacheManager.getCache(CacheNames.USER_GROUPS_VISIBLE);
                            if (userGroupsVisibleCache != null) {
                                userGroupsVisibleCache.clear();
                            }

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
                    var userGroupCache = cacheManager.getCache(CacheNames.USER_GROUP);
                    if (userGroupCache != null) {
                        userGroupCache.evict(id);
                    }

                    var userGroupsListCache = cacheManager.getCache(CacheNames.USER_GROUPS_LIST);
                    if (userGroupsListCache != null) {
                        userGroupsListCache.clear();
                    }

                    var userGroupsVisibleCache = cacheManager.getCache(CacheNames.USER_GROUPS_VISIBLE);
                    if (userGroupsVisibleCache != null) {
                        userGroupsVisibleCache.clear();
                    }

                    var cache = cacheManager.getCache(CacheNames.GROUP_USER_STATUS);
                    if (cache != null) {
                        cache.evict(id);
                    }
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
                    var userGroupCache = cacheManager.getCache(CacheNames.USER_GROUP);
                    if (userGroupCache != null) {
                        UserGroup restored = userGroupRepository.findById(id).orElse(null);
                        if (restored != null && restored.isActive()) {
                            userGroupCache.put(id, UserGroupResponseDTO.from(restored));
                        }
                    }
                    var userGroupsListCache = cacheManager.getCache(CacheNames.USER_GROUPS_LIST);
                    if (userGroupsListCache != null) {
                        userGroupsListCache.clear();
                    }

                    var userGroupsVisibleCache = cacheManager.getCache(CacheNames.USER_GROUPS_VISIBLE);
                    if (userGroupsVisibleCache != null) {
                        userGroupsVisibleCache.clear();
                    }

                    var cache = cacheManager.getCache(CacheNames.GROUP_USER_STATUS);
                    if (cache != null) {
                        cache.evict(id);
                    }
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
            @CacheEvict(value = CacheNames.GROUP_TASKS_BY_GROUP, key = "#id"),
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
