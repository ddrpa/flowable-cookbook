package cc.ddrpa.playground.vikare.service.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormField {
    @JsonProperty("component")
    private FormFieldType type;
    private String key;
    private String label;
    private Map<String, JsonNode> additionalConfiguration = new HashMap<>();

    @JsonAnySetter
    public void setAdditionalConfiguration(String key, JsonNode value) {
        additionalConfiguration.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, JsonNode> getAdditionalConfiguration() {
        return additionalConfiguration;
    }
}