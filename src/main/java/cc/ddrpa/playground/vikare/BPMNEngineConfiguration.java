package cc.ddrpa.playground.vikare;

import cc.ddrpa.playground.vikare.event.UserTaskCompletedEventListener;
import cc.ddrpa.playground.vikare.juel.CalendarDueDateInWorkdaysFunctionDelegate;
import cc.ddrpa.playground.vikare.juel.CustomCalendarService;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class BPMNEngineConfiguration {
    @Bean
    public UserTaskCompletedEventListener userTaskCompletedEventListener(CustomCalendarService customCalendarService) {
        return new UserTaskCompletedEventListener(customCalendarService);
    }

    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> processEngineConfigurationConfigurer(UserTaskCompletedEventListener userTaskCompletedEventListener) {
        return configuration -> {
            // enable safe BPMN XML
            configuration.setEnableSafeBpmnXml(true);
            // register custom function delegate
            if (configuration.getCustomFlowableFunctionDelegates() == null) {
                configuration.setCustomFlowableFunctionDelegates(List.of(new CalendarDueDateInWorkdaysFunctionDelegate()));
            } else {
                configuration.getCustomFlowableFunctionDelegates().add(new CalendarDueDateInWorkdaysFunctionDelegate());
            }
            // 注册 cc.ddrpa.playground.vikare.event.UserTaskUpdateEventListener 监听 TASK_COMPLETED 和 ACTIVITY_COMPLETED 事件
            // 注册同一个监听到不同的事件时，可以使用逗号分隔
            // TASK_COMPLETED 可以由 UserTask 触发，似乎不会由 ServiceTask 触发
            if (configuration.getTypedEventListeners() == null) {
                configuration.setTypedEventListeners(Map.of("TASK_COMPLETED,ACTIVITY_COMPLETED", List.of(userTaskCompletedEventListener)));
            } else if (configuration.getTypedEventListeners().containsKey("TASK_COMPLETED,ACTIVITY_COMPLETED")) {
                configuration.getTypedEventListeners().get("TASK_COMPLETED,ACTIVITY_COMPLETED").add(userTaskCompletedEventListener);
            } else {
                configuration.getTypedEventListeners().put("TASK_COMPLETED,ACTIVITY_COMPLETED", List.of(userTaskCompletedEventListener));
            }
        };
    }
}