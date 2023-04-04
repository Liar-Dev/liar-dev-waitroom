package liar.waitservice.common.auth.token.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
public class AccessToken extends Token implements Serializable {

    private AccessToken(String id, String userId, long expiration) {
        super(id, userId, expiration);
    }

    public static AccessToken of (String accessToken, String userId, Long expiration) {
        return new AccessToken(accessToken, userId, expiration);
    }
}