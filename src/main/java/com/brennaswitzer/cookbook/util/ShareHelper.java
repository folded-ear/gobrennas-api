package com.brennaswitzer.cookbook.util;

import com.brennaswitzer.cookbook.config.AppProperties;
import com.brennaswitzer.cookbook.domain.Identified;
import com.brennaswitzer.cookbook.domain.Named;
import com.brennaswitzer.cookbook.payload.ShareInfo;
import com.google.common.annotations.VisibleForTesting;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.SecurityContext;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class ShareHelper {

    private ImmutableSecret<SecurityContext> secret;

    @Autowired
    public void setAppProperties(AppProperties appProperties) {
        this.secret = new ImmutableSecret<>(
                appProperties.getAuth()
                        .getTokenSecret()
                        .getBytes());
    }

    public <T extends Identified> ShareInfo getInfo(Class<T> clazz, T object) {
        return new ShareInfo(object.getId(),
                             SlugUtils.toSlug(object instanceof Named named
                                                      ? named.getName()
                                                      : clazz.getSimpleName(), 40),
                             getHmacSecret(clazz, object.getId()));
    }

    @VisibleForTesting
    String getHmacSecret(Class<?> clazz, Long id) {
        Assert.notNull(clazz, "Cannot generate a secret for the null class");
        Assert.notNull(id, "Cannot generate a secret for the null id");
        return new HmacUtils(HmacAlgorithms.HMAC_SHA_1,
                             secret.getSecret())
                .hmacHex((clazz.getName() + '#' + id).getBytes());
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isSecretValid(Class<?> clazz, Long id, String secret) {
        Assert.notNull(clazz, "Cannot validate a secret for the null class");
        Assert.notNull(id, "Cannot validate a secret for the null id");
        Assert.notNull(secret, "Cannot validate the null secret");
        return secret.equals(getHmacSecret(clazz, id));
    }

}
