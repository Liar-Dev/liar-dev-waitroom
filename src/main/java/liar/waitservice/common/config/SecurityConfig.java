package liar.waitservice.common.config;

import liar.waitservice.common.auth.token.filter.AuthenticationGiveFilter;
import liar.waitservice.common.auth.token.tokenprovider.TokenProviderPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenProviderPolicy tokenProviderPolicy;
    private final AntPathMatcher antPathMatcher;
    private static final String[] webServiceWhiteList = {
            "/static/**", "/static/js/**", "/static/images/**",
            "/static/css/**", "/static/scss/**", "/static/docs/**",
            "/h2-console/**", "/favicon.ico", "/error"
    };

    private static final String[] socketWhiteList = {
            "/wait-service/wait-websocket",
            "/wait-service/wait-websocket/info",
            "/wait-service/wait-websocket/**"
    };


    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(webServiceWhiteList);
    }

    @Bean
    SecurityFilterChain securityFilter(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers(socketWhiteList)
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                )
                .formLogin().disable()
                .addFilterBefore(authenticationGiveFilter(), UsernamePasswordAuthenticationFilter.class)

                .httpBasic().disable()
                .csrf(AbstractHttpConfigurer::disable)
                .cors().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .headers()
                .xssProtection()
                .and()
                .contentSecurityPolicy("script-src 'self'")
                .and()
                .frameOptions()
                .sameOrigin();

        return http.build();

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationGiveFilter authenticationGiveFilter() {
        return new AuthenticationGiveFilter(tokenProviderPolicy, antPathMatcher);
    }
}

