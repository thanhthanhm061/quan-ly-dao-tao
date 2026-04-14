package com.qldt.config;

import com.qldt.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Tài nguyên tĩnh - cho phép tất cả
                .requestMatchers("/webjars/**", "/css/**", "/js/**", "/images/**").permitAll()
                // Trang đăng nhập
                .requestMatchers("/login", "/error").permitAll()

                // ADMIN - toàn quyền
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // GIẢNG VIÊN
                .requestMatchers("/giangvien/**").hasAnyRole("ADMIN", "GIANG_VIEN")

                // SINH VIÊN
                .requestMatchers("/sinhvien/**").hasAnyRole("ADMIN", "SINH_VIEN")

                // Các route chung
                .requestMatchers("/tkb/**").hasAnyRole("ADMIN", "GIANG_VIEN", "SINH_VIEN")
                .requestMatchers("/doi-mat-khau").authenticated()

                // Còn lại cần đăng nhập
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                // Chuyển hướng theo role
                .successHandler((req, res, auth) -> {
                    String role = auth.getAuthorities().iterator().next().getAuthority();
                    switch (role) {
                        case "ROLE_ADMIN"       -> res.sendRedirect("/admin/dashboard");
                        case "ROLE_GIANG_VIEN"  -> res.sendRedirect("/giangvien/dashboard");
                        case "ROLE_SINH_VIEN"   -> res.sendRedirect("/sinhvien/dashboard");
                        default                 -> res.sendRedirect("/");
                    }
                })
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied")
            );

        return http.build();
    }
}
