package liar.waitservice.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebCorsConfig implements WebMvcConfigurer {
    private static String[] allowedOrigins = {
            "---- secret ----",
            "---- secret ----",
            "---- secret ----",
            "---- secret ----",
            "---- secret ----",
            "---- secret ----"
    };

    private static final String allowMethod = "*";

    private static final String allowHeader = "*";

    private static final String allowAccessControlAllowOrigin = "Access-Control-Allow-Origin";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins)
                .allowedMethods(allowMethod)
                .allowedHeaders(allowHeader)
                .exposedHeaders(allowAccessControlAllowOrigin)
                .allowCredentials(true);
    }
}
