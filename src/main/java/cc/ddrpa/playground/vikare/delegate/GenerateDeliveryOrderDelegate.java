package cc.ddrpa.playground.vikare.delegate;

import cc.ddrpa.playground.vikare.repository.InventoryRepository;
import cc.ddrpa.playground.vikare.repository.MockHolidaysRepository;
import lombok.SneakyThrows;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class GenerateDeliveryOrderDelegate implements JavaDelegate {
    private static final SecureRandom random = new SecureRandom();
    private final InventoryRepository inventoryRepository;
    private final MockHolidaysRepository mockHolidaysRepository;

    public GenerateDeliveryOrderDelegate(InventoryRepository inventoryRepository, MockHolidaysRepository mockHolidaysRepository) {
        this.inventoryRepository = inventoryRepository;
        this.mockHolidaysRepository = mockHolidaysRepository;
    }

    @SneakyThrows
    @Override
    public void execute(DelegateExecution execution) {
        String testCase = (String) execution.getVariable("testcase");
        Thread.sleep(3000);
        // 生成发货单到外部系统
        if ("shouldKeepInventoryStockAdjustmentTest".equals(testCase)) {
            throw new RuntimeException("Failed to generate delivery order");
        }
        execution.setVariable("deliveryOrder", "DO-" + random.nextInt(1000));
    }
}