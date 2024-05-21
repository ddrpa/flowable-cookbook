package cc.ddrpa.playground.vikare;

import cc.ddrpa.playground.vikare.event.UserTaskCompletedEventListener;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 展示全局事件监听的作用
 * {@link UserTaskCompletedEventListener} 是一个全局事件监听器，
 * 在 {@link cc.ddrpa.playground.vikare.BPMNEngineConfiguration} 中注册监听 TASK_COMPLETED 事件
 */
@SpringBootTest
@ActiveProfiles("mem")
public class ExecutionListenersTests {
    private static final Logger logger = LoggerFactory.getLogger(ExecutionListenersTests.class);
    private static final String PROCESS_DEFINITION_KEY = "execution_listeners";

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;

    @Test
    void runningTest() {
        runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, Map.of("stage-start", "process start", "next_usergroup", "tom"));
        int taskCounter = 1;
        while (true) {
            var taskList = taskService.createTaskQuery().taskCandidateGroupIn(List.of("tom", "jerry")).list();
            if (taskList.size() == 0) {
                break;
            }
            assertEquals(1, taskList.size());
            var taskId = taskList.get(0).getId();
            if (taskCounter % 2 == 0) {
                // 有些任务不需要变量，看看会不会产生不同的事件
                taskService.complete(taskId, "tom-" + taskCounter, Map.of("stage-task-" + taskCounter, UUID.randomUUID(), "next_usergroup", "jerry"));
            } else {
                taskService.complete(taskId, "jerry-" + taskCounter, Map.of("next_usergroup", "tom"));
            }
            taskCounter++;
        }
    }
}