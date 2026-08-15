package com.apteka.portal.services;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.dtos.request.rating.AptekaTaskRatingCreateRequestDTO;
import com.apteka.portal.dtos.request.rating.AptekaTaskRatingUpdateRequestDTO;
import com.apteka.portal.dtos.response.rating.AptekaRatingsPageResponseDTO;
import com.apteka.portal.dtos.response.rating.AptekaTaskRatingResponseDTO;
import com.apteka.portal.exceptions.AptekaNotFoundException;
import com.apteka.portal.exceptions.AptekaTaskRatingAlreadyExistsException;
import com.apteka.portal.exceptions.AptekaTaskRatingEditLimitExceededException;
import com.apteka.portal.exceptions.AptekaTaskRatingNotFoundException;
import com.apteka.portal.exceptions.CreatorHasNoAptekaException;
import com.apteka.portal.exceptions.TaskNotFoundException;
import com.apteka.portal.exceptions.TaskRatingInvalidStatusException;
import com.apteka.portal.models.Account;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.AptekaTaskRating;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskStatus;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.repository.AccountRepository;
import com.apteka.portal.repository.AptekaRepository;
import com.apteka.portal.repository.AptekaTaskRatingRepository;
import com.apteka.portal.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AptekaRatingService {
    private static final Set<TaskStatus> RATEABLE_STATUSES = Set.of(TaskStatus.CLOSED, TaskStatus.DENIED);

    private final AptekaTaskRatingRepository aptekaTaskRatingRepository;
    private final TaskRepository taskRepository;
    private final AccountRepository accountRepository;
    private final AptekaRepository aptekaRepository;

    @Transactional
    public AptekaTaskRatingResponseDTO create(Long taskId, AptekaTaskRatingCreateRequestDTO dto,
            AppUserDetails currentUser) {
        Task task = taskRepository.findByIdForRating(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        validateCanCreateRating(task, currentUser);

        if (aptekaTaskRatingRepository.existsByTaskId(taskId)) {
            throw new AptekaTaskRatingAlreadyExistsException(taskId);
        }

        Apteka apteka = resolveCreatorApteka(task);
        Account rater = accountRepository.findById(currentUser.getInternalId())
                .orElseThrow(() -> new AccessDeniedException("Пользователь не обнаружен!"));

        AptekaTaskRating rating = AptekaTaskRating.builder()
                .task(task)
                .apteka(apteka)
                .raterAccount(rater)
                .stars(dto.stars())
                .reason(dto.reason().strip())
                .employeeEditCount(0)
                .build();

        return AptekaTaskRatingResponseDTO.from(aptekaTaskRatingRepository.save(rating));
    }

    @Transactional
    public AptekaTaskRatingResponseDTO update(Long taskId, AptekaTaskRatingUpdateRequestDTO dto,
            AppUserDetails currentUser) {
        AptekaTaskRating rating = aptekaTaskRatingRepository.findByTaskId(taskId)
                .orElseThrow(() -> new AptekaTaskRatingNotFoundException(taskId));

        validateCanUpdateRating(rating, currentUser);

        rating.setStars(dto.stars());
        rating.setReason(dto.reason().strip());

        return AptekaTaskRatingResponseDTO.from(aptekaTaskRatingRepository.save(rating));
    }

    @Transactional(readOnly = true)
    public AptekaTaskRatingResponseDTO getByTask(Long taskId, AppUserDetails currentUser) {
        validateCanViewTaskRating(currentUser);

        AptekaTaskRating rating = aptekaTaskRatingRepository.findByTaskId(taskId)
                .orElseThrow(() -> new AptekaTaskRatingNotFoundException(taskId));

        return AptekaTaskRatingResponseDTO.from(rating);
    }

    @Transactional(readOnly = true)
    public AptekaRatingsPageResponseDTO getByApteka(UUID aptekaId, Pageable pageable, AppUserDetails currentUser) {
        validateOfficeAccess(currentUser);

        aptekaRepository.findById(aptekaId)
                .orElseThrow(() -> new AptekaNotFoundException(aptekaId));

        Page<AptekaTaskRatingResponseDTO> page = aptekaTaskRatingRepository.findByAptekaId(aptekaId, pageable)
                .map(AptekaTaskRatingResponseDTO::from);

        List<Object[]> aggregateRows = aptekaTaskRatingRepository.findAggregatesByAptekaId(aptekaId);
        Object[] aggregates = aggregateRows.isEmpty() ? new Object[] { 0.0, 0L } : aggregateRows.get(0);
        Double averageStars = aggregates[0] != null ? ((Number) aggregates[0]).doubleValue() : 0.0;
        Long totalCount = aggregates[1] != null ? ((Number) aggregates[1]).longValue() : 0L;

        return new AptekaRatingsPageResponseDTO(page, averageStars, totalCount);
    }

    private void validateCanCreateRating(Task task, AppUserDetails currentUser) {
        if (!currentUser.hasRole(UserRole.ADMIN) && !isAssigner(task, currentUser)) {
            throw new AccessDeniedException("Только исполнитель задачи или администратор может выставить оценку!");
        }

        if (!RATEABLE_STATUSES.contains(task.getStatus())) {
            throw new TaskRatingInvalidStatusException(task.getId());
        }

        if (task.getCreator() == null || task.getCreator().getApteka() == null) {
            throw new CreatorHasNoAptekaException(task.getId());
        }
    }

    private void validateCanUpdateRating(AptekaTaskRating rating, AppUserDetails currentUser) {
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }

        if (!isAssigner(rating.getTask(), currentUser)) {
            throw new AccessDeniedException("Только исполнитель задачи или администратор может изменить оценку!");
        }

        if (rating.getEmployeeEditCount() >= 1) {
            throw new AptekaTaskRatingEditLimitExceededException();
        }

        rating.setEmployeeEditCount(rating.getEmployeeEditCount() + 1);
    }

    private void validateCanViewTaskRating(AppUserDetails currentUser) {
        if (currentUser.isApteka()) {
            throw new AccessDeniedException("Пользователи аптеки не могут просматривать оценки!");
        }

        if (!currentUser.hasRole(UserRole.APTEKA)) {
            throw new AccessDeniedException("Недостаточно прав для просмотра оценки!");
        }
    }

    private void validateOfficeAccess(AppUserDetails currentUser) {
        if (currentUser.isApteka()) {
            throw new AccessDeniedException("Пользователи аптеки не могут просматривать рейтинг аптеки!");
        }

        if (!currentUser.isClient()) {
            throw new AccessDeniedException("Доступ разрешён только сотрудникам офиса!");
        }
    }

    private Apteka resolveCreatorApteka(Task task) {
        Account creator = task.getCreator();
        if (creator == null || creator.getApteka() == null) {
            throw new CreatorHasNoAptekaException(task.getId());
        }
        return creator.getApteka();
    }

    private boolean isAssigner(Task task, AppUserDetails currentUser) {
        return task.getAssigner() != null
                && Objects.equals(task.getAssigner().getId(), currentUser.getInternalId());
    }
}
