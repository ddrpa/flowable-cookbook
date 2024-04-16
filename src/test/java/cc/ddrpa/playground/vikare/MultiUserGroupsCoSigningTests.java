package cc.ddrpa.playground.vikare;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 每个用户组中都需要有用户完成任务才会进入下个环节
 * 演示多实例用户任务的使用
 */
@SpringBootTest
@ActiveProfiles("mem")
public class MultiUserGroupsCoSigningTests {
    private static final Logger logger = LoggerFactory.getLogger(MultiUserGroupsCoSigningTests.class);
    private static final List<String> CANDIDATE_GROUPS = List.of(
            "candidate_group_1",
            "candidate_group_2",
            "candidate_group_3",
            "candidate_group_4",
            "candidate_group_5");
    private static final SecureRandom random = new SecureRandom();

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;

    /**
     * 任务需要所有候选用户组中的用户完成
     */
    @Test
    void allCandidateGroupsShouldCompleteTask() {
        CANDIDATE_GROUPS.forEach(group -> assertEquals(0, taskService.createTaskQuery().taskCandidateGroup(group).count()));

        // 候选用户组任务列表增加
        runtimeService.startProcessInstanceByKey("multi_groups_cosigning", Map.of("workgroups", CANDIDATE_GROUPS));
        CANDIDATE_GROUPS.forEach(group -> assertEquals(1, taskService.createTaskQuery().taskCandidateGroup(group).count()));

        CANDIDATE_GROUPS.forEach(group -> {
            // 只要有一个用户组中的用户没有完成任务，下一环节不会收到任务
            assertEquals(0, taskService.createTaskQuery().taskAssignee("tom").count());
            var taskId = taskService.createTaskQuery().taskCandidateGroup(group).singleResult().getId();
            taskService.complete(taskId, "user_in_" + group);
            logger.info("Task {} completed by user_in_{}", taskId, group);
            assertEquals(0, taskService.createTaskQuery().taskCandidateGroup(group).count());
        });
        // 全部完成后下一环节收到任务
        assertEquals(1, taskService.createTaskQuery().taskAssignee("tom").count());
    }

    /**
     * 当任意用户更新任务的状态（而不完成）时
     * 系统的结束这批任务并创建新任务，所有的任务计时器都被重置
     * 注意如果有用户已经完成了任务，也会收到新的任务，这时候可以在外部系统记录用户的上一次提交并替用户自动提交
     */
    @Test
    void coSigningNeedUpdatingWithExternalStorageTest() throws InterruptedException {
        Map<String, Map<String, Object>> mockExternalStorageSystem = new HashMap<>();
        CANDIDATE_GROUPS.forEach(group -> assertEquals(0, taskService.createTaskQuery().taskCandidateGroup(group).count()));

        var triple = userGroupClassifier();
        // 选择一个用户完成任务
        var inWhichGroupTheUserCompleteTask = triple.left;
        // 选择一个用户更新任务
        var inWhichGroupTheUserUpdateTask = triple.middle;

        runtimeService.startProcessInstanceByKey("multi_groups_cosigning_need_update", Map.of("workgroups", CANDIDATE_GROUPS));
        CANDIDATE_GROUPS.forEach(group -> {
            // 任务列表增加
            assertEquals(1, taskService.createTaskQuery().taskCandidateGroup(group).count());
            // 没有任务过期
            assertEquals(0, taskService.createTaskQuery().taskDueBefore(new Date()).count());
        });

        // 某用户完成任务
        var userId = "user_in_" + inWhichGroupTheUserCompleteTask;
        Map<String, Object> userSubmitData = Map.of("action", "complete", "some_data", userId);
        // 在某个模拟的外部系统中存储表单
        mockExternalStorageSystem.put(inWhichGroupTheUserCompleteTask, userSubmitData);
        var taskId = taskService.createTaskQuery().taskCandidateGroup(inWhichGroupTheUserCompleteTask).singleResult().getId();
        taskService.complete(taskId, userSubmitData);
        assertEquals(0, taskService.createTaskQuery().taskCandidateGroup(inWhichGroupTheUserCompleteTask).count());

        // 等待任务超时
        Thread.sleep(4000);
        // 完成任务的用户组没有超时任务
        assertEquals(0, taskService.createTaskQuery().taskCandidateGroup(inWhichGroupTheUserCompleteTask).taskDueBefore(new Date()).count());
        // 未完成任务的用户组有超时任务
        CANDIDATE_GROUPS.stream()
                .filter(group -> !group.equals(inWhichGroupTheUserCompleteTask))
                .forEach(group -> assertEquals(1, taskService.createTaskQuery().taskCandidateGroup(group).taskDueBefore(new Date()).count()));

        // 某用户更新状态
        var anotherUserId = "user_in_" + inWhichGroupTheUserUpdateTask;
        Map<String, Object> anotherUserSubmitData = Map.of("action", "update", "some_data", anotherUserId);
        var anotherTaskId = taskService.createTaskQuery().taskCandidateGroup(inWhichGroupTheUserUpdateTask).singleResult().getId();
        taskService.complete(anotherTaskId, anotherUserSubmitData);

        // token 重新进入到同一个用户任务，现在每个用户又有任务了
        CANDIDATE_GROUPS.forEach(group -> assertEquals(1, taskService.createTaskQuery().taskCandidateGroup(group).count()));
        // 没有任务过期
        assertEquals(0, taskService.createTaskQuery().taskDueBefore(new Date()).count());
        assertEquals(0, taskService.createTaskQuery().taskAssignee("tom").count());

        // 对已完成任务的用户组，需要自动提交之前提交的任务
        mockExternalStorageSystem.forEach((group, data) -> {
            var tid = taskService.createTaskQuery().taskCandidateGroup(group).singleResult().getId();
            taskService.complete(tid, data);
        });

        // 其他用户还是有待完成任务
        // 当剩余用户完成任务时，进入下一个环节
        CANDIDATE_GROUPS.forEach(group -> {
            if (mockExternalStorageSystem.containsKey(group)) {
                assertEquals(0, taskService.createTaskQuery().taskCandidateGroup(group).count());
            } else {
                assertEquals(1, taskService.createTaskQuery().taskCandidateGroup(group).count());
                var tid = taskService.createTaskQuery().taskCandidateGroup(group).singleResult().getId();
                taskService.complete(tid, Map.of("action", "complete", "some_data", "user_in_" + group));
                assertEquals(0, taskService.createTaskQuery().taskCandidateGroup(group).count());
            }
        });
        assertEquals(1, taskService.createTaskQuery().taskAssignee("tom").count());
    }

    /**
     * 把用户任务放在多实例子流程中，不同的用户组之间的状态就不会互相干扰
     */
    @Test
    void coSigningNeedUpdatingWithSubProcessTest() throws InterruptedException {
        CANDIDATE_GROUPS.forEach(group -> assertEquals(0, taskService.createTaskQuery().taskCandidateGroup(group).count()));
        var triple = userGroupClassifier();
        // 选择一个用户完成任务
        var inWhichGroupTheUserCompleteTask = triple.left;
        // 选择一个用户更新任务
        var inWhichGroupTheUserUpdateTask = triple.middle;

        runtimeService.startProcessInstanceByKey("multi_groups_cosigning_need_update_with_subprocess", Map.of("workgroups", CANDIDATE_GROUPS));
        CANDIDATE_GROUPS.forEach(group -> {
            // 任务列表增加
            assertEquals(1, taskService.createTaskQuery().taskCandidateGroup(group).count());
            // 没有任务过期
            assertEquals(0, taskService.createTaskQuery().taskDueBefore(new Date()).count());
        });

        // 某用户完成任务
        var userId = "user_in_" + inWhichGroupTheUserCompleteTask;
        var taskId = taskService.createTaskQuery().taskCandidateGroup(inWhichGroupTheUserCompleteTask).singleResult().getId();
        taskService.complete(taskId, Map.of("action", "complete", "some_data", userId));
        assertEquals(0, taskService.createTaskQuery().taskCandidateGroup(inWhichGroupTheUserCompleteTask).count());

        // 等待任务超时
        Thread.sleep(4000);
        // 完成任务的用户组没有超时任务
        assertEquals(0, taskService.createTaskQuery().taskCandidateGroup(inWhichGroupTheUserCompleteTask).taskDueBefore(new Date()).count());
        // 未完成任务的用户组有超时任务
        CANDIDATE_GROUPS.stream()
                .filter(group -> !group.equals(inWhichGroupTheUserCompleteTask))
                .forEach(group -> assertEquals(1, taskService.createTaskQuery().taskCandidateGroup(group).taskDueBefore(new Date()).count()));

        // 某用户更新状态
        var anotherUserId = "user_in_" + inWhichGroupTheUserUpdateTask;
        var anotherTaskId = taskService.createTaskQuery().taskCandidateGroup(inWhichGroupTheUserUpdateTask).singleResult().getId();
        taskService.complete(anotherTaskId, Map.of("action", "update", "some_data", anotherUserId));

        assertEquals(0, taskService.createTaskQuery().taskAssignee("tom").count());
        // 更新状态的用户组获得了新的用户任务
        assertEquals(1, taskService.createTaskQuery().taskCandidateGroup(inWhichGroupTheUserUpdateTask).count());
        // 完成任务的用户组没有任务
        assertEquals(0, taskService.createTaskQuery().taskCandidateGroup(inWhichGroupTheUserCompleteTask).count());
        // 更新状态的用户组和完成任务的用户组没有任务过期
        assertEquals(CANDIDATE_GROUPS.size() - 2, taskService.createTaskQuery().taskDueBefore(new Date()).count());
        // 当剩余用户完成任务时，进入下一个环节
        CANDIDATE_GROUPS.stream()
                .filter(group -> !group.equals(inWhichGroupTheUserCompleteTask))
                .forEach(group -> {
                    assertEquals(1, taskService.createTaskQuery().taskCandidateGroup(group).count());
                    var tid = taskService.createTaskQuery().taskCandidateGroup(group).singleResult().getId();
                    taskService.complete(tid, Map.of("action", "complete", "some_data", "user_in_" + group));
                    assertEquals(0, taskService.createTaskQuery().taskCandidateGroup(group).count());
                });
        assertEquals(1, taskService.createTaskQuery().taskAssignee("tom").count());
    }

    private ImmutableTriple<String, String, List<String>> userGroupClassifier() {
        // 选择一个用户完成任务
        var groupSize = MultiUserGroupsCoSigningTests.CANDIDATE_GROUPS.size();
        var inWhichGroupTheUserCompleteTask = MultiUserGroupsCoSigningTests.CANDIDATE_GROUPS.get(random.nextInt(groupSize));
        // 选择一个用户更新任务
        var inWhichGroupTheUserUpdateTask = MultiUserGroupsCoSigningTests.CANDIDATE_GROUPS.stream()
                .filter(group -> !group.equals(inWhichGroupTheUserCompleteTask))
                .toList()
                .get(random.nextInt(groupSize - 1));
        // 剩余用户
        var restUserGroups = MultiUserGroupsCoSigningTests.CANDIDATE_GROUPS.stream()
                .filter(group -> !group.equals(inWhichGroupTheUserCompleteTask) && !group.equals(inWhichGroupTheUserUpdateTask))
                .toList();
        return ImmutableTriple.of(inWhichGroupTheUserCompleteTask, inWhichGroupTheUserUpdateTask, restUserGroups);
    }
}