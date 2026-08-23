package com.velora.website.Config;

import com.velora.website.Entity.NguoiDung;
import com.velora.website.Entity.VaiTro;
import com.velora.website.Repository.NguoiDungRepository;
import com.velora.website.Repository.VaiTroRepository;
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

import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final NguoiDungRepository nguoiDungRepository;
    private final VaiTroRepository vaiTroRepository;

    SecurityConfig(NguoiDungRepository nguoiDungRepository, VaiTroRepository vaiTroRepository) {
        this.nguoiDungRepository = nguoiDungRepository;
        this.vaiTroRepository = vaiTroRepository;
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
                        
                        HttpSession session = request.getSession(false);
                        String mode = "login"; // Mặc định là đăng nhập
                        if (session != null) {
                            Object savedMode = session.getAttribute("oauth2_mode");
                            if (savedMode != null) {
                                mode = savedMode.toString();
                                session.removeAttribute("oauth2_mode");
                            }
                        }

                        String registrationId = "";
                        if (authentication instanceof OAuth2AuthenticationToken) {
                            registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
                        }

                        String provider = registrationId != null ? registrationId.toUpperCase() : "UNKNOWN";
                        String providerId = "";
                        String email = "";
                        String name = "";

                        if ("GOOGLE".equalsIgnoreCase(provider)) {
                            providerId = oauth2User.getAttribute("sub");
                            email = oauth2User.getAttribute("email");
                            name = oauth2User.getAttribute("name");
                        } else if ("FACEBOOK".equalsIgnoreCase(provider)) {
                            providerId = oauth2User.getAttribute("id");
                            email = oauth2User.getAttribute("email");
                            if (email == null) {
                                email = providerId + "@facebook.com";
                            }
                            name = oauth2User.getAttribute("name");
                        }

                        if (providerId == null || providerId.isEmpty()) {
                            providerId = oauth2User.getName();
                        }
                        if (name == null || name.isEmpty()) {
                            name = "Khách hàng Mạng Xã Hội";
                        }

                        // 🔥 KIỂM TRA CHÉO 2 CHIỀU (CHẶN TÀI KHOẢN THỦ CÔNG & CHÉO MẠNG XÃ HỘI)
                        NguoiDung existingUser = null;

                        if ("GOOGLE".equalsIgnoreCase(provider)) {
                            if (email != null && !email.isEmpty()) {
                                existingUser = nguoiDungRepository.findByEmail(email).orElse(null);
                                
                                if (existingUser != null) {
                                    String dbProvider = existingUser.getProvider();
                                    // Nếu email này đã tồn tại nhưng là tài khoản thủ công (provider null/rỗng) hoặc thuộc mạng xã hội khác
                                    if (dbProvider == null || dbProvider.trim().isEmpty() || !"GOOGLE".equalsIgnoreCase(dbProvider)) {
                                        String methodStr = (dbProvider == null || dbProvider.trim().isEmpty()) ? "đăng ký thủ công" : dbProvider;
                                        String errorMsg = URLEncoder.encode("Email này đã được đăng ký bằng phương thức " + methodStr + "! Vui lòng sử dụng đúng cách đăng nhập cũ.", StandardCharsets.UTF_8);
                                        response.sendRedirect("http://localhost:5174/dang-nhap?error=" + errorMsg);
                                        return;
                                    }
                                }
                            }
                            if (existingUser == null && providerId != null && !providerId.isEmpty()) {
                                existingUser = nguoiDungRepository.findByProviderAndProviderId(provider, providerId).orElse(null);
                            }

                        } else if ("FACEBOOK".equalsIgnoreCase(provider)) {
                            if (providerId != null && !providerId.isEmpty()) {
                                existingUser = nguoiDungRepository.findByProviderAndProviderId(provider, providerId).orElse(null);
                            }
                            if (email != null && !email.isEmpty() && !email.endsWith("@facebook.com")) {
                                NguoiDung emailUser = nguoiDungRepository.findByEmail(email).orElse(null);
                                if (emailUser != null) {
                                    String dbProvider = emailUser.getProvider();
                                    boolean isCurrentFacebookUser = "FACEBOOK".equalsIgnoreCase(dbProvider) && providerId.equals(emailUser.getProviderId());
                                    
                                    // Nếu email này thuộc về tài khoản thủ công hoặc tài khoản Google khác
                                    if (!isCurrentFacebookUser) {
                                        String methodStr = (dbProvider == null || dbProvider.trim().isEmpty()) ? "đăng ký thủ công" : dbProvider;
                                        String errorMsg = URLEncoder.encode("Email này đã được sử dụng bởi tài khoản " + methodStr + "! Không thể dùng chung chéo.", StandardCharsets.UTF_8);
                                        response.sendRedirect("http://localhost:5174/dang-nhap?error=" + errorMsg);
                                        return;
                                    }
                                }
                            }
                        }

                        String targetUrl = "http://localhost:5174/";

                        if ("register".equalsIgnoreCase(mode)) {
                            if (existingUser != null) {
                                String errorMsg = URLEncoder.encode("Tài khoản này đã tồn tại trên hệ thống, vui lòng chuyển sang đăng nhập!", StandardCharsets.UTF_8);
                                targetUrl = "http://localhost:5174/dang-nhap?error=" + errorMsg;
                            } else {
                                NguoiDung newUser = new NguoiDung();
                                newUser.setProvider(provider);
                                newUser.setProviderId(providerId);
                                newUser.setEmail(email);
                                newUser.setHoTen(name);
                                newUser.setTrangThai("HOAT_DONG");
                                newUser.setNgayTao(new Date());
                                newUser.setNgayCapNhat(new Date());
                                newUser.setSoLanViPham(0);
                                
                                VaiTro roleUser = vaiTroRepository.findByTenVaiTro("ROLE_CUSTOMER")
                                        .orElseThrow(() -> new RuntimeException("Lỗi Hệ Thống: Không tìm thấy quyền ROLE_CUSTOMER trong Database!"));
                                List<VaiTro> danhSachQuyen = new ArrayList<>();
                                danhSachQuyen.add(roleUser);
                                newUser.setVaiTros(danhSachQuyen);

                                BCryptPasswordEncoder tempEncoder = new BCryptPasswordEncoder();
                                newUser.setMatKhauMaHoa(tempEncoder.encode("OAUTH2_SECURE_PASS_" + System.currentTimeMillis()));

                                nguoiDungRepository.save(newUser);

                                String encodedName = URLEncoder.encode(name, StandardCharsets.UTF_8);
                                targetUrl = "http://localhost:5174/cap-nhat-thong-tin?email=" + email + "&name=" + encodedName + "&provider=" + provider;
                            }
                        } else {
                            if (existingUser == null) {
                                String errorMsg = URLEncoder.encode("Tài khoản chưa được liên kết, vui lòng đăng ký trước!", StandardCharsets.UTF_8);
                                targetUrl = "http://localhost:5174/dang-ky?error=" + errorMsg;
                            } else {
                                if (existingUser.getProviderId() == null) {
                                    existingUser.setProvider(provider);
                                    existingUser.setProviderId(providerId);
                                    nguoiDungRepository.save(existingUser);
                                }

                                if (existingUser.getSoDienThoai() == null || existingUser.getDiaChi() == null || existingUser.getSoDienThoai().isEmpty()) {
                                    String encodedName = URLEncoder.encode(existingUser.getHoTen(), StandardCharsets.UTF_8);
                                    targetUrl = "http://localhost:5174/cap-nhat-thong-tin?email=" + existingUser.getEmail() + "&name=" + encodedName + "&provider=" + provider;
                                } else {
                                    targetUrl = "http://localhost:5174/";
                                }
                            }
                        }

                        response.sendRedirect(targetUrl);
                    })
                )
                .logout(logout -> logout
                    .logoutUrl("/api/auth/logout")
                    .invalidateHttpSession(true)
                    .clearAuthentication(true)
                    .deleteCookies("JSESSIONID")
                    .logoutSuccessHandler((request, response, authentication) -> {
                        response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_OK);
                        response.getWriter().write("Đăng xuất thành công");
                        response.getWriter().flush();
                    })
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/me", "/api/**", "/oauth2/**", "/login/**").permitAll()
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