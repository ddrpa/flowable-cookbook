package cc.ddrpa.playground.vikare;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.security.SecureRandom;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("mem")
public class ProcedureAfterTimeoutTests {
    private static final Logger logger = LoggerFactory.getLogger(ProcedureAfterTimeoutTests.class);

    private static final String PROCESS_DEFINITION_KEY = "procedure_after_timeout";

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;

    private final SecureRandom random = new SecureRandom();

    @Test
    void taskShouldBeAssignedToAnotherUserAfterTimeout() throws InterruptedException {
        runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);
        assertEquals(1, taskService.createTaskQuery().taskAssignee("tom").count());
        do {
            Thread.sleep(1000);
            assertEquals(0, taskService.createTaskQuery().taskAssignee("jerry").count());
            assertEquals(1, taskService.createTaskQuery().taskAssignee("tom").count());
            taskService.complete(taskService.createTaskQuery().taskAssignee("tom").singleResult().getId(), Map.of("action", "update"));
        } while (random.nextFloat() < 0.8);
        Thread.sleep(4000);
        assertEquals(0, taskService.createTaskQuery().taskAssignee("tom").count());
        assertEquals(1, taskService.createTaskQuery().taskAssignee("jerry").count());
    }
}