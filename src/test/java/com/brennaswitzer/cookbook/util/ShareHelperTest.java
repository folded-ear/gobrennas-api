package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.domain.Identified;
import com.brennaswitzer.cookbook.domain.Named;
import com.brennaswitzer.cookbook.payload.ShareInfo;
import lombok.Value;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

class ShareHelperTest extends MockTest {

    @MockTestTarget
    private ShareHelper helper;

    @Value
    private static class Shareable implements Identified, Named {

        Long id;
        String name;

    }

    @Test
    void getInfo() {
        long id = 123L;
        String secret = "secret";
        doReturn(secret)
                .when(helper)
                .getSecret(Shareable.class, id);

        ShareInfo info = helper.getInfo(Shareable.class,
                                        new Shareable(id, "A Thinger!"));

        assertEquals(id, info.getId());
        assertEquals("a-thinger", info.getSlug());
        assertEquals(secret, info.getSecret());
    }

}
