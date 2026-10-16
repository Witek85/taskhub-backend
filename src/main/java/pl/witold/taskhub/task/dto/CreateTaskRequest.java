package pl.witold.taskhub.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.witold.taskhub.task.TaskPriority;

import java.util.List;

public record CreateTaskRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 255, message = "Name cannot be longer than 255 characters")
        String name,

        @Size(max = 2000, message = "Description cannot be longer than 2000 characters")
        String description,

        @NotNull(message = "Priority is required")
        TaskPriority priority,

        List<String> tagCodes
) {
}