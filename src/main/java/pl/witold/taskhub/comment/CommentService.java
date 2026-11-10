package pl.witold.taskhub.comment;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.witold.taskhub.comment.dto.CommentResponse;
import pl.witold.taskhub.comment.dto.CreateCommentRequest;
import pl.witold.taskhub.task.Task;
import pl.witold.taskhub.task.TaskRepository;
import pl.witold.taskhub.task.TaskService;
import pl.witold.taskhub.user.CurrentUserService;
import pl.witold.taskhub.user.User;

import java.util.List;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final TaskService taskService;
    private final CurrentUserService currentUserService;

    public CommentService(CommentRepository commentRepository, TaskRepository taskRepository, TaskService taskService, CurrentUserService currentUserService) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.taskService = taskService;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByTaskId(Long taskId) {
        Task task = taskService.getAccessibleTask(taskId);

        return commentRepository
                .findByTaskIdOrderByCreatedAtDesc(task.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CommentResponse create(
            Long taskId,
            CreateCommentRequest request
    ) {
        Task task = taskService.getAccessibleTask(taskId);
        User user = currentUserService.getCurrentUser();

        Comment comment = new Comment(
                request.content(),
                task,
                user
        );

        return toResponse(commentRepository.save(comment));
    }

    private CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
