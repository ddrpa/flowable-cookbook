package cc.ddrpa.playground.vikare.service;

import cc.ddrpa.playground.vikare.service.dto.FlowDirection;
import cc.ddrpa.playground.vikare.service.dto.UserTaskDefinition;
import cc.ddrpa.playground.vikare.service.dto.UserTaskSubmission;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flowable.engine.*;
import org.flowable.engine.form.StartFormData;
import org.flowable.identitylink.api.IdentityLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DocumentationAsUserTaskDefinitionService {
    private final TaskService taskService;
    private final FormService formService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final RepositoryService repositoryService;
    private final ObjectMapper objectMapper;

    public DocumentationAsUserTaskDefinitionService(TaskService taskService, FormService formService, RuntimeService runtimeService, HistoryService historyService, RepositoryService repositoryService, ObjectMapper objectMapper) {
        this.taskService = taskService;
        this.formService = formService;
        this.runtimeService = runtimeService;
        this.historyService = historyService;
        this.repositoryService = repositoryService;
        this.objectMapper = objectMapper;
    }

    private static final Logger logger = LoggerFactory.getLogger(DocumentationAsUserTaskDefinitionService.class);

    /**
     * 获取指定流程的启动参数
     *
     * @param processDefinitionKey
     * @return
     * @throws JsonProcessingException
     */
    public UserTaskDefinition fetchStartEventDefinition(String processDefinitionKey) throws JsonProcessingException {
        var latestProcessDefinitionId = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .latestVersion()
                .singleResult()
                .getId();
        return getProcessStartEventDefinition(latestProcessDefinitionId);
    }

    /**
     * 启动流程
     * 对流程启动事件，不执行流向逻辑，只获取启动表单参数并提交
     *
     * @param initiatorUserId
     * @param processDefinitionKey
     */
    public void startProcess(String processDefinitionKey, String initiatorUserId, UserTaskSubmission submission) throws JsonProcessingException {
        var latestProcessDefinitionId = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .latestVersion()
                .singleResult()
                .getId();
        UserTaskDefinition definition = getProcessStartEventDefinition(processDefinitionKey);
        // SKIPPED 检查用户提交内容
        // SKIPPED 检查用户权限
        var userFlowDirection = submission.flowDirection();
        // if userFlowDirection is in definition.flowDirection
        if (definition.flowDirection().stream().map(FlowDirection::key).noneMatch(p -> p.equals(userFlowDirection))) {
            throw new IllegalArgumentException("Invalid flow direction");
        }
        Map<String, String> userTaskForm = new HashMap<>();
        userTaskForm.put("initiator", initiatorUserId);
        JsonNode userFormData = submission.userFormData();
        StartFormData startFormDefinition = formService.getStartFormData(latestProcessDefinitionId);
        for (var formProperty : startFormDefinition.getFormProperties()) {
            var formKey = formProperty.getId();
            if (userFormData.has(formKey)) {
                userTaskForm.put(formKey, userFormData.get(formKey).asText());
            } else {
                throw new IllegalArgumentException("Missing required form data: " + formKey);
            }
        }
        formService.submitStartFormData(latestProcessDefinitionId, userTaskForm);
    }

    /**
     * 获取用户任务的定义
     * @param taskId
     * @param userId
     * @return
     */
    public UserTaskDefinition fetchUserTaskDefinition(String taskId, String userId) throws JsonProcessingException {
        if (!isUserAuthorized(taskId, userId)) {
            throw new IllegalArgumentException("User is not authorized to view this task");
        }
        var task = taskService.createTaskQuery().taskId(taskId).singleResult();
        return objectMapper.readValue(task.getDescription(), UserTaskDefinition.class);
    }

//    public void completeUserTask(String taskId, String userId) {
//        var task = taskService.createTaskQuery().taskId(taskId).singleResult();
//        // 判断用户是否有权限完成该用户任务
//        List<IdentityLink> links = taskService.getIdentityLinksForTask(taskId);
//        // TODO 检查用户提交内容
//        Map<String, String> userTaskForm = new HashMap<>();
//        JsonNode userFormData = submission.userFormData();
//        for (var formProperty : formService.getTaskFormData(taskId).getFormProperties()) {
//            var formKey = formProperty.getId();
//            if (userFormData.has(formKey)) {
//                userTaskForm.put(formKey, userFormData.get(formKey).asText());
//            } else {
//                throw new IllegalArgumentException("Missing required form data: " + formKey);
//            }
//        }
//        taskService.complete(taskId, userTaskForm);
//    }


    private UserTaskDefinition getProcessStartEventDefinition(String processDefinitionId) throws JsonProcessingException {
        var model = repositoryService.getBpmnModel(processDefinitionId);
        String documentation = model.getFlowElement("none_start_event").getDocumentation();
        return objectMapper.readValue(documentation, UserTaskDefinition.class);
    }

    private boolean isUserAuthorized(String taskId, String userId) {
        List<IdentityLink> links = taskService.getIdentityLinksForTask(taskId);
        for (IdentityLink link : links) {
//            if (IdentityLinkType.CANDIDATE.equals(link.getType())) {
//                String userId = link.getUserId();
//                if (userId != null) {
//                    users.add(userId);
//                }
//                String groupId = link.getGroupId();
//                if (groupId != null) {
//                    groups.add(groupId);
//                }
//            }
        }

        if (taskService.createTaskQuery().taskId(taskId).taskCandidateOrAssigned(userId).count() > 0) {
            return true;
        }
        // SKIPPED get user candidate group by userId
        List<String> userCandidateGroups = new ArrayList<>();
        if (taskService.createTaskQuery().taskId(taskId).taskCandidateGroupIn(userCandidateGroups).count() > 0) {
            return true;
        }
        return false;
    }
}