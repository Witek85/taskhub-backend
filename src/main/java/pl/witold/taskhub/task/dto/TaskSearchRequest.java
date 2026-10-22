package pl.witold.taskhub.task.dto;

import pl.witold.taskhub.task.TaskPriority;
import pl.witold.taskhub.task.TaskStatus;

import java.time.LocalDate;
import java.util.List;

public record TaskSearchRequest(
        String name,
        TaskPriority priority,
        TaskStatus status,
        LocalDate createdFrom,
        LocalDate createdTo,
        List<String> tagCodes,
        boolean allTagsIncluded
) {
}