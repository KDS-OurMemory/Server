package com.kds.ourmemory.v1.controller.memory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kds.ourmemory.v1.controller.memory.dto.MemoryRspDto;
import com.kds.ourmemory.v1.entity.memory.Memory;
import com.kds.ourmemory.v1.entity.user.DeviceOs;
import com.kds.ourmemory.v1.entity.user.User;
import com.kds.ourmemory.v1.entity.user.UserRole;
import com.kds.ourmemory.v1.service.memory.MemoryService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class MemoryControllerTest {

    // LocalDateTime 의 경우, 오브젝트매퍼로 바로 역직렬화되지 않는다.
    // 실제 API 통신에는 해당 값을 문자열로 변경하여 리턴하기 때문에 인터셉터에서 로그 기록 시, 문제가 되지 않는다.
    // -> 따라서 테스트용 코드에서만 사용할 수 있도록 아래와 같이 임시조치로 해결.
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final DateTimeFormatter alertTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM");

    @Mock
    MemoryService memoryService;

    @InjectMocks
    private MemoryController memoryController;

    @Test
    @DisplayName("일정 목록 조회 요청-응답 | 성공")
    void findMemoriesSuccess() throws JsonProcessingException {
        // given
        var writer = User.builder()
                .id(1L)
                .snsType(1)
                .snsId("snsId")
                .pushToken("pushToken")
                .push(false)
                .name("name")
                .birthday("0101")
                .solar(true)
                .birthdayOpen(true)
                .role(UserRole.ADMIN)
                .deviceOs(DeviceOs.IOS)
                .build();
        writer.updatePrivateRoomId(1L);

        var memory1 = Memory.builder()
                .id(1L)
                .writer(writer)
                .name("memory1")
                .contents("memory1 contents")
                .place("memory1 Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(7).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(7).plusHours(1).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(6).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var memory2 = Memory.builder()
                .id(2L)
                .writer(writer)
                .name("memory2")
                .contents("memory2 contents")
                .place("memory2 Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(6).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(8).plusHours(1).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(5).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var memory3 = Memory.builder()
                .id(3L)
                .writer(writer)
                .name("memory3")
                .contents("memory3 contents")
                .place("memory3 Place")
                .startDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(4).format(alertTimeFormat), alertTimeFormat)
                ) // 시작시간
                .endDate(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(5).plusHours(1).format(alertTimeFormat), alertTimeFormat)
                ) // 종료시간
                .firstAlarm(LocalDateTime.parse(
                        LocalDateTime.now().plusDays(1).format(alertTimeFormat), alertTimeFormat)
                ) // 첫 번째 알림
                .bgColor("#FFFFFF")
                .build();

        var memories = Stream.of(memory1, memory2, memory3).map(MemoryRspDto::new)
                .collect(Collectors.toList());

        var startMonth = YearMonth.parse(
                YearMonth.now().minusMonths(1).format(dateFormat), dateFormat);
        var endMonth = YearMonth.parse(
                YearMonth.now().plusMonths(1).format(dateFormat), dateFormat);

        // when
        when(memoryService.findMemories(writer.getId(), null, startMonth, endMonth)).thenReturn(memories);

        // then
        var responseDto = memoryController
                .findMemories(writer.getId(), null, startMonth, endMonth);

        assertThat(responseDto.getResultCode()).isEqualTo("S001");
        assertThat(responseDto.getResponse().size()).isEqualTo(3);

        // check response data
        log.debug(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseDto));
    }

}
