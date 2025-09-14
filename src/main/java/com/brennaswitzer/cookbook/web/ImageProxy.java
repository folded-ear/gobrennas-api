package com.brennaswitzer.cookbook.web;

import com.brennaswitzer.cookbook.config.AppProperties;
import com.brennaswitzer.cookbook.exceptions.BadRequestException;
import com.brennaswitzer.cookbook.util.UserPrincipalAccess;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * I allow logged-in users to proxy reasonably-sized images from arbitrary
 * origins, while "borrowing" BFS's liberal CORS policy. The maximum size can be
 * configured via {@link AppProperties#getMaxProxySize()}.
 */
@Controller
@PreAuthorize("hasRole('USER')")
@Slf4j
public class ImageProxy {

    private static final String BFS_HEADER = "x-bfs-url";

    private static final List<String> DO_NOT_PASS_HEADER_NAMES = List.of(
            HttpHeaders.ACCEPT,
            HttpHeaders.AUTHORIZATION,
            BFS_HEADER,
            HttpHeaders.CONNECTION,
            HttpHeaders.HOST,
            HttpHeaders.IF_RANGE,
            HttpHeaders.RANGE);

    private static final String TYPE_IMAGE = "image";

    @Autowired
    private AppProperties appProperties;

    @Autowired
    private RestClient restClient;

    @Autowired
    private UserPrincipalAccess principalAccess;

    /**
     * Comically primitive rate limiting: one request per minute, for all users,
     * with bursts of up to three.
     */
    private final Bucket throttle = Bucket.builder()
            .addLimit(limit -> limit
                    .capacity(3)
                    .refillGreedy(1, Duration.ofMinutes(1)))
            .build();

    @GetMapping("api/_image_proxy")
    public void proxy(HttpServletRequest request,
                      @RequestHeader(BFS_HEADER) String url,
                      HttpServletResponse response) {
        log.info("tokens:{} userId:{} imageUrl:{}",
                 throttle.getAvailableTokens(),
                 principalAccess.getId(),
                 url);
        if (throttle.tryConsume(1)) {
            Map<String, String> requestHeaders = buildRequestHeaders(request);
            verifyTarget(url, requestHeaders);
            respondWithTarget(url, requestHeaders, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        }
    }

    private void respondWithTarget(String url,
                                   Map<String, String> requestHeaders,
                                   HttpServletResponse response) {
        restClient.get()
                .uri(url)
                .headers(hs -> hs.setAll(requestHeaders))
                .exchange((req, resp) -> {
                    if (!resp.getStatusCode().equals(HttpStatus.OK)) {
                        throw new BadRequestException(String.format(
                                "Received %s from upstream",
                                resp.getStatusCode()));
                    }
//                    Set<String> existingHeaders = new HashSet<>(response.getHeaderNames());
//                    resp.getHeaders().forEach((n, vs) -> {
//                        if (existingHeaders.contains(n)) return;
//                        if (StringUtils.startsWithIgnoreCase(n, "access-control-")) return;
//                        for (var v : vs) {
//                            response.addHeader(n, v);
//                        }
//                    });
                    long bytes;
                    try (var in = resp.getBody()) {
                        bytes = in.transferTo(response.getOutputStream());
                    }
                    log.debug("bytesReturned:{}", bytes);
                    return bytes;
                });
    }

    private void verifyTarget(String url,
                              Map<String, String> requestHeaders) {
        HttpHeaders headers = restClient.head()
                .uri(url)
                .headers(hs -> hs.setAll(requestHeaders))
                .retrieve()
                .toBodilessEntity()
                .getHeaders();
        MediaType type = headers.getContentType();
        long length = headers.getContentLength();
        log.debug("contentType:{} contentLength:{}", type, length);
        if (type == null || !TYPE_IMAGE.equalsIgnoreCase(type.getType())) {
            throw new BadRequestException("Only image types may be proxied");
        }
        if (length > appProperties.getMaxProxySize()) {
            throw new BadRequestException("Image is too large to proxy");
        }
    }

    @NotNull
    private Map<String, String> buildRequestHeaders(HttpServletRequest request) {
        Map<String, String> passthrough = new HashMap<>();
        request.getHeaderNames()
                .asIterator()
                .forEachRemaining(n -> {
                    for (var dnp : DO_NOT_PASS_HEADER_NAMES) {
                        if (dnp.equalsIgnoreCase(n)) return;
                    }
                    if (StringUtils.startsWithIgnoreCase(n, "sec-")) return;
                    passthrough.put(n, request.getHeader(n));
                });
        passthrough.put(HttpHeaders.ACCEPT, TYPE_IMAGE + "/*");
        passthrough.put(BFS_HEADER, appProperties.getPublicUrl());
        return passthrough;
    }

}
