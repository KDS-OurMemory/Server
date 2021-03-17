package com.kds.ourmemory.util;

import java.text.SimpleDateFormat;

public class DateUtil {
    private DateUtil() {
    }
    
    public static String currentDate() {
        return new SimpleDateFormat("yyyyMMdd").format(System.currentTimeMillis());
    }
}
