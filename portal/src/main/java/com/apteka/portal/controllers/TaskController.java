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

import com.apteka.portal.dtos.request.TaskRequestAptekaDTO;
import com.apteka.portal.dtos.request.TaskRequestClientDTO;
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

        @GetMapping("/by-group/{groupId}")
        public CompletableFuture<ResponseEntity<List<TaskResponseDTO>>> getByGroup(
                        @PathVariable Integer groupId) {

                return taskService.getByGroup(groupId)
                                .thenApply(tasks -> tasks.stream()
                                                .map(TaskResponseDTO::from)
                                                .toList())
                                .thenApply(ResponseEntity::ok);
        }

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

        @PostMapping("/by-apteka")
        public CompletableFuture<ResponseEntity<TaskResponseDTO>> createByApteka(
                        @RequestBody TaskRequestAptekaDTO dto) {

                return taskService.createByApteka(
                                dto.title(),
                                dto.description(),
                                dto.comments(),
                                dto.aptekaId(),
                                dto.workTaskId())
                                .thenApply(task -> ResponseEntity
                                                .status(HttpStatus.CREATED)
                                                .body(TaskResponseDTO.from(task)));
        }

        @PostMapping("/by-client")
        public CompletableFuture<ResponseEntity<TaskResponseDTO>> createByClient(
                        @RequestBody TaskRequestClientDTO dto) {

                return taskService.createByClient(
                                dto.title(),
                                dto.description(),
                                dto.comments(),
                                dto.createdByClient(),
                                dto.workTaskId())
                                .thenApply(task -> ResponseEntity
                                                .status(HttpStatus.CREATED)
                                                .body(TaskResponseDTO.from(task)));
        }

        // ==================================================
        // STATUS
        // ==================================================

        @PatchMapping("/open/{id}")
        public CompletableFuture<ResponseEntity<String>> openTask(@PathVariable Long id) {
                return taskService.openTask(id)
                                .thenApply(task -> ResponseEntity.ok("Задача {" + task.getId() + "} открыта"));
        }

        @PatchMapping("/processed/{id}")
        public CompletableFuture<ResponseEntity<String>> processedTask(@PathVariable Long id) {
                return taskService.processedTask(id)
                                .thenApply(task -> ResponseEntity.ok("Задача {" + task.getId() + "} в процессе"));
        }

        @PatchMapping("/close/{id}")
        public CompletableFuture<ResponseEntity<String>> closeTask(@PathVariable Long id) {
                return taskService.closeTask(id)
                                .thenApply(task -> ResponseEntity.ok("Задача {" + task.getId() + "} закрыта"));
        }

        @PatchMapping("/denied/{id}")
        public CompletableFuture<ResponseEntity<String>> deniedTask(@PathVariable Long id) {
                return taskService.deniedTask(id)
                                .thenApply(task -> ResponseEntity.ok("Задача {" + task.getId() + "} отклонена"));
        }

        // ==================================================
        // UPDATE / DELETE
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

        @DeleteMapping("/{id}")
        public CompletableFuture<ResponseEntity<Void>> delete(@PathVariable Long id) {
                return taskService.delete(id)
                                .thenApply(v -> ResponseEntity.noContent().build());
        }
}