package liar.waitservice.common.auth.token.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import liar.waitservice.common.auth.token.tokenprovider.TokenProviderPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class AuthenticationGiveFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    private final TokenProviderPolicy tokenProvider;
    private final AntPathMatcher antPathMatcher;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        log.info("requestURI = {}", requestURI);

        if (isAuthorizationIssueRequired(requestURI)) {
            String jwt = resolveToken(request.getHeader(AUTHORIZATION_HEADER));
            log.info("jwt from http request = {}", jwt);

            SecurityContextHolder.getContext().setAuthentication(tokenProvider.getAuthentication(jwt));
        }

        log.info("filter chain do filter");
        filterChain.doFilter(request, response);
    }

    private boolean isAuthorizationIssueRequired(String requestURI) {
        return !FilterWhiteList.AuthenticationGivenFilterWhitelist.stream()
                .anyMatch(uri -> antPathMatcher.match(uri, requestURI));
    }

    private String resolveToken(String bearerToken) {

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

}
