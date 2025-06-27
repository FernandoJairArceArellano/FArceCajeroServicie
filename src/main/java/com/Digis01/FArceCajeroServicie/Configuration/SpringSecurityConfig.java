package com.Digis01.FArceCajeroServicie.Configuration;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors().and()
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        configure -> configure
                                .requestMatchers("/login", "/access-denied").permitAll()
                                .requestMatchers(HttpMethod.POST, "/autenticateTheUser").permitAll()
                                .requestMatchers("/cajero/**").hasAnyRole("ADMINISTRADOR", "PROGRAMADOR", "ANALISTA", "Administrador", "Usuario", "Comprador", "Visitnate")
                                .requestMatchers(HttpMethod.POST, "/Usuario/GetAllDinamico").hasAnyRole("ADMINISTRADOR", "Comprador", "PROGRAMADOR", "Administrador", "ANALISTA", "Usuario")
                                .requestMatchers("/Usuario/CargaMasiva").hasAnyRole("ADMINISTRADOR", "Comprador", "PROGRAMADOR", "Administrador")
                                .requestMatchers(HttpMethod.GET, "/Usuario/**").hasAnyRole("ANALISTA", "Visitante", "PROGRAMADOR", "Administrador", "ADMINISTRADOR", "Comprador")
                                .requestMatchers(HttpMethod.POST, "/Usuario/**").hasAnyRole("PROGRAMADOR", "Administrador")
                                .requestMatchers("/cajeroapi/v1/**").permitAll()
                                .requestMatchers("/auth/status").permitAll()
                                .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                .loginPage("/Usuario/iniciarSesion")
                .loginProcessingUrl("/autenticateTheUser")
                .permitAll()
                .successHandler((request, response, authentication) -> {
                    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                    response.setHeader("Pragma", "no-cache");
                    response.setDateHeader("Expires", 0);
                    response.sendRedirect("http://localhost:8080/cajero/index");
                })
                )
                .logout((logout) -> logout
                .logoutUrl("/logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                    response.setHeader("Pragma", "no-cache");
                    response.setDateHeader("Expires", 0);
//                    response.sendRedirect("/login?logout");
                    response.sendRedirect("http://localhost:8080/Usuario/iniciarSesion");
                })
                )
                .exceptionHandling((exceptions) -> exceptions
                .accessDeniedPage("/access-denied")
                )
                .headers(headers -> headers
                .cacheControl(cache -> cache.disable())
                );

        return httpSecurity.build();
    }

    @Bean
    public UserDetailsService jdbcUserDetailsService(DataSource dataSource) {
        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager(dataSource);

        jdbcUserDetailsManager.setUsersByUsernameQuery("SELECT username, password, status FROM usuario WHERE username = ?");

        jdbcUserDetailsManager.setAuthoritiesByUsernameQuery(
                "SELECT username, CONCAT('ROLE_',nombrerol) AS authority FROM rolmanager WHERE username = ?");

        return jdbcUserDetailsManager;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:8080") // Cliente
                        .allowedMethods("GET", "POST")
                        .allowCredentials(true);
            }
        };
    }
}
