package cc.ddrpa.playground.vikare.event;

import cc.ddrpa.playground.vikare.juel.CustomCalendarService;
import lombok.RequiredArgsConstructor;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngines;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.event.impl.FlowableActivityEventImpl;
import org.flowable.engine.delegate.event.impl.FlowableEntityEventImpl;
import org.flowable.engine.delegate.event.impl.FlowableEntityWithVariablesEventImpl;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.service.impl.persistence.entity.TaskEntityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

import static org.flowable.common.engine.api.delegate.event.FlowableEngineEventType.ACTIVITY_COMPLETED;
import static org.flowable.common.engine.api.delegate.event.FlowableEngineEventType.TASK_COMPLETED;

@Component
@RequiredArgsConstructor
public class UserTaskCompletedEventListener implements FlowableEventListener {
    private static final Logger logger = LoggerFactory.getLogger(UserTaskCompletedEventListener.class);

    private RuntimeService runtimeService;
    private HistoryService historyService;
    // 模拟用于持久化数据的服务
    private final CustomCalendarService customCalendarService;

    @Override
    public void onEvent(FlowableEvent event) {
        if (Objects.isNull(customCalendarService)) {
            logger.error("CustomCalendarService is not initialized");
        }
        if (Objects.isNull(event)) {
            return;
        }
        // 可以看到许多事件都会触发 ACTIVITY_COMPLETED，包括 StartEvent 和 EndEvent
        if (event.getType().equals(ACTIVITY_COMPLETED)) {
            logger.info("Activity completed event, activityId: {}, activityType: {}", ((FlowableActivityEventImpl) event).getActivityId(), ((FlowableActivityEventImpl) event).getActivityType());
            return;
        } else if (!event.getType().equals(TASK_COMPLETED)) {
            // ignore other type event
            return;
        }
        // 获取 BPMN 中设置的任务名称
        logger.info("Task completed event: {}", ((TaskEntityImpl) ((FlowableEntityEventImpl) event).getEntity()).getName());

        // 如果完成任务时有提交表单，则 event 是 FlowableEntityWithVariablesEventImpl 的实例，
        // 否则是 FlowableEntityEventImpl 的实例
        if (event instanceof FlowableEntityWithVariablesEventImpl entityWithVariablesEvent) {
            Map variables = entityWithVariablesEvent.getVariables();
            logger.info("User task completed with variables: {}", variables);
        }
        // 此外 event 一定可以转换为 FlowableEntityEventImpl 实例
        FlowableEntityEventImpl entityEvent = (FlowableEntityEventImpl) event;
        var taskEntity = (TaskEntityImpl) entityEvent.getEntity();
        // 无法获得任务的 candidateGroups，会抛出 java.lang.NullPointerException 异常
//            var candidateGroups = taskEntity.getCandidates();
//            logger.info("User task completed with candidateGroups: {}", candidateGroups);

        // TODO 需要一种更好的注入方式
        if (Objects.isNull(historyService)) {
            ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
            historyService = processEngine.getHistoryService();
        }
        if (Objects.isNull(runtimeService)) {
            ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
            runtimeService = processEngine.getRuntimeService();
        }

        // 判断是谁最终完成了任务，需要查询历史任务
        var historyTaskInstance = historyService.createHistoricTaskInstanceQuery()
                .taskId(taskEntity.getId())
                .singleResult();
        if (Objects.isNull(historyTaskInstance)) {
            logger.warn("Can not find history task instance with taskId: {}", taskEntity.getId());
            return;
        }
        String completedUserId = historyTaskInstance.getCompletedBy();
        logger.info("User task completed by: {}", completedUserId);
        // 通过 processInstanceId 查询流程实例
        String processInstanceId = entityEvent.getProcessInstanceId();
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).singleResult();
        logger.info("Query runtime service with processInstanceId {}: {}", processInstanceId, processInstance);
    }

    @Override
    public boolean isFailOnException() {
        // 当监听器方法抛出异常时，不要让对应的任务失败
        return false;
    }

    @Override
    public boolean isFireOnTransactionLifecycleEvent() {
        return true;
    }

    @Override
    public String getOnTransaction() {
        return "COMMITTED";
    }
}