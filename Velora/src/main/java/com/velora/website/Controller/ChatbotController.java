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
import org.springframework.jdbc.core.JdbcTemplate; // Đã thêm import JdbcTemplate

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(originPatterns = "*")
public class ChatbotController {

    private final SanPhamRepository sanPhamRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    // ĐÃ THÊM: Công cụ để bắn lệnh SQL trực tiếp vào Database
    private final JdbcTemplate jdbcTemplate;
    
    // 1. Bộ nhớ đệm chống nghẽn (Cache)
    private final Map<String, String> memoryCache = new HashMap<>();

    // 2. CÔNG TẮC: Ghi nhớ xem phòng chat nào đang được Người Thật (Chuyên viên) tiếp quản
    private final Map<String, Boolean> humanModeMap = new HashMap<>();

    // API KEY
    private final String GEMINI_API_KEY = "AQ.Ab8RN6IG7DBXCmEAymGsocuMJJJNwDqZdhVGn5vHRDQhBJolDg"; 
    private final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=" + GEMINI_API_KEY;

    // ĐÃ SỬA: Bơm thêm JdbcTemplate vào Constructor
    public ChatbotController(SanPhamRepository sanPhamRepository, SimpMessagingTemplate messagingTemplate, JdbcTemplate jdbcTemplate) {
        this.sanPhamRepository = sanPhamRepository;
        this.messagingTemplate = messagingTemplate;
        this.jdbcTemplate = jdbcTemplate;
    }

    // =================================================================================
    // API 1: CHAT VỚI AI 
    // =================================================================================
    @PostMapping("/tu-van")
    public ResponseEntity<?> chatVoiAI(@RequestBody Map<String, String> request) {
        String cauHoi = request.get("message");
        String maPhienChat = request.get("maPhienChat");
        String currentTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        // [QUAN TRỌNG] Kiểm tra nếu phòng này đã có Chuyên viên vào -> Khóa AI lại
        if (maPhienChat != null && humanModeMap.getOrDefault(maPhienChat, false)) {
            // Đẩy tin của khách lên để Admin đọc, nhưng AI không trả lời
            messagingTemplate.convertAndSend("/topic/chat/" + maPhienChat, new ChatMessage("USER", cauHoi, currentTimestamp));
            return ResponseEntity.ok(Map.of("reply", "SILENT")); 
        }

        // KIỂM TRA CACHE
        String cacheKey = cauHoi.toLowerCase().trim();
        if (memoryCache.containsKey(cacheKey)) {
            String cauTraLoi = memoryCache.get(cacheKey);
            if (maPhienChat != null && !maPhienChat.isEmpty()) {
                messagingTemplate.convertAndSend("/topic/chat/" + maPhienChat, new ChatMessage("AI", cauTraLoi, currentTimestamp));
            }
            return ResponseEntity.ok(Map.of("reply", cauTraLoi));
        }

        // ĐẨY TIN NHẮN CỦA KHÁCH LÊN WEBSOCKET TRƯỚC
        if (maPhienChat != null && !maPhienChat.isEmpty()) {
            ChatMessage msgKhach = new ChatMessage("USER", cauHoi, currentTimestamp);
            messagingTemplate.convertAndSend("/topic/chat/" + maPhienChat, msgKhach);
        }

        try {
            // LẤY DỮ LIỆU SẢN PHẨM TỪ DB
            List<Object[]> danhSachSP = sanPhamRepository.layDuLieuChoChatbot();
            String dataWeb = danhSachSP.stream().map(sp -> {
                String tenSP = String.valueOf(sp[0]);
                String giaGoc = sp[1] != null ? String.valueOf(sp[1]) : "0";
                String trangThai = sp[2] != null ? String.valueOf(sp[2]) : "Không xác định";
                String giaHienThi = (giaGoc.equals("0") || giaGoc.equals("0.0")) ? "Giá chờ hàng (Liên hệ)" : giaGoc + " VNĐ";
                return "- " + tenSP + " (Giá: " + giaHienThi + " | Trạng thái: " + trangThai + ")";
            }).collect(Collectors.joining("\n"));

            String prompt = "Bạn là trợ lý AI chuyên nghiệp của thương hiệu đồng hồ cao cấp Velora Clock. "
                    + "BẠN PHẢI TUÂN THỦ TUYỆT ĐỐI CÁC QUY TẮC SỐNG CÒN SAU:\n"
                    + "1. PHẠM VI TRẢ LỜI: CHỈ trả lời các vấn đề về đồng hồ và dịch vụ của Velora. TỪ CHỐI mọi câu hỏi ngoài luồng.\n"
                    + "2. CHÍNH SÁCH BÁO GIÁ (CỰC KỲ QUAN TRỌNG): TUYỆT ĐỐI KHÔNG tự ý bịa giá. Bạn phải kiểm tra mức giá trong dữ liệu kho hàng bên dưới:\n"
                    + "   - NẾU SẢN PHẨM CÓ GIÁ TRÊN 100.000.000 VNĐ HOẶC là 'Giá chờ hàng': TUYỆT ĐỐI KHÔNG nói ra con số. CHỈ ĐƯỢC báo khách rằng: 'Dạ, mẫu siêu phẩm này Velora đang có sẵn. Vì thuộc phân khúc cao cấp, Quý khách vui lòng nhấn biểu tượng [Tai nghe] để chuyên viên báo giá ạ!'.\n"
                    + "   - NẾU SẢN PHẨM DƯỚI 100 TRIỆU: Báo giá bình thường.\n"
                    + "3. VĂN PHONG: Xưng 'Velora', gọi khách là 'Quý khách'. Trả lời CỰC KỲ NGẮN GỌN (dưới 80 chữ).\n\n"
                    + "Dữ liệu kho hàng hôm nay:\n" + dataWeb + "\n\n"
                    + "Khách hàng nói: " + cauHoi;

            // CALL GOOGLE GEMINI API
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> requestBody = Map.of("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            Map response = null;
            int maxRetries = 3;
            for (int i = 0; i < maxRetries; i++) {
                try {
                    response = restTemplate.postForObject(GEMINI_URL, entity, Map.class);
                    break;
                } catch (Exception e) {
                    if (i == maxRetries - 1) throw e;
                    Thread.sleep(1000);
                }
            }
            
            // TRÍCH XUẤT CÂU TRẢ LỜI
            List candidates = (List) response.get("candidates");
            Map firstCandidate = (Map) candidates.get(0);
            Map content = (Map) firstCandidate.get("content");
            List parts = (List) content.get("parts"); 
            Map firstPart = (Map) parts.get(0);
            String cauTraLoi = (String) firstPart.get("text");

            // LƯU CACHE & ĐẨY LÊN WEBSOCKET
            memoryCache.put(cacheKey, cauTraLoi);
            if (maPhienChat != null && !maPhienChat.isEmpty()) {
                messagingTemplate.convertAndSend("/topic/chat/" + maPhienChat, new ChatMessage("AI", cauTraLoi, currentTimestamp));
            }

            return ResponseEntity.ok(Map.of("reply", cauTraLoi));

        } catch (Exception e) {
            System.err.println("=== LỖI CHATBOT AI - CHUYỂN SANG DỰ PHÒNG ===");
            e.printStackTrace();
            String cauTraLoiDuPhong = getFallbackAnswer(cauHoi);
            if (maPhienChat != null && !maPhienChat.isEmpty()) {
                messagingTemplate.convertAndSend("/topic/chat/" + maPhienChat, new ChatMessage("AI", cauTraLoiDuPhong, currentTimestamp));
            }
            return ResponseEntity.ok(Map.of("reply", cauTraLoiDuPhong));
        }
    }

    // =================================================================================
    // API 2: KHÁCH BẤM NÚT TAI NGHE (ĐỂ Vue.js gọi lên không bị 404)
    // =================================================================================
    @PostMapping("/request-human")
    public ResponseEntity<?> yeuCauNhanVien(@RequestBody Map<String, String> request) {
        String maPhienChat = request.get("maPhienChat");
        String currentTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        if (maPhienChat == null || maPhienChat.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Thiếu mã phiên chat"));
        }

        // Bật công tắc khóa AI
        humanModeMap.put(maPhienChat, true);

        // Báo cho khách biết đã kết nối
        String tbKhach = "Kết nối thành công! Chuyên viên Velora đã sẵn sàng hỗ trợ Quý khách.";
        messagingTemplate.convertAndSend("/topic/chat/" + maPhienChat, new ChatMessage("SYSTEM", tbKhach, currentTimestamp));

        // Phát loa sang màn hình ADMIN
        Map<String, String> yeuCau = new HashMap<>();
        yeuCau.put("maPhienChat", maPhienChat);
        yeuCau.put("tenKhach", "Khách hàng VIP 00" + maPhienChat); 
        
        messagingTemplate.convertAndSend("/topic/cvtv/requests", yeuCau);

        return ResponseEntity.ok(Map.of("status", "Đã chuyển yêu cầu thành công"));
    }

    // =================================================================================
    // API 3: CHUYÊN VIÊN (ADMIN) GỬI TIN NHẮN CHO KHÁCH
    // =================================================================================
    @PostMapping("/admin-reply")
    public ResponseEntity<?> adminChatVoiKhach(@RequestBody Map<String, String> request) {
        String maPhienChat = request.get("maPhienChat");
        String tinNhan = request.get("message");
        String currentTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        if (maPhienChat != null && !maPhienChat.isEmpty()) {
            ChatMessage msgAdmin = new ChatMessage("ADMIN", tinNhan, currentTimestamp);
            messagingTemplate.convertAndSend("/topic/chat/" + maPhienChat, msgAdmin);
            return ResponseEntity.ok(Map.of("status", "success"));
        }
        
        return ResponseEntity.badRequest().body(Map.of("error", "Thiếu thông tin"));
    }

    // =================================================================================
    // API 4: CHUYÊN VIÊN ẤN NÚT "NGẮT" -> LƯU TOÀN BỘ CHAT VÀO DATABASE
    // =================================================================================
    @SuppressWarnings("unchecked")
    @PostMapping("/luu-lich-su")
    public ResponseEntity<?> luuLichSuChatVaoDB(@RequestBody Map<String, Object> request) {
        try {
            String maPhienKhach = String.valueOf(request.get("maPhienChat"));
            String tenKhach = String.valueOf(request.get("tieuDePhien"));
            
            // Ép kiểu mảng tin nhắn từ request
            List<Map<String, String>> danhSachTinNhan = (List<Map<String, String>>) request.get("tinNhanList");

            // 1. TẠO PHIÊN CHAT MỚI (Lấy ra ID tự động tăng)
            String sqlPhien = "INSERT INTO PhienChatAI (MaDinhDanhKhach, DiaChiIP, TieuDePhien, ThoiGianBatDau, ThoiGianCapNhat) " +
                              "OUTPUT INSERTED.MaPhienChat VALUES (?, ?, ?, GETDATE(), GETDATE())";
            
            Integer maPhienDB = jdbcTemplate.queryForObject(sqlPhien, new Object[]{maPhienKhach, "127.0.0.1", tenKhach}, Integer.class);

            // 2. LƯU HÀNG LOẠT TIN NHẮN
            if (danhSachTinNhan != null && !danhSachTinNhan.isEmpty()) {
                String sqlTinNhan = "INSERT INTO TinNhanChatAI (MaPhienChat, NguoiGui, NoiDungTinNhan, ThoiGianGui) VALUES (?, ?, ?, GETDATE())";
                for (Map<String, String> msg : danhSachTinNhan) {
                    jdbcTemplate.update(sqlTinNhan, maPhienDB, msg.get("nguoiGui"), msg.get("noiDungTinNhan"));
                }
            }

            // 3. Trả tự do cho AI ở phòng này
            humanModeMap.remove(maPhienKhach);

            // 4. Báo cho khách biết chuyên viên đã rời đi
            messagingTemplate.convertAndSend("/topic/chat/" + maPhienKhach, 
                new ChatMessage("SYSTEM", "Chuyên viên đã kết thúc phiên hỗ trợ. Cảm ơn Quý khách!", ""));

            return ResponseEntity.ok(Map.of("status", "success", "message", "Đã lưu DB thành công!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // HÀM DỰ PHÒNG
    private String getFallbackAnswer(String cauHoi) {
        String input = cauHoi.toLowerCase();
        if (input.contains("giá") || input.contains("bao nhiêu")) {
            return "Dạ, vì thuộc phân khúc cao cấp, mời Quý khách nhấn biểu tượng [Tai nghe] để chuyên viên báo giá chi tiết ạ!";
        }
        return "Dạ, hệ thống Velora đã ghi nhận yêu cầu. Quý khách vui lòng nhấn biểu tượng [Tai nghe] để chuyên viên tư vấn chi tiết ạ!";
    }
}