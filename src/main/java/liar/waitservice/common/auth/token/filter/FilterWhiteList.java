package liar.waitservice.common.auth.token.filter;

import java.util.Arrays;
import java.util.List;

public class FilterWhiteList {

    public static final List<String> AuthenticationGivenFilterWhitelist = Arrays.asList(
            "/",
            "/static/**",
            "/favicon.ico",
            "/wait-service/wait-websocket/**"
    );
}
