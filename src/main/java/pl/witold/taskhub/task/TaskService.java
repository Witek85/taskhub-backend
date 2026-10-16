package pl.witold.taskhub.task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.witold.taskhub.tag.Tag;
import pl.witold.taskhub.tag.TagRepository;
import pl.witold.taskhub.tag.dto.TagResponse;
import pl.witold.taskhub.task.dto.*;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Transactional
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TagRepository tagRepository;

    public TaskService(TaskRepository taskRepository, TagRepository tagRepository) {
        this.taskRepository = taskRepository;
        this.tagRepository = tagRepository;
    }

    public TaskResponse create(CreateTaskRequest request) {
        Task task = new Task(
                request.name(),
                request.description(),
                request.priority()
        );

        task.replaceTags(resolveTags(request.tagCodes()));

        Task savedTask = taskRepository.save(task);
        return toResponse(savedTask);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> getAll(TaskSearchRequest request, Pageable pageable) {
        return taskRepository.findAll(TaskSpecification.withFilters(request), pageable)
                .map(this::toResponse);
    }

    private TaskResponse toResponse(Task task) {
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
                        .toList()
        );
    }

    @Transactional(readOnly = true)
    public TaskResponse getById(Long id) {
        Task task = findTaskById(id);
        return toResponse(task);
    }

    public TaskResponse update(Long id, UpdateTaskRequest request) {
        Task task = findTaskById(id);

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

        return toResponse(savedTask);
    }

    public TaskResponse replace(Long id, ReplaceTaskRequest request) {
        Task task = findTaskById(id);
        task.updateName(request.name());
        task.updateDescription(request.description());
        task.updatePriority(request.priority());
        task.updateStatus(request.status());
        task.replaceTags(resolveTags(request.tagCodes()));

        Task savedTask = taskRepository.save(task);

        return toResponse(savedTask);
    }

    public void delete(Long id) {
        Task task = findTaskById(id);
        taskRepository.delete(task);
    }

    private Task findTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
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

}