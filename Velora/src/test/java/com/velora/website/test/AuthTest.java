package com.velora.website.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.velora.website.Controller.AuthController;
import com.velora.website.Entity.NguoiDung;
import com.velora.website.Repository.NguoiDungRepository;
import com.velora.website.Repository.VaiTroRepository;
import com.velora.website.Request.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print; // 🔥 Thêm import này để in log kết quả
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AuthTest {

    private MockMvc mockMvc;

    @Mock
    private NguoiDungRepository nguoiDungRepository;

    @Mock
    private VaiTroRepository vaiTroRepository;

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    // Trường hợp: Đăng nhập thành công với tài khoản và mật khẩu hợp lệ
    void testDangNhapThanhCong() throws Exception {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode("123456");

        NguoiDung mockUser = new NguoiDung();
        mockUser.setMaNguoiDung(3);
        mockUser.setHoTen("Lê Hoàng Khách");
        mockUser.setEmail("khachhang1@gmail.com");
        mockUser.setMatKhauMaHoa(hashedPassword);
        mockUser.setTrangThai("HOAT_DONG");

        Mockito.when(nguoiDungRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("khachhang1@gmail.com");
        loginRequest.setPassword("123456");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print()) // 🔥 In chi tiết Request/Response ra cửa sổ Debug / Terminal
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("khachhang1@gmail.com"));
    }

    @Test
    // Trường hợp: Đăng nhập thất bại do nhập sai mật khẩu
    void testDangNhapThatBaidoSaiMatKhau() throws Exception {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        NguoiDung mockUser = new NguoiDung();
        mockUser.setEmail("khachhang1@gmail.com");
        mockUser.setMatKhauMaHoa(encoder.encode("123456"));
        mockUser.setTrangThai("HOAT_DONG");

        Mockito.when(nguoiDungRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("khachhang1@gmail.com");
        loginRequest.setPassword("sai_mat_khau_nay");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print()) // 🔥 In kết quả ra console
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Sai email hoặc mật khẩu!")); // Kiểm tra đúng thông báo trả về
    }

    @Test
    // Trường hợp: Đăng nhập thất bại do không nhập tài khoản (để trống email)
    void testDangNhapKhongNhapTaiKhoan() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("");
        loginRequest.setPassword("123456");

        Mockito.when(nguoiDungRepository.findByEmail("")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print()) // 🔥 In kết quả ra console
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Sai email hoặc mật khẩu!"));
    }

    @Test
    // Trường hợp: Đăng nhập thất bại do không nhập mật khẩu (để trống password)
    void testDangNhapKhongNhapMatKhau() throws Exception {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        NguoiDung mockUser = new NguoiDung();
        mockUser.setEmail("khachhang1@gmail.com");
        mockUser.setMatKhauMaHoa(encoder.encode("123456"));
        mockUser.setTrangThai("HOAT_DONG");

        Mockito.when(nguoiDungRepository.findByEmail("khachhang1@gmail.com")).thenReturn(Optional.of(mockUser));

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("khachhang1@gmail.com");
        loginRequest.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print()) // 🔥 In kết quả ra console
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Sai email hoặc mật khẩu!"));
    }

    @Test
    // Trường hợp: Đăng nhập thất bại do không nhập cả tài khoản và mật khẩu (bỏ trống cả hai)
    void testDangNhapKhongNhapCaTaiKhoanVaMatKhau() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("");
        loginRequest.setPassword("");

        Mockito.when(nguoiDungRepository.findByEmail("")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print()) // 🔥 In kết quả ra console
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Sai email hoặc mật khẩu!"));
    }
}