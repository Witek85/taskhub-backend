package pl.witold.taskhub.task.dto;

import pl.witold.taskhub.tag.dto.TagResponse;
import pl.witold.taskhub.task.TaskPriority;
import pl.witold.taskhub.task.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;

public record TaskResponse(
        Long id,
        String name,
        String description,
        TaskPriority priority,
        TaskStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<TagResponse> tags
) {
}