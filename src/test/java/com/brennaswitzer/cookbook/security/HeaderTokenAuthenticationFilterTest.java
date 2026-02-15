package com.brennaswitzer.cookbook.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpHeaders;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HeaderTokenAuthenticationFilterTest {

    @ParameterizedTest
    @MethodSource
    void caseInsensitive(String authorization, String token) {
        HeaderTokenAuthenticationFilter filter = new HeaderTokenAuthenticationFilter();
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getHeader(HttpHeaders.AUTHORIZATION))
                .thenReturn(authorization);

        assertEquals(token,
                     filter.getJwtFromRequest(req));
    }

    public static Stream<Arguments> caseInsensitive() {
        return Stream.of(
                Arguments.of("",
                             null),
                Arguments.of("soManyBears",
                             null),
                Arguments.of("Bear cow",
                             null),
                Arguments.of("Bearers horse donkey",
                             null),
                Arguments.of("BEARER goat",
                             "goat"),
                Arguments.of("bearer goat",
                             "goat"),
                Arguments.of("Bearer goat",
                             "goat"),
                Arguments.of("bEARer rabbit",
                             "rabbit"),
                Arguments.of("Bearer   sebastian  ",
                             "sebastian"));
    }

}
