package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.config.AppProperties;
import com.brennaswitzer.cookbook.domain.Identified;
import com.brennaswitzer.cookbook.domain.Named;
import com.brennaswitzer.cookbook.domain.Recipe;
import com.brennaswitzer.cookbook.payload.ShareInfo;
import lombok.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShareHelperTest {

    @InjectMocks
    @Spy
    private ShareHelper helper;

    @Mock
    private AppProperties appProperties;

    @Mock
    private AppProperties.Auth auth;

    @BeforeEach
    void setUp() {
        when(appProperties.getAuth()).thenReturn(auth);
        when(auth.getTokenSecret()).thenReturn("some-random-key");
        helper.setAppProperties(appProperties);
    }

    @Value
    private static class Shareable implements Identified, Named {

        Long id;
        String name;

    }

    @Test
    void getInfo() {
        long id = 123L;
        String anonSecret = "329bb9790fd6c157e68cb4187cf52b11b6e9f4ba";

        ShareInfo info = helper.getInfo(Shareable.class,
                                        new Shareable(id, "A Thinger!"));

        assertEquals(id, info.getId());
        assertEquals("a-thinger", info.getSlug());
        assertTrue(helper.isSecretValid(Shareable.class, id, info.getSecret()));
        assertTrue(helper.isSecretValid(Shareable.class, id, anonSecret));
    }

    @Test
    void getHmacSecret() {
        long id = 877123L;
        String anonSecret = helper.getHmacSecret(Recipe.class, id);

        assertEquals("6922b21b1364896cdfbd6ad930a50975c15a198a", anonSecret);
        assertTrue(helper.isSecretValid(Recipe.class, id, anonSecret));
    }

}
