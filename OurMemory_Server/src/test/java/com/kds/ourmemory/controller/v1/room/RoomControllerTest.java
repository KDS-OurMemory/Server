package com.kds.ourmemory.controller.v1.room;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kds.ourmemory.controller.v1.ApiResult;
import com.kds.ourmemory.controller.v1.room.dto.RoomRspDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class RoomControllerTest {

    private final RoomController roomController;
    private final ObjectMapper mapper;

    @Autowired
    private RoomControllerTest(RoomController roomController) {
        this.roomController = roomController;
        this.mapper = new ObjectMapper();
    }

    @Transactional
    @Test
    void findRooms() throws JsonProcessingException {
        ApiResult<List<RoomRspDto>> responseDto = roomController.findRooms(99L, null);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getResultCode()).isEqualTo("00");
        assertThat(responseDto.getResponse()).isNotNull();

        log.debug(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseDto));
    }
}
