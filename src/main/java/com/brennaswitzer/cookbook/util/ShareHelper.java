package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.config.AppProperties;
import io.jsonwebtoken.lang.Assert;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShareHelper {

    @Autowired
    private AppProperties appProperties;

    public String getSecretForId(Long id) {
        Assert.notNull(id, "Cannot generate a secret for the null ID");
        return new HmacUtils(
                HmacAlgorithms.HMAC_SHA_1,
                appProperties.getAuth().getTokenSecret().getBytes()
        ).hmacHex(
                id.toString().getBytes()
        );
    }

}
