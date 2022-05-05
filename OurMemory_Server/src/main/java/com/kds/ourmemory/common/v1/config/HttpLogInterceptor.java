package com.kds.ourmemory.common.v1.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

@Slf4j
@Component
@RequiredArgsConstructor
public class HttpLogInterceptor implements HandlerInterceptor {

    private final ObjectMapper mapper;

    @Override
    public boolean preHandle(
            HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler
    ) throws Exception {
        log.info("Request URL - [{}]{}", request.getMethod(), request.getRequestURL());
        log.info("Request RemoteAddress {}:{}", request.getRemoteAddr(), request.getRemotePort());
        log.info("Request Header - {}", getHeaderValues(request));
        log.info("Request QueryString - {}", queryStringToPrettyString(request.getQueryString()));

        if (request instanceof CachingRequestWrapper) {
            if (!MediaType.MULTIPART_FORM_DATA_VALUE.equals(request.getContentType())) {
                var req = IOUtils.toString(request.getInputStream(), request.getCharacterEncoding());
                log.info("Request Body - {}", bodyToPrettyJson(req));
            } else {
                log.info("Request Body - Don't read  MultipartHttpServletRequest body.");
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(
            @NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler,
            @Nullable Exception ex
    ) throws Exception {
        log.info("Response Header - {}", getHeaderValues(response));

        // 리스폰스를 다른 래퍼로 필터링하는 경우를 대비해 래퍼 검증 후 로깅함.
        if (response instanceof ContentCachingResponseWrapper wrapperResponse) {
            if (!MediaType.MULTIPART_FORM_DATA_VALUE.equals(wrapperResponse.getContentType())) {
                var rsp = IOUtils.toString(
                        wrapperResponse.getContentInputStream(), wrapperResponse.getCharacterEncoding()
                );

                log.info("Response Body - {}", bodyToPrettyJson(rsp));
            } else {
                log.info("Response Body - Don't read  MultipartHttpServletResponse body.");
            }
        }
    }

    private String bodyToPrettyJson(String body) {
        try {
            if (StringUtils.isBlank(body)) {
                return "Not provided";
            }

            var jsonObject = mapper.readValue(body, Object.class);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
        } catch (JsonProcessingException e) {
            log.error("String to json failed. original data return");
            return body;
        }
    }

    private String queryStringToPrettyString(String queryString) {
        if (StringUtils.isBlank(queryString)) {
            return "Not provided";
        }

        return StringUtils.join(queryString.split("&"), ", ");
    }

    private String getHeaderValues(HttpServletRequest request) {
        var headerNames = request.getHeaderNames();

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        headerNames.asIterator().forEachRemaining(headerName ->
                sb.append("\n\t").append(headerName).append(": ")
                        .append(getHeaderMultiValue(request.getHeaders(headerName)))
        );
        sb.append("\n}");

        return sb.toString();
    }

    private String getHeaderMultiValue(Enumeration<String> enums) {
        var values = new ArrayList<String>();
        enums.asIterator().forEachRemaining(values::add);

        return StringUtils.join(values, ", ");
    }

    private String getHeaderValues(HttpServletResponse response) {
        var headerNames = response.getHeaderNames();

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        headerNames.stream().distinct().forEach(headerName ->
                sb.append("\n\t").append(headerName).append(": ")
                        .append(getHeaderMultiValue(response.getHeaders(headerName)))
        );
        sb.append("\n}");

        return sb.toString();
    }

    private String getHeaderMultiValue(Collection<String> enums) {
        return StringUtils.join(new ArrayList<>(enums), ", ");
    }

}
