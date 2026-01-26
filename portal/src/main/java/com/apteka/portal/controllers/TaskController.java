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

        @GetMapping
        public CompletableFuture<ResponseEntity<List<TaskResponseDTO>>> getAll() {
                return taskService.getAll()
                                .thenApply(tasks -> tasks.stream()
                                                .map(TaskResponseDTO::from)
                                                .toList())
                                .thenApply(ResponseEntity::ok);
        }

        @GetMapping("/{id}")
        public CompletableFuture<ResponseEntity<TaskResponseDTO>> getOne(@PathVariable Long id) {
                return taskService.getOne(id)
                                .thenApply(task -> ResponseEntity.ok(TaskResponseDTO.from(task)));
        }

        @GetMapping("/byApteka/{id}")
        public CompletableFuture<ResponseEntity<List<TaskResponseDTO>>> getByApteka(@PathVariable Integer aptekaId) {
                return taskService.getByApteka(aptekaId)
                                .thenApply(tasks -> tasks.stream()
                                                .map(TaskResponseDTO::from)
                                                .toList())
                                .thenApply(ResponseEntity::ok);
        }

        @GetMapping("/byClient/{id}")
        public CompletableFuture<ResponseEntity<List<TaskResponseDTO>>> getByClient(@PathVariable UUID clientId) {
                return taskService.getByClient(clientId)
                                .thenApply(tasks -> tasks.stream()
                                                .map(TaskResponseDTO::from)
                                                .toList())
                                .thenApply(ResponseEntity::ok);
        }

        @GetMapping("/byGroup/{id}")
        public CompletableFuture<ResponseEntity<List<TaskResponseDTO>>> getByGroup(@PathVariable Integer groupId) {
                return taskService.getByGroup(groupId)
                                .thenApply(tasks -> tasks.stream()
                                                .map(TaskResponseDTO::from)
                                                .toList())
                                .thenApply(ResponseEntity::ok);
        }

        @GetMapping("/byCreatedClient/{id}")
        public CompletableFuture<ResponseEntity<List<TaskResponseDTO>>> getByCreatedByClient(
                        @PathVariable UUID createdByClientId) {
                return taskService.getByCreatedByClient(createdByClientId)
                                .thenApply(tasks -> tasks.stream()
                                                .map(TaskResponseDTO::from)
                                                .toList())
                                .thenApply(ResponseEntity::ok);
        }

        @PostMapping("/byApteka")
        public CompletableFuture<ResponseEntity<TaskResponseDTO>> create(
                        @RequestBody TaskRequestAptekaDTO taskRequestDTO) {
                return taskService.create(
                                taskRequestDTO.title(),
                                taskRequestDTO.description(),
                                taskRequestDTO.comments(),
                                taskRequestDTO.aptekaId(),
                                taskRequestDTO.groupId())
                                .thenApply(task -> ResponseEntity
                                                .status(HttpStatus.CREATED)
                                                .body(TaskResponseDTO.from(task)));
        }

        @PostMapping("/byClient")
        public CompletableFuture<ResponseEntity<TaskResponseDTO>> create(
                        @RequestBody TaskRequestClientDTO taskRequestDTO) {
                return taskService.create(
                                taskRequestDTO.title(),
                                taskRequestDTO.description(),
                                taskRequestDTO.comments(),
                                taskRequestDTO.createdByClient(),
                                taskRequestDTO.groupId())
                                .thenApply(task -> ResponseEntity
                                                .status(HttpStatus.CREATED)
                                                .body(TaskResponseDTO.from(task)));
        }

        @PatchMapping("/open-task/{id}")
        public CompletableFuture<ResponseEntity<String>> openTask(@PathVariable Long id) {
                return taskService.openTask(id)
                                .thenApply(task -> ResponseEntity
                                                .ok()
                                                .body("Задача {" + task.getId() + "} успешно открыта!"));
        }

        @PatchMapping("/close-task/{id}")
        public CompletableFuture<ResponseEntity<String>> closeTask(@PathVariable Long id) {
                return taskService.closeTask(id)
                                .thenApply(task -> ResponseEntity
                                                .ok()
                                                .body("Задача {" + task.getId() + "} успешно закрыта!"));
        }

        @PatchMapping("/processed-task/{id}")
        public CompletableFuture<ResponseEntity<String>> processedTask(@PathVariable Long id) {
                return taskService.processedTask(id)
                                .thenApply(task -> ResponseEntity
                                                .ok()
                                                .body("Задача {" + task.getId() + "} в процессе выполнения!"));
        }

        @PatchMapping("/denied-task/{id}")
        public CompletableFuture<ResponseEntity<String>> deniedTask(@PathVariable Long id) {
                return taskService.deniedTask(id)
                                .thenApply(task -> ResponseEntity
                                                .ok()
                                                .body("Задача {" + task.getId() + "} отклонена!"));
        }

        @PutMapping("/{id}")
        public CompletableFuture<ResponseEntity<TaskResponseDTO>> update(@PathVariable Long id,
                        @RequestBody TaskUpdateRequestDTO taskUpdateRequestDTO) {
                return taskService.update(
                                id, taskUpdateRequestDTO.title(),
                                taskUpdateRequestDTO.description(),
                                taskUpdateRequestDTO.comments())
                                .thenApply(task -> ResponseEntity
                                                .ok()
                                                .body(TaskResponseDTO.from(task)));
        }

        @DeleteMapping("/{id}")
        public CompletableFuture<ResponseEntity<Void>> delete(@PathVariable Long id) {
                return taskService.delete(id)
                                .thenApply(v -> ResponseEntity
                                                .noContent()
                                                .build());
        }
}