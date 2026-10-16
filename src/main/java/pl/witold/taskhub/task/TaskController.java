package pl.witold.taskhub.task;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pl.witold.taskhub.task.dto.CreateTaskRequest;
import pl.witold.taskhub.task.dto.TaskResponse;
import pl.witold.taskhub.task.dto.UpdateTaskRequest;
import pl.witold.taskhub.task.dto.ReplaceTaskRequest;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import pl.witold.taskhub.task.dto.TaskSearchRequest;

@RestController
@RequestMapping(
        value = "/api/tasks",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public TaskResponse create(@Valid @RequestBody CreateTaskRequest request) {
        return taskService.create(request);
    }

    @GetMapping
    public Page<TaskResponse> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdTo,
            @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        TaskSearchRequest request = new TaskSearchRequest(
                name,
                priority,
                status,
                createdFrom,
                createdTo
        );

        return taskService.getAll(request, pageable);
    }
    @GetMapping("/{id}")
    public TaskResponse getById(@PathVariable Long id) {
        return taskService.getById(id);
    }

    @PatchMapping("/{id}")
    public TaskResponse patchTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        return taskService.update(id, request);
    }

    @PutMapping("/{id}")
    public TaskResponse replace(
            @PathVariable Long id,
            @Valid @RequestBody ReplaceTaskRequest request
    ) {
        return taskService.replace(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        taskService.delete(id);
    }
}