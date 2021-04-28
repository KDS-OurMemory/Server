package com.kds.ourmemory.controller.v1.memory;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.memory.dto.FindMemorysDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class MemoryControllerTest {

    @Autowired
    private MemoryController memoryController;
    
    private ObjectMapper mapper = new ObjectMapper();
    
    @Transactional
    @Test
    void 일정_조회() throws JsonProcessingException{
        ApiResult<List<FindMemorysDto.Response>> responseDto = memoryController.findMemorys(99L);
        
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getResultcode()).isEqualTo("00");
        assertThat(responseDto.getResponse()).isNotNull();
        
        log.debug(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseDto));
    }
    
}
