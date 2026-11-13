package com.hospital.his.config;

import com.hospital.his.interceptor.DisponibilidadInterceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final DisponibilidadInterceptor disponibilidadInterceptor;

    public WebMvcConfig(
            DisponibilidadInterceptor disponibilidadInterceptor) {

        this.disponibilidadInterceptor = disponibilidadInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(disponibilidadInterceptor)

                // Verificar todas las rutas del sistema
                .addPathPatterns("/**")

                // Excluir recursos estáticos
                .excludePathPatterns(
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/images/**",
                        "/webjars/**",
                        "/favicon.ico"
                );
    }
}