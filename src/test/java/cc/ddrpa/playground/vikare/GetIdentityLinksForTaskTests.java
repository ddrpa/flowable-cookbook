package cc.ddrpa.playground.vikare;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.identitylink.api.IdentityLink;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("mem")
public class GetIdentityLinksForTaskTests {
    private static final Logger logger = LoggerFactory.getLogger(GetIdentityLinksForTaskTests.class);
    private static final String PROCESS_DEFINITION_KEY = "get_identity_links_for_task";

    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;

    @Test
    void accessIdentityLinksForTaskTest() {
        runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY);
        var taskId = taskService.createTaskQuery().singleResult().getId();
        assertTrue(isAssigneeOrCandidate(taskId, "tom", List.of("cat-group")));
        assertFalse(isAssigneeOrCandidate(taskId, "jerry"));
        assertTrue(isAssigneeOrCandidate(taskId, "rick"));
        assertTrue(isAssigneeOrCandidate(taskId, "morty"));
        assertTrue(isAssigneeOrCandidate(taskId, "summer", List.of("smiths-group", "user-group-1")));
        assertTrue(isAssigneeOrCandidate(taskId, List.of("smiths-group", "user-group-1")));
    }

    private boolean isAssigneeOrCandidate(String taskId, String user, List<String> candidateGroups) {
        List<IdentityLink> links = taskService.getIdentityLinksForTask(taskId);
        return links.stream().anyMatch(link -> switch (link.getType()) {
            case "assignee" -> user.equals(link.getUserId());
            case "candidate" -> {
                if (user.equals(link.getUserId())) {
                    yield true;
                } else {
                    yield candidateGroups.contains(link.getGroupId());
                }
            }
            default -> false;
        });
    }

    private boolean isAssigneeOrCandidate(String taskId, String user) {
        List<IdentityLink> links = taskService.getIdentityLinksForTask(taskId);
        return links.stream().anyMatch(link -> user.equals(link.getUserId()));
    }

    private boolean isAssigneeOrCandidate(String taskId, List<String> candidateGroups) {
        List<IdentityLink> links = taskService.getIdentityLinksForTask(taskId);
        return links.stream().anyMatch(link -> "candidate".equals(link.getType()) && candidateGroups.contains(link.getGroupId()));
    }
}