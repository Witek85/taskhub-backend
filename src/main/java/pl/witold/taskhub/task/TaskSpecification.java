package pl.witold.taskhub.task;

import org.springframework.data.jpa.domain.Specification;
import pl.witold.taskhub.task.dto.TaskSearchRequest;
import pl.witold.taskhub.tag.Tag;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Subquery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TaskSpecification {

    private TaskSpecification() {
    }

    public static Specification<Task> withFilters(TaskSearchRequest request) {
        return Specification.allOf(
                nameContains(request.name()),
                priorityEquals(request.priority()),
                statusEquals(request.status()),
                createdFrom(request.createdFrom() == null ? null : request.createdFrom().atStartOfDay()),
                createdTo(request.createdTo() == null ? null : request.createdTo().plusDays(1).atStartOfDay()),
                tagsMatch(request.tagCodes(), request.allTagsIncluded())
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

    private static Specification<Task> tagsMatch(
            List<String> tagCodes,
            boolean allTagsIncluded
    ) {
        return (root, query, cb) -> {
            if (tagCodes == null || tagCodes.isEmpty()) {
                return cb.conjunction();
            }

            Set<String> codes = tagCodes.stream()
                    .filter(code -> code != null && !code.isBlank())
                    .collect(Collectors.toSet());

            if (codes.isEmpty()) {
                return cb.conjunction();
            }

            assert query != null;
            Subquery<Long> subquery = query.subquery(Long.class);

            var taskRoot = subquery.from(Task.class);
            Join<Task, Tag> tagJoin = taskRoot.join("tags");

            subquery.select(cb.countDistinct(tagJoin.get("code")));

            subquery.where(
                    cb.equal(taskRoot.get("id"), root.get("id")),
                    tagJoin.get("code").in(codes)
            );

            long requiredCount = allTagsIncluded
                    ? codes.size()
                    : 1L;

            return cb.greaterThanOrEqualTo(
                    subquery,
                    requiredCount
            );
        };
    }
}