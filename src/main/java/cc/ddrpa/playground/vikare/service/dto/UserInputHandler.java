package cc.ddrpa.playground.vikare.service.dto;

public record UserInputHandler(
        String handlerClass,
        String method,
        String bucket
) {
}
