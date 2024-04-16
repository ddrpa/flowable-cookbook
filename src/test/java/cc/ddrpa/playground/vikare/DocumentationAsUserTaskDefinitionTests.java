package cc.ddrpa.playground.vikare;

import cc.ddrpa.playground.vikare.service.DocumentationAsUserTaskDefinitionService;
import cc.ddrpa.playground.vikare.service.dto.UserTaskDefinition;
import cc.ddrpa.playground.vikare.service.dto.UserTaskSubmission;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("mem")
public class DocumentationAsUserTaskDefinitionTests {
    private static final Logger logger = LoggerFactory.getLogger(DocumentationAsUserTaskDefinitionTests.class);

    @Autowired
    private DocumentationAsUserTaskDefinitionService documentationAsUserTaskDefinitionService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getStartEventRequirementTest() throws JsonProcessingException {
        // mock request parameters
        // GET /process/{processDefinitionKey}/start
        String processDefinitionKey = "documentation_as_user_task_definition";

        // simulate controller logic
        UserTaskDefinition startInfo = documentationAsUserTaskDefinitionService.fetchStartEventDefinition(processDefinitionKey);
        // just return startInfo
        logger.info("Start event requirement: {}", objectMapper.writeValueAsString(startInfo));
    }

    @Test
    void userStartProcessTest() throws JsonProcessingException {
        // mock request parameters
        // POST /process/{processDefinitionKey}/start
        // -H Authorization: Bearer {token}
        // SKIPPED if processDefinition available
        // SKIPPED user authentication
        String processDefinitionKey = "documentation_as_user_task_definition";
        String userId = "user1";
        UserTaskSubmission userSubmission = objectMapper.readValue("""
                {
                    "flow_direction": "event-report",
                    "form_data": {
                        "cascade-area-selector_label": ["浙江省", "衢州市", "柯城区", "XX街道", "XX社区", "XX社区022网格"],
                        "cascade-area-selector": ["33", "3308", "330802", "330802001", "330802001003", "330802001003022"],
                        "eventDetails": "event description"
                        "fileUploadURL": [
                            "http://oss.some-cloud.com/upload-bucket/00001/00001.jpeg",
                            "http://oss.some-cloud.com/upload-bucket/00001/00002.jpeg",
                            "http://oss.some-cloud.com/upload-bucket/00001/00003.jpeg"
                        ]
                    }
                """, UserTaskSubmission.class);

        // simulate controller logic
        documentationAsUserTaskDefinitionService.startProcess(processDefinitionKey, userId, userSubmission);
    }
//
//    @Test
//    void userCompleteTaskTest() throws JsonProcessingException {
//        // mock request parameters
//        // POST /process/{processDefinitionKey}/task/{taskId}/complete
//        // -H Authorization: Bearer {token}
//        // SKIPPED if processDefinition available
//        // SKIPPED user authentication
//        String processDefinitionKey = "documentation_as_user_task_definition";
//        String taskId = "task1";
//        String userId = "user1";
//        UserTaskSubmission userSubmission = objectMapper.readValue("""
//                {
//                    "flow_direction": "event-report",
//                    "form_data": {
//                        "cascade-area-selector_label": ["浙江省", "衢州市", "柯城区", "XX街道", "XX社区", "XX社区022网格"],
//                        "cascade-area-selector": ["33", "3308", "330802", "330802001", "330802001003", "330802001003022"],
//                        "eventDetails": "event description"
//                        "fileUploadURL": [
//                            "http://oss.some-cloud.com/upload-bucket/00001/00001.jpeg",
//                            "http://oss.some-cloud.com/upload-bucket/00001/00002.jpeg",
//                            "http://oss.some-cloud.com/upload-bucket/00001/00003.jpeg"
//                        ]
//                    }
//                """, UserTaskSubmission.class);
//
//        // simulate controller logic
//        documentationAsUserTaskDefinitionService.completeTask(processDefinitionKey, taskId, userId, userSubmission);
//    }

}