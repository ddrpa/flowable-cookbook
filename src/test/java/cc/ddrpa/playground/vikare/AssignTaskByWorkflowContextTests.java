package cc.ddrpa.playground.vikare;

import org.flowable.engine.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static cc.ddrpa.playground.vikare.delegate.TaskAssignmentDecisionTableDelegate.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 运行时根据流程上下文和预设业务逻辑更改用户任务的指派方式到：
 * - 指定用户处理
 * - 指定一个候选用户列表
 * - 指定候选用户组
 */
@SpringBootTest
@ActiveProfiles("mem")
public class AssignTaskByWorkflowContextTests {
    private static final Logger logger = LoggerFactory.getLogger(AssignTaskByWorkflowContextTests.class);
    private static final String PROCESS_DEFINITION_KEY = "assign_task_by_workflow_context";

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;

    /**
     * 任务被指派到单个用户
     * 用户1或用户2的指派任务列表+1
     */
    @Test
    void aUserShouldBeAssignedWithOneTaskTest() {
        var counter1 = countTaskAssignment();
        runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, Map.of("action", Action.ASSIGNEE_TO_SINGLE_USER));
        var counter2 = countTaskAssignment();

        assertEquals(counter1.singleUser1AssigneeCount + counter1.singleUser2AssigneeCount + 1,
                counter2.singleUser1AssigneeCount + counter2.singleUser2AssigneeCount);
        assertEquals(counter1.singleUser1CandidateCount, counter2.singleUser1CandidateCount);
        assertEquals(counter1.singleUser2CandidateCount, counter2.singleUser2CandidateCount);
        assertEquals(counter1.candidateGroupCount, counter2.candidateGroupCount);
    }

    /**
     * 任务被指派给一组用户
     * 用户1和用户2的候选任务列表+1
     */
    @Test
    void bothUserShouldBeCandidateUserOfTheTaskTest() {
        var counter1 = countTaskAssignment();
        runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, Map.of("action", Action.ASSIGNEE_TO_MULTI_USERS));
        var counter2 = countTaskAssignment();

        assertEquals(counter1.singleUser1AssigneeCount,counter2.singleUser1AssigneeCount);
        assertEquals(counter1.singleUser2AssigneeCount,counter2.singleUser2AssigneeCount);
        assertEquals(counter1.singleUser1CandidateCount + 1, counter2.singleUser1CandidateCount);
        assertEquals(counter1.singleUser2CandidateCount + 1, counter2.singleUser2CandidateCount);
        assertEquals(counter1.candidateGroupCount, counter2.candidateGroupCount);
    }

    /**
     * 任务指派给候选用户组
     * 候选用户组的任务列表+1
     */
    @Test
    void candidateUserGroupShouldBeCandidateGroupOfTheTaskTest() {
        var counter1 = countTaskAssignment();
        runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, Map.of("action", Action.ASSIGNEE_TO_GROUP));
        var counter2 = countTaskAssignment();

        assertEquals(counter1.singleUser1AssigneeCount,counter2.singleUser1AssigneeCount);
        assertEquals(counter1.singleUser2AssigneeCount,counter2.singleUser2AssigneeCount);
        assertEquals(counter1.singleUser1CandidateCount, counter2.singleUser1CandidateCount);
        assertEquals(counter1.singleUser2CandidateCount, counter2.singleUser2CandidateCount);
        assertEquals(counter1.candidateGroupCount + 1, counter2.candidateGroupCount);
    }

    private record TaskAssignmentCounter(
            long singleUser1AssigneeCount,
            long singleUser1CandidateCount,
            long singleUser2AssigneeCount,
            long singleUser2CandidateCount,
            long candidateGroupCount
    ) {
    }

    private TaskAssignmentCounter countTaskAssignment() {
        return new TaskAssignmentCounter(
                taskService.createTaskQuery().taskAssignee(SINGLE_USER_1).count(),
                taskService.createTaskQuery().taskCandidateUser(SINGLE_USER_1).count(),
                taskService.createTaskQuery().taskAssignee(SINGLE_USER_2).count(),
                taskService.createTaskQuery().taskCandidateUser(SINGLE_USER_2).count(),
                taskService.createTaskQuery().taskCandidateGroup(CANDIDATE_USER_GROUPS).count()
        );
    }
}