package cc.ddrpa.playground.vikare;

import cc.ddrpa.playground.vikare.juel.CalendarDueDateInWorkdaysFunctionDelegate;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class BPMNEngineConfiguration {
    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> processEngineConfigurationConfigurer() {
        return configuration -> {
            // enable safe BPMN XML
            configuration.setEnableSafeBpmnXml(true);
            // register custom function delegate
            if (configuration.getCustomFlowableFunctionDelegates() == null) {
                configuration.setCustomFlowableFunctionDelegates(List.of(new CalendarDueDateInWorkdaysFunctionDelegate()));
            } else {
                configuration.getCustomFlowableFunctionDelegates().add(new CalendarDueDateInWorkdaysFunctionDelegate());
            }
        };
    }
}