package project.coca;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import project.coca.auth.jwt.JwtProperties;

@SpringBootApplication()
@EnableConfigurationProperties(JwtProperties.class)
public class CocaApplication {
    public static void main(String[] args) {
        SpringApplication.run(CocaApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("OPTIONS", "GET", "POST", "PUT", "DELETE");
            }
        };
    }
}
