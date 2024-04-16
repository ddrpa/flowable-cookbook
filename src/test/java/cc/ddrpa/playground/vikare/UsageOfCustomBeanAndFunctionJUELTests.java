package cc.ddrpa.playground.vikare;

import cc.ddrpa.playground.vikare.juel.CalendarDueDateInWorkdaysFunctionDelegate;
import cc.ddrpa.playground.vikare.juel.CustomCalendarService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("mem")
public class UsageOfCustomBeanAndFunctionJUELTests {
    private static final Logger logger = LoggerFactory.getLogger(UsageOfCustomBeanAndFunctionJUELTests.class);

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private CustomCalendarService customCalendarService;

    @Test
    void customBeanMethodTest() {
        runtimeService.startProcessInstanceByKey("usage_of_custom_bean_and_function_juel");
        var task = taskService.createTaskQuery().taskAssignee("tom").singleResult();
        LocalDate dueDate = LocalDate.ofInstant(task.getDueDate().toInstant(), ZoneId.of("UTC+8"));
        LocalDate expectedDueDate = OffsetDateTime.parse(customCalendarService.dueDateInWorkDays(null, "P7D")).toLocalDate();
        assertEquals(expectedDueDate.toEpochDay(), dueDate.toEpochDay());

        taskService.complete(task.getId());
        task= taskService.createTaskQuery().taskAssignee("jerry").singleResult();
        dueDate = LocalDate.ofInstant(task.getDueDate().toInstant(), ZoneId.of("UTC+8"));
        expectedDueDate = OffsetDateTime.parse(CalendarDueDateInWorkdaysFunctionDelegate.dueDateInWorkDays("P14D")).toLocalDate();
        assertEquals(expectedDueDate.toEpochDay(), dueDate.toEpochDay());
    }
}