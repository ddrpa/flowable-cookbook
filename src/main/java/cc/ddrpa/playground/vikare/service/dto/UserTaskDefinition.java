package cc.ddrpa.playground.vikare.service.dto;

import java.util.List;

public record UserTaskDefinition(
        List<FlowDirection> flowDirection,
        List<UserInputHandler> handler,
        List<FormField> formControl
) {
}