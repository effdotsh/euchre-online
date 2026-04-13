package org.example;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class Json {
    private Json() {
    }

    public static String stringify(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String string) {
            return "\"" + escape(string) + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if (value instanceof Map<?, ?> map) {
            StringBuilder builder = new StringBuilder("{");
            Iterator<? extends Map.Entry<?, ?>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<?, ?> entry = iterator.next();
                builder.append(stringify(String.valueOf(entry.getKey())));
                builder.append(":");
                builder.append(stringify(entry.getValue()));
                if (iterator.hasNext()) {
                    builder.append(",");
                }
            }
            builder.append("}");
            return builder.toString();
        }
        if (value instanceof List<?> list) {
            StringBuilder builder = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                builder.append(stringify(list.get(i)));
                if (i < list.size() - 1) {
                    builder.append(",");
                }
            }
            builder.append("]");
            return builder.toString();
        }
        throw new IllegalArgumentException("Unsupported JSON value: " + value.getClass().getName());
    }

    private static String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
