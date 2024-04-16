package cc.ddrpa.playground.vikare.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class TaskAssignmentDecisionTableDelegate implements JavaDelegate {
    public static final String SINGLE_USER_1 = "tom";
    public static final String SINGLE_USER_2 = "jerry";
    public static final String LIST_OF_USERS = String.join(",", List.of(SINGLE_USER_1, SINGLE_USER_2));
    public static final String CANDIDATE_USER_GROUPS = "cartoon-department";

    public enum Action {
        ASSIGNEE_TO_SINGLE_USER,
        ASSIGNEE_TO_MULTI_USERS,
        ASSIGNEE_TO_GROUP;
    }

    @Override
    public void execute(DelegateExecution execution) {
        execution.setVariables(Map.of("sAssignee", "", "sCandidateUsers", "", "sCandidateGroups", ""));
        var action = (TaskAssignmentDecisionTableDelegate.Action) execution.getVariable("action");
        switch (action) {
            case ASSIGNEE_TO_SINGLE_USER:
                if (new Random().nextBoolean()) {
                    execution.setVariable("sAssignee", SINGLE_USER_1);
                } else {
                    execution.setVariable("sAssignee", SINGLE_USER_2);
                }
                break;
            case ASSIGNEE_TO_MULTI_USERS:
                execution.setVariable("sCandidateUsers", LIST_OF_USERS);
                break;
            case ASSIGNEE_TO_GROUP:
                execution.setVariable("sCandidateGroups", CANDIDATE_USER_GROUPS);
                break;
            default:
                throw new IllegalArgumentException("Invalid action: " + execution.getVariable("action"));
        }
    }
}