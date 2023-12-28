package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.config.AppProperties;
import com.brennaswitzer.cookbook.domain.Identified;
import com.brennaswitzer.cookbook.domain.Named;
import com.brennaswitzer.cookbook.payload.ShareInfo;
import io.jsonwebtoken.lang.Assert;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ShareHelper {

    @Autowired
    private AppProperties appProperties;

    public <T extends Identified & Named> ShareInfo getInfo(Class<T> clazz, T object) {
        return new ShareInfo(object.getId(),
                             SlugUtils.toSlug(object.getName()),
                             getSecret(clazz,
                                       object.getId()));
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isSecretValid(Class<?> clazz, Long id, String secret) {
        Assert.notNull(clazz, "Cannot validate a secret for the null class");
        Assert.notNull(id, "Cannot validate a secret for the null id");
        Assert.notNull(secret, "Cannot validate the null secret");
        return secret.equals(getSecret(clazz, id));
    }

}
