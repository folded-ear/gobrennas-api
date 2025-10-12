package com.brennaswitzer.cookbook.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app")
@Validated
@Getter
@Setter
public class AppProperties {

    private String publicUrl;
    private long maxProxySize = 1024 * 1024 * 2;

    /**
     * Items untouched in the trash for this many days will be hard-deleted.
     */
    private int daysInTrashBin = 30;

    @Valid
    private final Auth auth = new Auth();
    private final OAuth2 oauth2 = new OAuth2();
    private final AWSProperties aws = new AWSProperties();

    @Getter
    @Setter
    public static class Auth {

        /**
         * Secret for anonymous shared link tokens (not session tokens). Unlike
         * sessions, these tokens live forever, so the secret isn't rotated.
         */
        @Size(min = 15)
        private String tokenSecret;

        /**
         * List of token secrets, in priority order. Tokens signed with any
         * secret can be verified, but all new tokens will be minted with the
         * first.
         */
        @Valid
        private List<Secret> tokenSecrets = List.of();

        private long tokenExpirationMsec;

    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Secret {

        /**
         * ID of the secret (e.g., for a JWT's header's {@code kid} attribute).
         */
        @NotBlank
        private String id;

        /**
         * Base64 encoded secret, which must be at least 512 bits / 64 bytes in
         * length. You can use <kbd>openssl rand -base64 64</kbd> to make one.
         * Beware the base64url encoding, which is used by various online tools,
         * but is different from the base64 used here.
         */
        @Size(min = 88, message = "Must be at least 88 characters, the length of 512 bits / 64 bytes, encoded with base64.")
        private String secret;

    }

    @Getter
    @Setter
    public static final class OAuth2 {
        private List<String> authorizedRedirectUris = new ArrayList<>();
    }

}
