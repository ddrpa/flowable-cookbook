package cc.ddrpa.playground.vikare;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("mem")
public class VariablesScopeTests {
    private static final Logger logger = LoggerFactory.getLogger(VariablesScopeTests.class);

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;

    private static final String outsiderUserGroup = "outsider";
    private static final List<String> subProcessUserTaskCandidateGroup = List.of("user-group-1", "user-group-2");

    @Test
    void subprocessVariableShouldBeScopedTest() {
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("variables_scope", Map.of("workgroup", outsiderUserGroup));
        assertEquals(1, taskService.createTaskQuery().taskCandidateGroup(outsiderUserGroup).count());
        subProcessUserTaskCandidateGroup.forEach(userGroup -> assertEquals(0, taskService.createTaskQuery().taskCandidateGroup(userGroup).count()));

        var task = taskService.createTaskQuery().taskCandidateGroup(outsiderUserGroup).singleResult();
        var executionId = task.getExecutionId();
        logger.info("Execution ID: {}, workgroup: {}", executionId, runtimeService.getVariable(executionId, "workgroup"));

        taskService.complete(task.getId(), Map.of("workgroups", subProcessUserTaskCandidateGroup, "action", "reassign_to_multi"));
        assertEquals(0, taskService.createTaskQuery().taskCandidateGroup(outsiderUserGroup).count());
        subProcessUserTaskCandidateGroup.forEach(userGroup -> {
            assertEquals(1, taskService.createTaskQuery().taskCandidateGroup(userGroup).count());
            var subProcessUserTask = taskService.createTaskQuery().taskCandidateGroup(userGroup).singleResult();

            var subProcessUserTaskExecutionId = subProcessUserTask.getExecutionId();
            logger.info("Execution ID: {}, workgroup: {}", subProcessUserTaskExecutionId, runtimeService.getVariable(subProcessUserTaskExecutionId, "workgroup"));

            taskService.complete(subProcessUserTask.getId(), Map.of("action", "complete"));
            assertEquals(0, taskService.createTaskQuery().taskCandidateGroup(userGroup).count());
        });

        assertEquals(1, taskService.createTaskQuery().taskCandidateGroup(outsiderUserGroup).count());
        task = taskService.createTaskQuery().taskCandidateGroup(outsiderUserGroup).singleResult();
        executionId = task.getExecutionId();
        logger.info("Execution ID: {}, workgroup: {}", executionId, runtimeService.getVariable(executionId, "workgroup"));
    }

    public VariablesScopeTests setRuntimeService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
        return this;
    }
}