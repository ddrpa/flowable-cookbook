package cc.ddrpa.playground.vikare.service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.stream.Stream;

public enum FormFieldType {
    CASCADE_AREA_SELECTOR("cascade-area-selector"),
    TEXTAREA("textarea"),
    FILE_UPLOAD("file_upload");

    private final String value;

    FormFieldType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static FormFieldType of(String value) {
        return Stream.of(FormFieldType.values())
                .filter(p -> p.getValue().equals(value))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
