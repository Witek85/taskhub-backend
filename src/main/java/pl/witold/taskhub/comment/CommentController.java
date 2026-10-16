package pl.witold.taskhub.comment;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pl.witold.taskhub.comment.dto.CommentResponse;
import pl.witold.taskhub.comment.dto.CreateCommentRequest;
import java.util.List;


@RestController
@RequestMapping(
        value = "/api/tasks/{taskId}/comments",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public List<CommentResponse> getCommentsByTaskId(@PathVariable Long taskId) {
        return commentService.getCommentsByTaskId(taskId);
    }

    @PostMapping
    public CommentResponse create(
            @PathVariable Long taskId,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        return commentService.create(taskId, request);
    }
}
