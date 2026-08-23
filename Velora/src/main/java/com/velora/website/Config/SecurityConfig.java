package com.velora.website.Config;

import com.velora.website.Entity.NguoiDung;
import com.velora.website.Repository.NguoiDungRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final NguoiDungRepository nguoiDungRepository;

    SecurityConfig(NguoiDungRepository nguoiDungRepository) {
        this.nguoiDungRepository = nguoiDungRepository;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .oauth2Login(oauth2 -> oauth2
                    .successHandler((request, response, authentication) -> {
                        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                        
                        // Lấy định danh nhà cung cấp (google hoặc facebook)
                        String registrationId = "";
                        if (authentication instanceof OAuth2AuthenticationToken) {
                            registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
                        }

                        String email = "";
                        String name = "";

                        if ("google".equalsIgnoreCase(registrationId)) {
                            // Xử lý riêng cho Google
                            email = oauth2User.getAttribute("email");
                            name = oauth2User.getAttribute("name");
                        } else if ("facebook".equalsIgnoreCase(registrationId)) {
                            // Xử lý riêng cho Facebook (phòng hờ trường hợp Facebook không cấp email)
                            email = oauth2User.getAttribute("email");
                            if (email == null) {
                                Object fbId = oauth2User.getAttribute("id");
                                email = (fbId != null ? fbId.toString() : "fb_user_" + System.currentTimeMillis()) + "@facebook.com";
                            }
                            name = oauth2User.getAttribute("name");
                        }

                        // Fallback chung nếu vẫn trống
                        if (email == null || email.isEmpty()) {
                            email = "social_" + System.currentTimeMillis() + "@velora.com";
                        }
                        if (name == null || name.isEmpty()) {
                            name = "Khách hàng Mạng Xã Hội";
                        }

                        // Kiểm tra xem người dùng đã tồn tại trong database chưa
                        NguoiDung existingUser = nguoiDungRepository.findByEmail(email).orElse(null);

                        if (existingUser == null) {
                            // Chưa có: Lưu bản ghi mới vào DB với thông tin cơ bản
                            NguoiDung newUser = new NguoiDung();
                            newUser.setEmail(email);
                            newUser.setHoTen(name);
                            newUser.setTrangThai("HOAT_DONG");
                            
                            // 🔥 Gán mật khẩu ngẫu nhiên đã mã hóa để thỏa mãn điều kiện not-null của CSDL
                            BCryptPasswordEncoder tempEncoder = new BCryptPasswordEncoder();
                            newUser.setMatKhauMaHoa(tempEncoder.encode("OAUTH2_SECURE_PASS_" + System.currentTimeMillis()));

                            nguoiDungRepository.save(newUser);

                            // Chuyển hướng sang trang cập nhật thông tin trên Vue kèm email
                            response.sendRedirect("http://localhost:5174/cap-nhat-thong-tin?email=" + email);
                        } else {
                            // Đã có: Kiểm tra xem đã đủ SĐT và địa chỉ chưa
                            if (existingUser.getSoDienThoai() == null || existingUser.getDiaChi() == null || existingUser.getSoDienThoai().isEmpty()) {
                                response.sendRedirect("http://localhost:5174/cap-nhat-thong-tin?email=" + email);
                            } else {
                                // Đã đủ thông tin -> Vào thẳng trang chủ
                                response.sendRedirect("http://localhost:5174/");
                            }
                        }
                    })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**", "/oauth2/**", "/login/**").permitAll()
                        .anyRequest().permitAll());
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        configuration.setAllowedOrigins(List.of(
            "http://localhost:5174", 
            "http://127.0.0.1:5174", 
            "http://192.168.0.105:5174"
        ));
        
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}