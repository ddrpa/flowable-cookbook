package cc.ddrpa.playground.vikare.delegate;

import cc.ddrpa.playground.vikare.repository.InventoryRepository;
import cc.ddrpa.playground.vikare.repository.MockHolidaysRepository;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class AdjustInventoryStockDelegate implements JavaDelegate {
    private final InventoryRepository inventoryRepository;
    private final MockHolidaysRepository mockHolidaysRepository;

    public AdjustInventoryStockDelegate(InventoryRepository inventoryRepository, MockHolidaysRepository mockHolidaysRepository) {
        this.inventoryRepository = inventoryRepository;
        this.mockHolidaysRepository = mockHolidaysRepository;
    }

    @Override
    public void execute(DelegateExecution execution) {
        // 在这里获取到流程表单等信息进行扣减工作
        var inventoryId = (Long) execution.getVariable("inventoryId");
        var amount = (Integer) execution.getVariable("amount");
        var inventory = inventoryRepository.findById(inventoryId).get();
        if (inventory.getAmount() < amount) {
            throw new RuntimeException("inventory stock amount is negative");
        } else {
            // 不考虑并发问题
            inventory.setAmount(inventory.getAmount() - amount);
            inventoryRepository.save(inventory);
        }
    }
}