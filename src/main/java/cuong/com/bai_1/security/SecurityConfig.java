package cuong.com.bai_1.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // Bean dùng để mã hóa mật khẩu
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean dùng để quản lý quá trình đăng nhập
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Cấu hình phân quyền đường dẫn API
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 1. Mở cửa tự do cho đăng ký/đăng nhập
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()

                        // 2. Mở cửa tự do cho phép xem danh sách sản phẩm (Phương thức GET)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/products").permitAll()

                        // 3. Chỉ cho phép ROLE_ADMIN hoặc ROLE_STAFF sử dụng POST, PUT, DELETE trên API products
                        .requestMatchers("/api/products/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF")

                        // Các API khác (nếu có) phải đăng nhập
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}