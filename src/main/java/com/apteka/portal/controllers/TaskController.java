package com.apteka.portal.controllers;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.apteka.portal.dtos.request.TaskRequestCreatedByAptekaToClientDTO;
import com.apteka.portal.dtos.request.TaskRequestCreatedByAptekaToGroupClient;
import com.apteka.portal.dtos.request.TaskRequestCreatedByClientToClientDTO;
import com.apteka.portal.dtos.request.TaskRequestCreatedByClientToClientInGroupDTO;
import com.apteka.portal.dtos.request.TaskUpdateRequestDTO;
import com.apteka.portal.dtos.response.TaskResponseDTO;
import com.apteka.portal.services.TaskService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/task")
@RequiredArgsConstructor
public class TaskController {
        private final TaskService taskService;

        // ==================================================
        // GET
        // ==================================================

        //===================================================
        // Получение всех задач
        //===================================================
        @GetMapping
        public CompletableFuture<ResponseEntity<List<TaskResponseDTO>>> getAll() {
                return taskService.getAll()
                                .thenApply(tasks -> tasks.stream()
                                                .map(TaskResponseDTO::from)
                                                .toList())
                                .thenApply(ResponseEntity::ok);
        }

        //===================================================
        // Получение одной задачи
        //===================================================
        @GetMapping("/{id}")
        public CompletableFuture<ResponseEntity<TaskResponseDTO>> getOne(@PathVariable Long id) {
                return taskService.getOne(id)
                                .thenApply(task -> ResponseEntity.ok(TaskResponseDTO.from(task)));
        }

        //===================================================
        // Получение задач назначенных аптеке
        //===================================================
        @GetMapping("/by-assigned-apteka/{assignedAptekaId}")
        public CompletableFuture<ResponseEntity<List<TaskResponseDTO>>> getByAssignedApteka(
                        @PathVariable Integer assignedAptekaId) {

                return taskService.getByAssignedApteka(assignedAptekaId)
                                .thenApply(tasks -> tasks.stream()
                                                .map(TaskResponseDTO::from)
                                                .toList())
                                .thenApply(ResponseEntity::ok);
        }

        //===================================================
        // Получение задач назначенных сотруднику
        //===================================================
        @GetMapping("/by-assigned-client/{assignedClientId}")
        public CompletableFuture<ResponseEntity<List<TaskResponseDTO>>> getByAssignedClient(
                        @PathVariable UUID assignedClientId) {

                return taskService.getByAssignedClient(assignedClientId)
                                .thenApply(tasks -> tasks.stream()
                                                .map(TaskResponseDTO::from)
                                                .toList())
                                .thenApply(ResponseEntity::ok);
        }

        //===================================================
        // Получение задач созданных аптекой
        //===================================================
        @GetMapping("/by-created-apteka/{createdByAptekaId}")
        public CompletableFuture<ResponseEntity<List<TaskResponseDTO>>> getByCreatedByApteka(
                        @PathVariable Integer createdByAptekaId) {

                return taskService.getByCreatedByApteka(createdByAptekaId)
                                .thenApply(tasks -> tasks.stream()
                                                .map(TaskResponseDTO::from)
                                                .toList())
                                .thenApply(ResponseEntity::ok);
        }

        //===================================================
        // Получение задач созданных сотрудником
        //===================================================
        @GetMapping("/by-created-client/{createdByClientId}")
        public CompletableFuture<ResponseEntity<List<TaskResponseDTO>>> getByCreatedByClient(
                        @PathVariable UUID createdByClientId) {

                return taskService.getByCreatedByClient(createdByClientId)
                                .thenApply(tasks -> tasks.stream()
                                                .map(TaskResponseDTO::from)
                                                .toList())
                                .thenApply(ResponseEntity::ok);
        }

        //===================================================
        // Получение задач по группе
        //===================================================
        @GetMapping("/by-group/{groupId}")
        public CompletableFuture<ResponseEntity<List<TaskResponseDTO>>> getByGroup(
                        @PathVariable Integer groupId) {

                return taskService.getByGroup(groupId)
                                .thenApply(tasks -> tasks.stream()
                                                .map(TaskResponseDTO::from)
                                                .toList())
                                .thenApply(ResponseEntity::ok);
        }

        //===================================================
        // Получение задач по группе сотрудников
        //===================================================
        @GetMapping("/by-client-group/{groupId}")
        public CompletableFuture<ResponseEntity<List<TaskResponseDTO>>> getByGroupClient(
                        @PathVariable Integer groupId,
                        @RequestParam String status) {

                return taskService.getByGroupClient(groupId, status)
                                .thenApply(tasks -> tasks.stream()
                                                .map(TaskResponseDTO::from)
                                                .toList())
                                .thenApply(ResponseEntity::ok);
        }

        // ==================================================
        // CREATE
        // ==================================================

        // ==================================================
        // Создание задачи аптекой для сотрудника
        // ==================================================
        @PostMapping("/by-apteka-to-client")
        public CompletableFuture<ResponseEntity<TaskResponseDTO>> createByAptekaToClient(
                        @RequestBody TaskRequestCreatedByAptekaToClientDTO dto) {

                return taskService.createByAptekaToClient(
                                dto.title(),
                                dto.description(),
                                dto.comments(),
                                dto.createByAptekaId(),
                                dto.workTypeId(),
                                dto.assignedClientId())
                                .thenApply(task -> ResponseEntity
                                                .status(HttpStatus.CREATED)
                                                .body(TaskResponseDTO.from(task)));
        }

        // ==================================================
        // Создание задачи аптекой для группы сотрудников
        // ==================================================
        @PostMapping("/by-apteka-to-group-client")
        public CompletableFuture<ResponseEntity<TaskResponseDTO>> createByAptekaToGroupClient(
                        @RequestBody TaskRequestCreatedByAptekaToGroupClient dto) {
                return taskService.createByAptekaToGroupClient(
                                dto.title(),
                                dto.description(),
                                dto.comments(),
                                dto.createdAptekaId(),
                                dto.assignedGroupClientId(),
                                dto.workTypeId())
                                .thenApply(task -> ResponseEntity
                                                .status(HttpStatus.CREATED)
                                                .body(TaskResponseDTO.from(task)));
        }

        // ==================================================
        // Создание задачи сотрудником для сотрудника любой группы
        // ==================================================
        @PostMapping("/by-client-to-client")
        public CompletableFuture<ResponseEntity<TaskResponseDTO>> createByClient(
                        @RequestBody TaskRequestCreatedByClientToClientDTO dto) {

                return taskService.createByClientToClient(
                                dto.title(),
                                dto.description(),
                                dto.comments(),
                                dto.creatorClient(),
                                dto.assignedClient(),
                                dto.workTypeId())
                                .thenApply(task -> ResponseEntity
                                                .status(HttpStatus.CREATED)
                                                .body(TaskResponseDTO.from(task)));
        }

        // ==================================================
        // Создание задачи сотрудником для сотрудника только внутри группы
        // ==================================================
        @PostMapping("/by-client-to-client-in-group")
        public CompletableFuture<ResponseEntity<TaskResponseDTO>> createByClientToClientInGroup(
                        @RequestBody TaskRequestCreatedByClientToClientInGroupDTO dto) {

                return taskService.createByClientToClientInGroup(
                                dto.title(),
                                dto.description(),
                                dto.comments(),
                                dto.creatorClient(),
                                dto.assignedClient(),
                                dto.workTypeId())
                                .thenApply(task -> ResponseEntity
                                                .status(HttpStatus.CREATED)
                                                .body(TaskResponseDTO.from(task)));
        }

        // ==================================================
        // STATUS
        // ==================================================

        @PatchMapping("/change-status")
        public CompletableFuture<ResponseEntity<TaskResponseDTO>> changeStatus(@PathVariable Long id, String statusDescription) {
                return taskService.changeStatus(
                                id, 
                                statusDescription).
                                thenApply(task -> ResponseEntity
                                        .status(HttpStatus.CREATED)
                                        .body(TaskResponseDTO.from(task)));
        }

        // ==================================================
        // UPDATE / DELETE
        // ==================================================

        // ==================================================
        // Обновление содержания задачи (Заголовок, Описание)
        // ==================================================
        @PutMapping("/{id}")
        public CompletableFuture<ResponseEntity<TaskResponseDTO>> update(
                        @PathVariable Long id,
                        @RequestBody TaskUpdateRequestDTO dto) {

                return taskService.update(
                                id,
                                dto.title(),
                                dto.description(),
                                dto.comments())
                                .thenApply(task -> ResponseEntity.ok(TaskResponseDTO.from(task)));
        }

        // ==================================================
        // Распределение задачи на сотрудника для сотрудников из одной группы
        // ==================================================
        @PatchMapping("/change-assigned-client-in-group")
        public CompletableFuture<ResponseEntity<TaskResponseDTO>> changeAssignedClientInGroup(@PathVariable Long id, @RequestBody UUID assignedClientId) {
                return taskService.changeAssignedClientInGroup(
                                id, 
                                assignedClientId)
                                .thenApply(task -> ResponseEntity.ok(TaskResponseDTO.from(task)));
        }

        // ==================================================
        // Распределение задачи на любого сотрудника
        // ==================================================
        @PatchMapping("/change-assigned-client")
        public CompletableFuture<ResponseEntity<TaskResponseDTO>> changeAssignedClient(@PathVariable Long id, @RequestBody UUID assignedClientId) {
                return taskService.changeAssignedClient(
                                id, 
                                assignedClientId)
                                .thenApply(task -> ResponseEntity.ok(TaskResponseDTO.from(task)));
        }

        // ==================================================
        // Распределение задачи на группу
        // ==================================================
        @PatchMapping("/change-assigned-group-client")
        public CompletableFuture<ResponseEntity<TaskResponseDTO>> changeAssignedGroupClient(@PathVariable Long id, @RequestBody Integer assignedGroupClientId) {
                return taskService.changeAssignedGroupClient(
                                id,
                                assignedGroupClientId)
                                .thenApply(task -> ResponseEntity.ok(TaskResponseDTO.from(task)));
        }

        @DeleteMapping("/{id}")
        public CompletableFuture<ResponseEntity<Void>> delete(@PathVariable Long id) {
                return taskService.delete(id)
                                .thenApply(v -> ResponseEntity.noContent().build());
        }
}