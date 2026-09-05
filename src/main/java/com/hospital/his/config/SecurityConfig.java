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

                                // CAJA
                                "/caja/**",
                                "/api/caja/**",

                                // PANEL PRINCIPAL
                                "/panel",
                                "/panel/**",

                                // ENFERMERÍA
                                "/enfermeria/**",
                                "/api/enfermeria/**",

                                // MÉDICO
                                "/medico",
                                "/medico/**",
                                "/api/medico/**",

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

                                // RECEPCIÓN
                                "/recepcion/**",
                                "/api/recepcion/**",

                                // API PACIENTE
                                "/api/login",
                                "/api/registro",
                                "/api/verificar-dpi/**",

                                // API CITAS
                                "/api/citas/**",

                                // API PAGOS
                                "/api/pagos/**",

                                // API ADMIN
                                "/api/admin/login",
                                "/api/admin/usuarios/**",

                                // RECURSOS
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

                .formLogin(form -> form.disable())

                .httpBasic(basic -> basic.disable())

                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(

                                // LOGIN PACIENTE
                                "/api/login",

                                // LOGIN MÉDICO
                                "/medico/login",

                                // REGISTRO
                                "/api/registro",

                                // DPI
                                "/api/verificar-dpi/**",

                                // CAJA
                                "/api/caja/**",

                                // ADMIN
                                "/api/admin/login",
                                "/api/admin/usuarios/**",

                                // RECEPCIÓN
                                "/api/recepcion/**",

                                // CITAS
                                "/api/citas/**",

                                // ENFERMERÍA
                                "/api/enfermeria/**",

                                // MÉDICO
                                "/api/medico/**",

                                // PAGOS
                                "/api/pagos/**"
                        )
                );

        return http.build();
    }
}