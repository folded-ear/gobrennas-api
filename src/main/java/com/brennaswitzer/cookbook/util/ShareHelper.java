package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.config.AppProperties;
import com.brennaswitzer.cookbook.domain.Identified;
import io.jsonwebtoken.lang.Assert;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShareHelper {

    @Autowired
    private AppProperties appProperties;

    public String getSecret(Identified it) {
        return getSecret(it.getClass(), it.getId());
    }
    
    public String getSecret(Class<?> clazz, Long id) {
        Assert.notNull(clazz, "Cannot generate a secret for the null class");
        Assert.notNull(id, "Cannot generate a secret for the null id");
        return new HmacUtils(
                HmacAlgorithms.HMAC_SHA_1,
                appProperties.getAuth().getTokenSecret().getBytes()
        ).hmacHex(
                (clazz.getName() + '#' + id).getBytes()
        );
    }

    public boolean isSecretValid(Identified it, String secret) {
        return isSecretValid(it.getClass(), it.getId(), secret);
    }

    public boolean isSecretValid(Class<?> clazz, Long id, String secret) {
        Assert.notNull(clazz, "Cannot validate a secret for the null class");
        Assert.notNull(id, "Cannot validate a secret for the null id");
        Assert.notNull(secret, "Cannot validate the null secret");
        return secret.equals(getSecret(clazz, id));
    }

}
