package cc.ddrpa.playground.vikare.juel;

import cc.ddrpa.playground.vikare.repository.MockHolidaysRepository;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CustomCalendarService  {
    private final MockHolidaysRepository mockHolidaysRepository;

    public CustomCalendarService(MockHolidaysRepository mockHolidaysRepository) {
        this.mockHolidaysRepository = mockHolidaysRepository;
    }

    public String dueDateInWorkDays(ExecutionEntity execution, String duration) {
        var now = LocalDateTime.now();
        Duration d = Duration.parse(duration);
        var dayOff = mockHolidaysRepository.countDayOff(d);
        var dueDate = now.plus(d).plusDays(dayOff);
        return ZonedDateTime.of(dueDate, ZoneId.of("UTC+8")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}