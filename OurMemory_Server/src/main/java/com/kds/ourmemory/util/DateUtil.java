package com.kds.ourmemory.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {
    private DateUtil() {
    }
    
    public static String currentDate() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        return new SimpleDateFormat("yyyyMMdd").format(new Date(System.currentTimeMillis() + 9 * 1000 * 60 * 60));
    }
    
    public static Date currentTime(){
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            
            return format.parse(format.format(new Date(System.currentTimeMillis() + 9 * 1000 * 60 * 60)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        return new Date();
    }
}
