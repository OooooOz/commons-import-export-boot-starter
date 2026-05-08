package org.commons.infrastructure.util;

import org.apache.commons.lang3.StringUtils;

public class CommonUtil {

    private CommonUtil() {
    }

    public static Integer checkNumberDefault(Integer value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public static String checkEmptyDefault(String value, String defaultValue) {
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        return value;
    }

    public static String checkEmpty(String value) {
        return checkEmptyDefault(value, StringUtils.EMPTY);
    }

}
