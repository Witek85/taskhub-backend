package pl.witold.taskhub.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.witold.taskhub.tag.Tag;
import pl.witold.taskhub.tag.TagRepository;
import pl.witold.taskhub.tag.dto.TagResponse;
import pl.witold.taskhub.task.dto.*;
import pl.witold.taskhub.user.CurrentUserService;
import pl.witold.taskhub.user.Role;
import pl.witold.taskhub.user.User;
import pl.witold.taskhub.user.UserRepository;
import pl.witold.taskhub.user.dto.UserSummaryResponse;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Transactional
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TagRepository tagRepository;
    private final CurrentUserService currentUserService;
    private final TaskAuthorizationService authorizationService;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, TagRepository tagRepository, CurrentUserService currentUserService, TaskAuthorizationService authorizationService, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.tagRepository = tagRepository;
        this.currentUserService = currentUserService;
        this.authorizationService = authorizationService;
        this.userRepository = userRepository;
    }

    public TaskResponse create(CreateTaskRequest request) {
        User currentUser = currentUserService.getCurrentUser();

        Task task = new Task(
                request.name(),
                request.description(),
                request.priority()
        );

        task.setOwnership(
                currentUser.getTenant(),
                currentUser
        );

        if (request.assignedToId() != null) {
            if (currentUser.getRole() != Role.ADMIN) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Only admin can assign tasks"
                );
            }

            User assignee = resolveAssignee(
                    request.assignedToId(),
                    currentUser
            );

            task.assignTo(assignee);
        }

        task.replaceTags(resolveTags(request.tagCodes()));

        Task savedTask = taskRepository.save(task);
        return toResponse(savedTask, currentUser);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getAll(
            TaskSearchRequest request,
            Pageable pageable
    ) {
        User user = currentUserService.getCurrentUser();

        var specification = Specification.allOf(
                TaskSpecification.withFilters(request),
                TaskSpecification.visibleTo(user)
        );

        return taskRepository.findAll(specification, pageable)
                .map(task -> toResponse(task, user));
    }

    private TaskResponse toResponse(Task task, User currentUser) {
        return new TaskResponse(
                task.getId(),
                task.getName(),
                task.getDescription(),
                task.getPriority(),
                task.getStatus(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getTags().stream()
                        .map(tag -> new TagResponse(
                                tag.getCode(),
                                tag.getLabel(),
                                tag.getColor()
                        ))
                        .sorted(Comparator.comparing(TagResponse::label))
                        .toList(),

                task.getTenant().getId(),

                new UserSummaryResponse(
                        task.getCreatedBy().getId(),
                        task.getCreatedBy().getUsername()
                ),

                task.getAssignedTo() != null
                        ? new UserSummaryResponse(
                        task.getAssignedTo().getId(),
                        task.getAssignedTo().getUsername()
                )
                        : null,

                authorizationService.canEdit(task, currentUser),
                authorizationService.canDelete(task, currentUser)
        );
    }

    @Transactional(readOnly = true)
    public TaskResponse getById(Long id) {
        Task task = findTaskById(id);
        User user = currentUserService.getCurrentUser();
        return toResponse(task, user);
    }

    public TaskResponse update(Long id, UpdateTaskRequest request) {
        Task task = findTaskById(id);
        User user = currentUserService.getCurrentUser();
        authorizationService.requireEdit(task, user);

        if (request.name() != null) {
            task.updateName(request.name());
        }

        if (request.description() != null) {
            task.updateDescription(request.description());
        }

        if (request.priority() != null) {
            task.updatePriority(request.priority());
        }

        if (request.status() != null) {
            task.updateStatus(request.status());
        }

        if (request.tagCodes() != null) {
            task.replaceTags(resolveTags(request.tagCodes()));
        }

        Task savedTask = taskRepository.save(task);

        return toResponse(savedTask, user);
    }

    public TaskResponse replace(Long id, ReplaceTaskRequest request) {
        Task task = findTaskById(id);
        User user = currentUserService.getCurrentUser();
        authorizationService.requireEdit(task, user);
        task.updateName(request.name());
        task.updateDescription(request.description());
        task.updatePriority(request.priority());
        task.updateStatus(request.status());
        task.replaceTags(resolveTags(request.tagCodes()));

        Task savedTask = taskRepository.save(task);

        return toResponse(savedTask, user);
    }

    public void delete(Long id) {
        Task task = findTaskById(id);

        User user = currentUserService.getCurrentUser();
        authorizationService.requireDelete(task, user);

        taskRepository.delete(task);
    }

    private Task findTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        User user = currentUserService.getCurrentUser();

        authorizationService.requireView(task, user);

        return task;
    }

    private Set<Tag> resolveTags(List<String> tagCodes) {
        if (tagCodes == null || tagCodes.isEmpty()) {
            return Set.of();
        }

        Set<String> uniqueTagCodes = new HashSet<>(tagCodes);

        List<Tag> tags = tagRepository.findByCodeIn(uniqueTagCodes);

        if (tags.size() != uniqueTagCodes.size()) {
            throw new IllegalArgumentException("One or more tags do not exist");
        }

        return new HashSet<>(tags);
    }

    private User resolveAssignee(
            Long userId,
            User currentUser
    ) {
        User assignee = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Invalid assignee"
                        )
                );

        if (!assignee.isEnabled()
                || !assignee.getTenant().getId()
                .equals(currentUser.getTenant().getId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid assignee"
            );
        }

        return assignee;
    }

    @Transactional(readOnly = true)
    public Task getAccessibleTask(Long taskId) {
        return findTaskById(taskId);
    }
}