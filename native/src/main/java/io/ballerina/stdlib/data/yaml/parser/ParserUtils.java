package io.ballerina.stdlib.data.yaml.parser;

import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.stdlib.data.yaml.utils.DiagnosticErrorCode;
import io.ballerina.stdlib.data.yaml.utils.DiagnosticLog;

import java.util.HashMap;
import java.util.Map;

public class ParserUtils {

    public static final String FIELD = "$field$.";
    public static final String FIELD_REGEX = "\\$field\\$\\.";
    public static final String NAME = "Name";
    public static final BString VALUE = StringUtils.fromString("value");

    public static Map<String, Field> getAllFieldsInRecord(RecordType recordType) {
        BMap<BString, Object> annotations = recordType.getAnnotations();
        Map<String, String> modifiedNames = new HashMap<>();
        for (BString annotationKey : annotations.getKeys()) {
            String keyStr = annotationKey.getValue();
            if (!keyStr.contains(FIELD)) {
                continue;
            }
            String fieldName = keyStr.split(FIELD_REGEX)[1];
            Map<BString, Object> fieldAnnotation = (Map<BString, Object>) annotations.get(annotationKey);
            modifiedNames.put(fieldName, getModifiedName(fieldAnnotation, fieldName));
        }

        Map<String, Field> fields = new HashMap<>();
        Map<String, Field> recordFields = recordType.getFields();
        for (String key : recordFields.keySet()) {
            String fieldName = modifiedNames.getOrDefault(key, key);
            if (fields.containsKey(fieldName)) {
                throw DiagnosticLog.error(DiagnosticErrorCode.DUPLICATE_FIELD, fieldName);
            }
            fields.put(fieldName, recordFields.get(key));
        }
        return fields;
    }

    public static String getModifiedName(Map<BString, Object> fieldAnnotation, String fieldName) {
        for (BString key : fieldAnnotation.keySet()) {
            if (key.getValue().endsWith(NAME)) {
                return ((Map<BString, Object>) fieldAnnotation.get(key)).get(VALUE).toString();
            }
        }
        return fieldName;
    }

    public enum ParserOption {
        DEFAULT,
        EXPECT_MAP_KEY,
        EXPECT_MAP_VALUE,
        EXPECT_SEQUENCE_ENTRY,
        EXPECT_SEQUENCE_VALUE
    }

    public enum DocumentType {
        ANY_DOCUMENT,
        BARE_DOCUMENT,
        DIRECTIVE_DOCUMENT
    }
}
