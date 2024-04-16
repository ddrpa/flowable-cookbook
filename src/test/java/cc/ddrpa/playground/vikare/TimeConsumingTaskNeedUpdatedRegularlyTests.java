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
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 预计耗时很长的用户任务，有些业务中会要求处理人（组）定期更新任务状态
 * 当用户更新任务状态时，任务的到期计时器应当被重置
 */
@SpringBootTest
@ActiveProfiles("mem")
public class TimeConsumingTaskNeedUpdatedRegularlyTests {
    private static final Logger logger = LoggerFactory.getLogger(TimeConsumingTaskNeedUpdatedRegularlyTests.class);

    private static final String PROCESS_DEFINITION_KEY = "time_consuming_task_need_updated_regularly";

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;

    private final SecureRandom random = new SecureRandom();

    /**
     * 当用户更新而不结束任务时，实际上新创建了一个任务
     * dueDate 在 token 落到任务上时开始计时，可见：
     * <a href="https://www.digi.com/resources/documentation/digidocs/90001488-13/reference/r_iso_8601_duration_format.htm">有关 ISO 8601 时间间隔格式的说明</a>
     *
     * @throws InterruptedException
     */
    @Test
    void userTaskTimeoutTest() throws InterruptedException {
        var delayedTaskCount = countDelayedTask();
        var rickAndMortyTaskCandidateTaskCount = countRickAndMortyCandidateTask();

        // 防止干扰，需清除历史数据
        assertEquals(0, delayedTaskCount);
        assertEquals(0, rickAndMortyTaskCandidateTaskCount);

        runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);
        assertEquals(rickAndMortyTaskCandidateTaskCount + 2, countRickAndMortyCandidateTask());
        taskService.createTaskQuery().list()
                .forEach(task -> logger.info("Task {} expired on {}", task.getId(), task.getDueDate()));

        rickAndMortyTaskCandidateTaskCount += 2;
        while (rickAndMortyTaskCandidateTaskCount != 0) {
            // 任务在3秒后超时
            Thread.sleep(4000);
            logger.info("all task should be delayed.");
            // 应当有1个超时任务
            assertEquals(delayedTaskCount + 1, countDelayedTask());
            delayedTaskCount++;
            for (var task : taskService.createTaskQuery().taskDueBefore(new Date()).list()) {
                logger.info("Task {} expired on {}", task.getId(), task.getDueDate());
                var taskId = task.getId();
                taskService.claim(taskId, random.nextBoolean() ? "rick" : "morty");
                // 无论是更新还是完成任务，到期的任务都会被消耗
                delayedTaskCount--;
                rickAndMortyTaskCandidateTaskCount -= 2;
                if (random.nextFloat() > 0.8) {
                    taskService.complete(taskId, random.nextBoolean() ? "rick" : "morty", Map.of("action", "update"));
                    // 但是更新会产生新的任务
                    rickAndMortyTaskCandidateTaskCount += 2;
                } else {
                    taskService.complete(taskId, random.nextBoolean() ? "rick" : "morty", Map.of("action", "complete"));
                }
                assertEquals(delayedTaskCount, countDelayedTask());
                assertEquals(rickAndMortyTaskCandidateTaskCount, countRickAndMortyCandidateTask());
            }
            taskService.createTaskQuery().list()
                    .forEach(task -> logger.info("Task {} expired on {}", task.getId(), task.getDueDate()));
        }
        // 结束任务后落到下一个环节
        assertEquals(1, taskService.createTaskQuery().taskAssignee("squanchy").count());
    }

    private long countDelayedTask() {
        return taskService.createTaskQuery().taskDueBefore(new Date()).count();
    }

    private long countRickAndMortyCandidateTask() {
        return taskService.createTaskQuery().taskCandidateUser("rick").count()
                + taskService.createTaskQuery().taskCandidateUser("morty").count();
    }
}