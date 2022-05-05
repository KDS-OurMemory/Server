package com.kds.ourmemory.common.v1.config;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class RequestResponseWrapperFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain
    ) throws ServletException, IOException {
        if(isAsyncDispatch(request)) {
            filterChain.doFilter(request, response);
        } else {
            /**
             * 멀티파트 파일은 리퀘스트 로깅하지 않는 이유
             *
             * 서블릿 리퀘스트의 인풋스트림은 ServletInputStream 으로 reset(), mark() 를 지원하지 않는다.
             * 따라서 한번 읽으면 포지션을 되돌려 다시 읽을 수 없다.
             * 이로 인해 멀티파트 폼 데이터가 파라미터로 전달되는 경우, RequestHandler 가 받아서 처리하는 과정에서
             * StandardMultipartHttpServletRequest 로 변환된다.
             * 그 후 리퀘스트를 파싱하는 parseRequest() 메소드 진행 도중, 리퀘스트의 스트림을 읽게 되고, 이로 인해
             * 아래와 같이 읽을 수 없다는 오류가 발생한다.
             *
             * org.springframework.web.multipart.MultipartException: Failed to parse multipart servlet request;
             * nested exception is java.io.IOException:
             * org.apache.tomcat.util.http.fileupload.FileUploadException: Stream closed
             *
             * 이를 해결하기 위해 멀티파트 리퀘스트의 경우, resolve-lazily true 처리하여 리퀘스트 데이터를 읽지 않도록 한다.
             * -> application.yml 에 옵션을 설정함.(서블릿 리퀘스트 객체를 따로 구현하지 않음.)
             * -> 레이지로드되지 않아 바로 오류 발생함. 설정 삭제
             *
             * 22.02.24
             *  컨텐츠타입으로 멀티파트 데이터 구분하여 필터링 여부 설정하도록 수정
             *  -> StandardServletMultipartResolver.isMultipart() 코드 참고함.
             *
             * 22.03.05
             *  1. 스프링 래퍼클래스 ContentCachingResponseWrapper 로 변경
             *  2. ContentCachingRequestWrapper 는 스트림을 재사용할 수 없는 유틸성 클래스이기 때문에
             *  로깅하게 될 경우, 컨트롤러에서 읽을 수 없어 리퀘스트바디가 없다는 오류가 발생한다.
             *  이를 해결하기 위해 리퀘스트는 재사용가능한 커스텀 래퍼클래스를 사용한다.
             */
            var wrappingResponse = new ContentCachingResponseWrapper(response);
            filterChain.doFilter(
                    StringUtils.startsWithIgnoreCase(request.getContentType(), MediaType.MULTIPART_FORM_DATA_VALUE)
                            ? request : new CachingRequestWrapper(request),
                        wrappingResponse
            );

            // Response 로부터 body 데이터를 래퍼클래스에 복사함. -> 이 작업을 안하면 바디없이 리턴된다.
            wrappingResponse.copyBodyToResponse();
        }
    }

}
