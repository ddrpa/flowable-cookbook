package cc.ddrpa.playground.vikare.repository;

import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class MockHolidaysRepository {

    /**
     * 这部分工作日的计算逻辑是有问题的
     * 不过只要保证相同的输入会得到相同的输出就足够演示了
     * @param d
     * @return
     */
    public long countDayOff(Duration d) {
        return d.toDays() % 7 * 2;
    }
}