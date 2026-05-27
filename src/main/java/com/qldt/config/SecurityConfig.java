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
                        // Tài nguyên tĩnh
                        .requestMatchers("/webjars/**", "/css/**", "/js/**", "/images/**").permitAll()

                        // Trang đăng nhập
                        .requestMatchers("/login", "/error").permitAll()

                        // ADMIN — toàn quyền
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // NHÂN VIÊN — bao gồm cả giảng viên
                        .requestMatchers("/nhanvien/**").hasAnyRole("ADMIN", "NHAN_VIEN")

                        // Portal giảng viên
                        .requestMatchers("/giangvien/**").hasAnyRole("ADMIN", "NHAN_VIEN")

                        // SINH VIÊN
                        .requestMatchers("/sinhvien/**").hasAnyRole("ADMIN", "SINH_VIEN")

                        // Thời khóa biểu
                        .requestMatchers("/tkb/**")
                        .hasAnyRole("ADMIN", "NHAN_VIEN", "SINH_VIEN")

                        // Đổi mật khẩu
                        .requestMatchers("/doi-mat-khau").authenticated()

                        // =====================================================
                        // ĐƠN XIN NGHỈ
                        // =====================================================

                        // Tạo / xem / hủy đơn của mình
                        .requestMatchers(
                                "/don-nghi",
                                "/don-nghi/tao",
                                "/don-nghi/*/huy",
                                "/don-nghi/*"
                        ).authenticated( )

                        // Duyệt / từ chối đơn
                        .requestMatchers(
                                "/don-nghi/duyet",
                                "/don-nghi/*/duyet",
                                "/don-nghi/*/tu-choi"
                        ).hasAnyRole(
                                "ADMIN",
                                "TK",
                                "CNTT",
                                "TBM", "PTK","NHAN_VIEN"
                        )

                        // =====================================================
                        // LỊCH DẠY BÙ
                        // =====================================================

                        // Giảng viên xem lịch dạy bù của mình
                        .requestMatchers("/lich-day-bu/cua-toi")
                        .authenticated()

                        // Xếp / sửa / hủy / hoàn thành
                        .requestMatchers(
                                "/lich-day-bu/xep",
                                "/lich-day-bu/*/sua",
                                "/lich-day-bu/*/huy",
                                "/lich-day-bu/*/hoan-thanh"
                        ).hasAnyRole(
                                "ADMIN",
                                "TK",
                                "CNTT",
                                "PTK",
                                "TBM", "NHAN_VIEN"
                        )

                        // Danh sách + chi tiết lịch dạy bù
                        .requestMatchers(
                                "/lich-day-bu",
                                "/lich-day-bu/*"
                        ).hasAnyRole(
                                "ADMIN",
                                "TK",
                                "CNTT",
                                "PTK",
                                "TBM", "NHAN_VIEN"
                        )

                        // =====================================================

                        // Còn lại cần đăng nhập
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler((req, res, auth) -> {
                            String role = auth.getAuthorities()
                                    .iterator()
                                    .next()
                                    .getAuthority();

                            switch (role) {
                                case "ROLE_ADMIN" ->
                                        res.sendRedirect("/admin/dashboard");

                                case "ROLE_NHAN_VIEN" ->
                                        res.sendRedirect("/nhanvien/redirect");

                                case "ROLE_SINH_VIEN" ->
                                        res.sendRedirect("/sinhvien/dashboard");

                                default ->
                                        res.sendRedirect("/");
                            }
                        })
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutRequestMatcher(
                                new AntPathRequestMatcher("/logout")
                        )
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