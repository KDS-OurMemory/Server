package com.kds.ourmemory.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {
    private DateUtil() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
        Locale.setDefault(Locale.KOREA);
    }
    
    public static String currentDate() {
        return new SimpleDateFormat("yyyyMMdd").format(new Date(System.currentTimeMillis() + 9 * 1000 * 60 * 60));
    }
    
    public static Date currentTime(){
        
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            
            return format.parse(format.format(new Date(System.currentTimeMillis() + 9 * 1000 * 60 * 60)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
        return new Date();
    }
}
