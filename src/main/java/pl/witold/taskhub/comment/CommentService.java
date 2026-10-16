package pl.witold.taskhub.comment;

import org.springframework.stereotype.Service;
import pl.witold.taskhub.comment.dto.CommentResponse;
import pl.witold.taskhub.comment.dto.CreateCommentRequest;
import pl.witold.taskhub.task.Task;
import pl.witold.taskhub.task.TaskRepository;
import java.util.List;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;

    public CommentService(CommentRepository commentRepository, TaskRepository taskRepository) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
    }

    public List<CommentResponse> getCommentsByTaskId(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        return commentRepository.findByTaskIdOrderByCreatedAtDesc(task.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CommentResponse create(Long taskId, CreateCommentRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found: " + taskId));

        Comment comment = new Comment(
                request.content(),
                task
        );

        Comment savedComment = commentRepository.save(comment);

        return toResponse(savedComment);
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
