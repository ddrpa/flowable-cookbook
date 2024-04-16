package cc.ddrpa.playground.vikare.service.dto;

import com.fasterxml.jackson.databind.JsonNode;

public record UserTaskSubmission(
        String flowDirection,
        JsonNode userFormData
) {
}
