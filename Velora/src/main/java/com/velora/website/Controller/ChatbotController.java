package com.velora.website.Controller;

import com.velora.website.Repository.SanPhamRepository;
import com.velora.website.dto.ChatMessage;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(originPatterns = "*")
public class ChatbotController {

    private final SanPhamRepository sanPhamRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final JdbcTemplate jdbcTemplate;

    // Cache và Công tắc điều khiển
    private final Map<String, String> memoryCache = new HashMap<>();
    private final Map<String, Boolean> humanModeMap = new HashMap<>();

    private final String GEMINI_API_KEY = "AQ.Ab8RN6IG7DBXCmEAymGsocuMJJJNwDqZdhVGn5vHRDQhBJolDg";
    private final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key="
            + GEMINI_API_KEY;

    public ChatbotController(SanPhamRepository sanPhamRepository, SimpMessagingTemplate messagingTemplate,
            JdbcTemplate jdbcTemplate) {
        this.sanPhamRepository = sanPhamRepository;
        this.messagingTemplate = messagingTemplate;
        this.jdbcTemplate = jdbcTemplate;
    }

    // =================================================================================
    // 1. API: CHAT VỚI AI (Logic cũ giữ nguyên)
    // =================================================================================
    @PostMapping("/tu-van")
    public ResponseEntity<?> chatVoiAI(@RequestBody Map<String, String> request) {
        String cauHoi = request.get("message");
        String maPhienChat = request.get("maPhienChat");
        String currentTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        if (maPhienChat != null && humanModeMap.getOrDefault(maPhienChat, false)) {
            messagingTemplate.convertAndSend("/topic/chat/" + maPhienChat,
                    new ChatMessage("USER", cauHoi, currentTimestamp));
            return ResponseEntity.ok(Map.of("reply", "SILENT"));
        }

        String cacheKey = cauHoi.toLowerCase().trim();
        if (memoryCache.containsKey(cacheKey)) {
            String cauTraLoi = memoryCache.get(cacheKey);
            messagingTemplate.convertAndSend("/topic/chat/" + maPhienChat,
                    new ChatMessage("AI", cauTraLoi, currentTimestamp));
            return ResponseEntity.ok(Map.of("reply", cauTraLoi));
        }

        if (maPhienChat != null) {
            messagingTemplate.convertAndSend("/topic/chat/" + maPhienChat,
                    new ChatMessage("USER", cauHoi, currentTimestamp));
        }

        try {
            List<Object[]> danhSachSP = sanPhamRepository.layDuLieuChoChatbot();
            String dataWeb = danhSachSP.stream().map(sp -> {
                String tenSP = String.valueOf(sp[0]);
                String giaGoc = sp[1] != null ? String.valueOf(sp[1]) : "0";
                String trangThai = sp[2] != null ? String.valueOf(sp[2]) : "Không xác định";
                String giaHienThi = (giaGoc.equals("0") || giaGoc.equals("0.0")) ? "Giá chờ hàng (Liên hệ)"
                        : giaGoc + " VNĐ";
                return "- " + tenSP + " (Giá: " + giaHienThi + " | Trạng thái: " + trangThai + ")";
            }).collect(Collectors.joining("\n"));

            String prompt = "Bạn là trợ lý AI Velora Clock. Trả lời ngắn gọn dưới 80 chữ. Dữ liệu kho: " + dataWeb
                    + ". Khách hỏi: " + cauHoi;

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> requestBody = Map.of("contents",
                    List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            Map response = restTemplate.postForObject(GEMINI_URL, entity, Map.class);

            List candidates = (List) response.get("candidates");
            Map firstCandidate = (Map) candidates.get(0);
            Map content = (Map) firstCandidate.get("content");
            List parts = (List) content.get("parts");
            String cauTraLoi = (String) ((Map) parts.get(0)).get("text");

            memoryCache.put(cacheKey, cauTraLoi);
            messagingTemplate.convertAndSend("/topic/chat/" + maPhienChat,
                    new ChatMessage("AI", cauTraLoi, currentTimestamp));
            return ResponseEntity.ok(Map.of("reply", cauTraLoi));

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("reply", "Dạ, Quý khách nhấn [Tai nghe] để chuyên viên hỗ trợ ạ!"));
        }
    }

    // =================================================================================
    // 2. API: CÁC TIỆN ÍCH ĐỒNG BỘ (Dùng cho liên thiết bị)
    // =================================================================================

    // API Lấy lịch sử chat từ DB cho Khách/Admin
    @GetMapping("/history/{maPhienChat}")
    public ResponseEntity<?> getLichSuChat(@PathVariable String maPhienChat) {
        String sql = "SELECT NguoiGui as nguoiGui, NoiDungTinNhan as noiDungTinNhan, FORMAT(ThoiGianGui, 'HH:mm') as thoiGianGui FROM TinNhanChatAI WHERE MaPhienChat = (SELECT TOP 1 MaPhienChat FROM PhienChatAI WHERE MaDinhDanhKhach = ?) ORDER BY ThoiGianGui ASC";
        return ResponseEntity.ok(jdbcTemplate.queryForList(sql, maPhienChat));
    }

    // API Lấy tất cả phiên đang mở cho Admin
    @GetMapping("/active-sessions")
    public ResponseEntity<?> getActiveSessions() {
        // Lọc các phiên đang xử lý (Trạng thái AI_HANDLING)
        String sql = "SELECT MaDinhDanhKhach as maPhienChat, TieuDePhien as tieuDePhien FROM PhienChatAI WHERE TrangThaiChat IN ('AI_HANDLING', 'HUMAN_HANDLING')";
        return ResponseEntity.ok(jdbcTemplate.queryForList(sql));
    }

    // =================================================================================
    // 3. API: XỬ LÝ CHUYÊN VIÊN
    // =================================================================================
    @PostMapping("/request-human")
    public ResponseEntity<?> requestHuman(@RequestBody ChatMessage request) {
        humanModeMap.put(request.getMaPhienChat(), true); // Bật chế độ người thật

        Map<String, String> payload = new HashMap<>();
        payload.put("maPhienChat", request.getMaPhienChat());
        payload.put("tenKhach", (request.getTenKhach() != null) ? request.getTenKhach() : "Khách");

        messagingTemplate.convertAndSend("/topic/cvtv/requests", payload);
        return ResponseEntity.ok(Map.of("status", "success"));
    }

    @PostMapping("/admin-reply")
    public ResponseEntity<?> adminChatVoiKhach(@RequestBody Map<String, String> request) {
        String maPhienChat = request.get("maPhienChat");
        String tinNhan = request.get("message");
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        messagingTemplate.convertAndSend("/topic/chat/" + maPhienChat, new ChatMessage("ADMIN", tinNhan, ts));
        return ResponseEntity.ok(Map.of("status", "success"));
    }

    @PostMapping("/luu-lich-su")
    public ResponseEntity<?> luuLichSuChatVaoDB(@RequestBody Map<String, Object> request) {
        try {
            String maPhienKhach = String.valueOf(request.get("maPhienChat"));
            String tenKhach = String.valueOf(request.get("tieuDePhien"));

            // Lưu Session chat
            String sqlPhien = "INSERT INTO PhienChatAI (MaDinhDanhKhach, TieuDePhien, TrangThaiChat) VALUES (?, ?, 'CLOSED')";
            jdbcTemplate.update(sqlPhien, maPhienKhach, tenKhach);

            humanModeMap.remove(maPhienKhach);
            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}