package com.kds.ourmemory.v1.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kds.ourmemory.v1.controller.ApiResult;
import com.kds.ourmemory.v1.controller.user.dto.UserReqDto;
import com.kds.ourmemory.v1.entity.user.DeviceOs;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 인터셉터를 통해 로깅이 정상적으로 이루어지는지 테스트하기 위한 클래스
 *
 * 각 요청의 상세 로직은 서비스테스트코드를 통해 검증되기 때문에
 * 성공 / 실패 / 특이 케이스 의 사례 하나씩만 작성함.
 *
 * 참고) 특이케이스
 * 1. 멀티파트 파일 요청
 *   예 - 프로필이미지 업로드
 *    멀티파트 파일의 경우, 로깅을 위해 스트림을 미리 읽어 멀티파트핸들러 작업 시 오류가 발생한다.
 *    따라서 멀티파트 파일을 포함한 요청은 데이터를 로깅하지 않으며, 이러한 특이사항을 테스트하기 위해 케이스를 추가했다.
 *    => 22.03.05 MultipartFile 의 경우
 *    TestRestTemplate 의 기본 MessageConverters 가 MultipartFile 에 포함된 InputStream 을 직렬화하지 못해 예외가 발생한다.
 *    이를 해결하기 위해선 MultipartFile 대신 MultiValueMap 에 file 데이터를 넣어 전달하는 방식으로 수정이 필요하다.
 *    이 경우, 파일업로드 로직 및 관련 서비스 코드와 테스트 코드 모두 수정해야 하기 때문에 범위가 넓어 보류함.
 */
@ActiveProfiles("test")
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HttpLogInterceptorTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 | 성공")
    void signUpSuccess() {
        var userReqDto = UserReqDto.builder()
                .snsType(1)
                .snsId("testId")
                .pushToken("testToken")
                .push(false)
                .name("Test Name!")
                .birthday("0107")
                .solar(true)
                .birthdayOpen(true)
                .deviceOs(DeviceOs.IOS)
                .build();

        var apiResult = restTemplate.postForObject("/v1/users", userReqDto, ApiResult.class);
        assertThat(apiResult.getResultCode()).isEqualTo("S001"); // 성공
        log.debug(prettyPrint(apiResult));
    }

    @Test
    @DisplayName("내 정보조회 요청 테스트 | 실패 | 사용자를 찾을 수 없음.")
    void findRequestFailToNotFoundUser() {
        var notFoundUserId = -1;
        var url = "/v1/users/" + notFoundUserId;
        var apiResult = restTemplate.getForObject(url, ApiResult.class);
        assertThat(apiResult.getResultCode()).isEqualTo("U002"); // 사용자를 찾을 수 없습니다.
        log.debug(prettyPrint(apiResult));
    }

    private String prettyPrint(Object object) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("prettyPrint Error!", e);
            return "WriteValueAsString Failed.";
        }
    }

}
