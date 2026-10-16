package pl.witold.taskhub.task;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.witold.taskhub.tag.TagRepository;
import pl.witold.taskhub.task.dto.CreateTaskRequest;
import pl.witold.taskhub.task.dto.TaskResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    public void getById_shouldReturnTaskWhenExists() {
        // given
        Task task = new Task(
                "Learn JUnit",
                "Write first unit test",
                TaskPriority.HIGH
        );

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        // when
        TaskResponse result = taskService.getById(1L);

        // then
        assertThat(result.name()).isEqualTo("Learn JUnit");
        assertThat(result.description()).isEqualTo("Write first unit test");
        assertThat(result.priority()).isEqualTo(TaskPriority.HIGH);
        assertThat(result.status()).isEqualTo(TaskStatus.NEW);
    }

    @Test
    void getById_shouldThrowWhenTaskDoesNotExist() {
        when(taskRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getById(99L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task not found: 99");
    }

    @Test
    void create_shouldSaveTask() {
        // given
        CreateTaskRequest request = new CreateTaskRequest(
                "Learn Mockito",
                "Understand repository mocks",
                TaskPriority.HIGH,
                List.of()
        );

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        TaskResponse result = taskService.create(request);

        // then
        assertThat(result.name()).isEqualTo("Learn Mockito");
        assertThat(result.description()).isEqualTo("Understand repository mocks");
        assertThat(result.priority()).isEqualTo(TaskPriority.HIGH);
        assertThat(result.status()).isEqualTo(TaskStatus.NEW);

        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void delete_shouldDeleteExistingTask() {
        // given
        Task task = new Task(
                "Task to delete",
                "Description",
                TaskPriority.LOW
        );

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        // when
        taskService.delete(1L);

        // then
        verify(taskRepository).delete(task);
    }
}