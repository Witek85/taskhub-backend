package pl.witold.taskhub.task.dto;

import jakarta.validation.constraints.Size;
import pl.witold.taskhub.task.TaskPriority;
import pl.witold.taskhub.task.TaskStatus;

import java.util.List;

public record UpdateTaskRequest(
        @Size(max = 255)
        String name,

        @Size(max = 2000)
        String description,

        TaskPriority priority,

        TaskStatus status,

        List<String> tagCodes
) {
}