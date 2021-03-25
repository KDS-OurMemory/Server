package com.kds.ourmemory.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class DateUtilTest {
    
    @Test
    void 시간_계산() {
        log.info(DateUtil.currentTime().toString());
    }
    
    @Test
    void 날짜_계산() {
        String currentDate = DateUtil.currentDate();
        assertThat(currentDate).isEqualTo("20210323");
        
        log.info(currentDate);
    }

}
