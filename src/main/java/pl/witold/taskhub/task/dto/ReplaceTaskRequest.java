package pl.witold.taskhub.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.witold.taskhub.task.TaskPriority;
import pl.witold.taskhub.task.TaskStatus;

import java.util.List;

public record ReplaceTaskRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 255)
        String name,

        @Size(max = 2000)
        String description,

        @NotNull(message = "Priority is required")
        TaskPriority priority,

        @NotNull(message = "Status is required")
        TaskStatus status,

        List<String> tagCodes
) {
}