package pl.witold.taskhub.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
        @NotBlank(message = "Content is required")
        @Size(max = 2000, message = "Content cannot be longer than 2000 characters")
        String content
) {

}
