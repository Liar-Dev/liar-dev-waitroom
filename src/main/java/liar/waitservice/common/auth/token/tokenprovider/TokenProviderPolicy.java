package liar.waitservice.common.auth.token.tokenprovider;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.Authentication;

public interface TokenProviderPolicy {

    String TOKEN_TYPE = "Bearer ";

    Claims getClaims(String token);

    String getUserIdFromToken(String token);

    long getRemainingTimeFromToken(String token);

    boolean isMoreThanReissueTime(String token);

    Authentication getAuthentication(String token);

    boolean validateToken(String authToken);

    String removeType(String token);

    Long getRemainTime(String token);

}