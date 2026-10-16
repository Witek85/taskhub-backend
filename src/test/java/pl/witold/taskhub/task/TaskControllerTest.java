package pl.witold.taskhub.task;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.witold.taskhub.auth.JwtService;
import pl.witold.taskhub.task.dto.TaskResponse;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void getById_shouldReturn200AndTask() throws Exception {
        // given
        TaskResponse response = new TaskResponse(
                1L,
                "Learn JUnit",
                "Controller test",
                TaskPriority.HIGH,
                TaskStatus.NEW,
                LocalDateTime.of(2026, 9, 29, 20, 0),
                LocalDateTime.of(2026, 9, 29, 20, 0),
                List.of()
        );

        when(taskService.getById(1L))
                .thenReturn(response);

        // when + then
        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Learn JUnit"))
                .andExpect(jsonPath("$.description").value("Controller test"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    void getById_shouldReturn404WhenTaskDoesNotExist() throws Exception {
        // given
        when(taskService.getById(99L))
                .thenThrow(new TaskNotFoundException(99L));

        // when + then
        mockMvc.perform(get("/api/tasks/99"))
                .andExpect(status().isNotFound());
    }
}