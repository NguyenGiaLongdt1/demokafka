package com.example.demo.security.config;

import com.example.demo.security.jwt.JwtAuthenticationFilter;
import com.example.demo.security.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Cho phép dùng @PreAuthorize trên Controller/Service
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserRepository userRepository;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Khong tim thay user: " + username));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Mã hóa mật khẩu bằng BCrypt chuẩn bảo mật
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Tắt CSRF vì REST API dùng Stateless JWT
            .csrf(csrf -> csrf.disable())
            
            // 2. Kích hoạt cấu hình CORS bên dưới
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 3. Phân quyền Endpoint
            .authorizeHttpRequests(auth -> auth
                // Public các API đăng ký/đăng nhập/refresh/logout
                .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/refresh", "/api/auth/logout").permitAll()
                
                // Public các API test OTP / Rate limit nếu muốn
                .requestMatchers("/practice/**").permitAll()
                
                // Submit order sang Kafka chỉ dành cho ADMIN
                .requestMatchers(HttpMethod.POST, "/api/orders/*/submit").hasRole("ADMIN")
                
                // Tất cả request còn lại bắt buộc phải đăng nhập (có JWT)
                .anyRequest().authenticated()
            )
            
            // 4. Trả về JSON chuẩn khi bị từ chối truy cập (401 / 403)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("""
                        {"status":401,"error":"Unauthorized","message":"Ban can dang nhap va gui kem Bearer Token"}
                        """);
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("""
                        {"status":403,"error":"Forbidden","message":"Ban khong co quyen (Role) de thuc hien thao tac nay"}
                        """);
                })
            )

            // 5. Quản lý Session: STATELESS (không lưu session trên RAM Server)
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 6. Thêm Authentication Provider & JWT Filter
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Cấu hình CORS cho phép React / Vue gọi API
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173")); // Domain Frontend
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
