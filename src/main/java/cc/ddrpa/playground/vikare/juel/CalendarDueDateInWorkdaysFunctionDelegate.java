package cc.ddrpa.playground.vikare.juel;

import cc.ddrpa.playground.vikare.BPMNEngineConfiguration;
import org.flowable.common.engine.api.delegate.FlowableFunctionDelegate;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 参考 {@link BPMNEngineConfiguration} 的方法进行注册
 */
public class CalendarDueDateInWorkdaysFunctionDelegate implements FlowableFunctionDelegate {
    @Override
    public String prefix() {
        return "calendar";
    }

    @Override
    public String localName() {
        return "dueDateInWorkDays";
    }

    @Override
    public Method functionMethod() {
        try {
            return CalendarDueDateInWorkdaysFunctionDelegate.class.getDeclaredMethod("dueDateInWorkDays", String.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Could not find dueDateInWorkDays function", e);
        }
    }

    public static String dueDateInWorkDays(String duration) {
        var now = LocalDateTime.now();
        Duration d = Duration.parse(duration);
        // 这部分工作日的计算逻辑是有问题的
        // 不过只要保证相同的输入会得到相同的输出就足够演示了
        var dueDate = now.plus(d).plusDays(d.toDays() % 7 * 2);
        return ZonedDateTime.of(dueDate, ZoneId.of("UTC+8")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}