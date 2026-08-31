package com.hospital.his.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(

                                // PORTAL
                                "/",
                                "/portal",
                                "/caja/**",
                                "/api/caja/**",
                                "/panel",
                                "/panel/**",
                                "/enfermeria/**",
                                "/api/enfermeria/**",

                                // LOGIN PACIENTE
                                "/login",
                                "/logout",

                                // REGISTRO PACIENTE
                                "/registro",

                                // CITAS PACIENTE
                                "/paciente/citas/**",

                                // PAGO PACIENTE
                                "/paciente/pago",
                                "/paciente/pago/**",

                                // ADMINISTRACIÓN
                                "/admin/login",
                                "/admin/usuarios",
                                "/admin/usuarios/**",

                                // API PACIENTE
                                "/api/login",
                                "/api/registro",
                                "/api/verificar-dpi/**",

                                // API CITAS
                                "/api/citas/**",

                                "/recepcion/**",
                                "/api/recepcion/**",

                                // API PAGOS
                                "/api/pagos/**",

                                // API ADMINISTRADOR
                                "/api/admin/login",
                                "/api/admin/usuarios/**",

                                // RECURSOS ESTÁTICOS
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/images/**",
                                "/error"
                        )
                        .permitAll()

                        .anyRequest()
                        .permitAll()
                )

                // Usamos nuestros propios formularios
                .formLogin(form -> form.disable())

                // Desactivar autenticación HTTP Basic
                .httpBasic(basic -> basic.disable())

                // Ignorar CSRF en nuestras APIs
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(

                                "/api/login",

                                "/api/registro",
                                "/api/caja/**",

                                "/api/admin/login",

                                "/api/recepcion/**",

                                "/api/verificar-dpi/**",

                                "/api/admin/usuarios/**",

                                // CU-03 - CITAS
                                "/api/citas/**",
                                "/api/enfermeria/**",

                                // CU-04 - PAGOS
                                "/api/pagos/**"
                        )
                );

        return http.build();
    }
}