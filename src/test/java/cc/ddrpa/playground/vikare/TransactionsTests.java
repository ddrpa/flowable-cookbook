package cc.ddrpa.playground.vikare;

import cc.ddrpa.playground.vikare.repository.Inventory;
import cc.ddrpa.playground.vikare.repository.InventoryRepository;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.security.SecureRandom;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("mem")
public class TransactionsTests {
    private static final Logger logger = LoggerFactory.getLogger(TransactionsTests.class);

    private static final String PROCESS_DEFINITION_KEY = "transactions";

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private InventoryRepository inventoryRepository;

    private final SecureRandom random = new SecureRandom();

    @Test
    void shouldFallbackToUserOrderTest() {
        // register a new inventory
        var InventoryId = random.nextLong();
        inventoryRepository.save(new Inventory(InventoryId, 10));

        runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);
        var task = taskService.createTaskQuery().singleResult();
        taskService.complete(task.getId(), Map.of("inventoryId", InventoryId, "amount", 50));
        // TODO
    }

    @Test
    void shouldKeepInventoryStockAdjustmentTest() throws InterruptedException {
        // register a new inventory
        var InventoryId = random.nextLong(10000);
        inventoryRepository.save(new Inventory(InventoryId, 10));

        runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);
        var task = taskService.createTaskQuery().singleResult();
        assertEquals(10, inventoryRepository.findById(InventoryId).get().getAmount());
        taskService.complete(task.getId(), Map.of("inventoryId", InventoryId, "amount", 1, "testcase", "shouldKeepInventoryStockAdjustmentTest"));
        assertEquals(9, inventoryRepository.findById(InventoryId).get().getAmount());
        Thread.sleep(5000);
        assertEquals(9, inventoryRepository.findById(InventoryId).get().getAmount());
    }
}