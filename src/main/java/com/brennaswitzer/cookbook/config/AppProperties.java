package com.brennaswitzer.cookbook.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {

    private String publicUrl;
    private long maxProxySize = 1024 * 1024 * 2;

    /**
     * Items untouched in the trash for this many days will be hard-deleted.
     */
    private int daysInTrashBin = 30;

    private final Auth auth = new Auth();
    private final OAuth2 oauth2 = new OAuth2();
    private final AWSProperties aws = new AWSProperties();

    @Getter
    @Setter
    public static class Auth {
        private String tokenSecret;
        private long tokenExpirationMsec;
    }

    @Getter
    @Setter
    public static final class OAuth2 {
        private List<String> authorizedRedirectUris = new ArrayList<>();
    }

}
