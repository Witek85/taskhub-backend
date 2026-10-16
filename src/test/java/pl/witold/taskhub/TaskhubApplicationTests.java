package pl.witold.taskhub;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"app.jwt.secret=test-secret-key-for-taskhub-tests-12345678901234567890"
})
class TaskhubApplicationTests {

	@Test
	void contextLoads() {
	}
}
