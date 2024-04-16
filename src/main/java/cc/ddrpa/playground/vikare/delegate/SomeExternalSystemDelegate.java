package cc.ddrpa.playground.vikare.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SomeExternalSystemDelegate implements JavaDelegate {
    private static final Logger logger = LoggerFactory.getLogger(SomeExternalSystemDelegate.class);

    @Override
    public void execute(DelegateExecution execution) {
        // 在这里获取到流程表单等信息并发送到外部系统
        logger.info("Calling the external system for execution {}",
                execution.getId());
                execution.getVariable("employee");
        // TODO如果在发送过程中出现异常要如何处理？
    }
}