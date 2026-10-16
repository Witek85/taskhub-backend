package pl.witold.taskhub.task;

import org.springframework.data.jpa.domain.Specification;
import pl.witold.taskhub.task.dto.TaskSearchRequest;

import java.time.LocalDateTime;

public class TaskSpecification {

    private TaskSpecification() {
    }

    public static Specification<Task> withFilters(TaskSearchRequest request) {
        return Specification.allOf(
                nameContains(request.name()),
                priorityEquals(request.priority()),
                statusEquals(request.status()),
                createdFrom(request.createdFrom() == null ? null : request.createdFrom().atStartOfDay()),
                createdTo(request.createdTo() == null ? null : request.createdTo().plusDays(1).atStartOfDay())
        );
    }

    private static Specification<Task> nameContains(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }

    private static Specification<Task> priorityEquals(TaskPriority priority) {
        return (root, query, criteriaBuilder) -> {
            if (priority == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("priority"), priority);
        };
    }

    private static Specification<Task> statusEquals(TaskStatus status) {
        return (root, query, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    private static Specification<Task> createdFrom(LocalDateTime createdFrom) {
        return (root, query, criteriaBuilder) -> {
            if (createdFrom == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), createdFrom);
        };
    }

    private static Specification<Task> createdTo(LocalDateTime createdToExclusive) {
        return (root, query, criteriaBuilder) -> {
            if (createdToExclusive == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.lessThan(root.get("createdAt"), createdToExclusive);
        };
    }
}