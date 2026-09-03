package com.velora.website.Controller;

import com.velora.website.Repository.SanPhamRepository;
import com.velora.website.dto.ChatMessage;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.text.Normalizer;
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

    private final Map<String, Boolean> humanModeMap = new HashMap<>();

    // Đưa key vào application.properties càng sớm càng tốt: gemini.api.key=...
    private final String GEMINI_API_KEY = "DÁN_GEMINI_API_KEY_VÀO_ĐÂY";
    private final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key="
                    + GEMINI_API_KEY;

    public ChatbotController(SanPhamRepository sanPhamRepository,
                             SimpMessagingTemplate messagingTemplate,
                             JdbcTemplate jdbcTemplate) {
        this.sanPhamRepository = sanPhamRepository;
        this.messagingTemplate = messagingTemplate;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostMapping("/tu-van")
    public ResponseEntity<?> chatVoiAI(@RequestBody Map<String, String> request) {
        String cauHoi = request.getOrDefault("message", "");
        String maPhienChat = request.get("maPhienChat");
        String currentTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        if (maPhienChat != null && humanModeMap.getOrDefault(maPhienChat, false)) {
            messagingTemplate.convertAndSend("/topic/chat/" + maPhienChat,
                    new ChatMessage("USER", cauHoi, currentTimestamp));
            return ResponseEntity.ok(Map.of("reply", "SILENT"));
        }

        if (maPhienChat != null && !maPhienChat.isEmpty()) {
            messagingTemplate.convertAndSend("/topic/chat/" + maPhienChat,
                    new ChatMessage("USER", cauHoi, currentTimestamp));
        }

        List<Map<String, Object>> khoHangMapList = new ArrayList<>();

        try {
            List<Object[]> danhSachSP = sanPhamRepository.layDuLieuChoChatbot();
            for (Object[] row : danhSachSP) {
                khoHangMapList.add(mapSanPhamTuDb(row));
            }

            List<Map<String, Object>> matchedProducts = locSanPhamTheoYeuCau(cauHoi, khoHangMapList);

            String dataWeb = matchedProducts.isEmpty()
                    ? "(Không có sản phẩm khớp trong kho Velora)"
                    : matchedProducts.stream()
                    .map(p -> "- [ID:" + p.get("id") + "] "
                            + p.get("thuongHieu") + " | " + p.get("ten")
                            + " | " + p.get("giaHienThi")
                            + " | Tồn: " + p.get("tonKho"))
                    .collect(Collectors.joining("\n"));

            String prompt = "Đóng vai: chuyên viên tư vấn cao cấp thương hiệu đồng hồ Velora Clock.\n"
                    + "Tính cách: sang trọng, lịch sự. Xưng hô Velora / Quý khách.\n"
                    + "Quy tắc:\n"
                    + "- CẤM bịa sản phẩm, giá, model không có trong danh sách được cung cấp.\n"
                    + "- Trả lời tối đa 3 câu.\n"
                    + "- Nếu có sản phẩm: giới thiệu ngắn và mời Quý khách xem card bên dưới.\n"
                    + "- Nếu danh sách trống: xin lỗi, nói cửa hàng chưa có đúng mẫu đó, hỏi lại thương hiệu hoặc tầm giá.\n"
                    + "- Không liệt kê ID kỹ thuật.\n\n"
                    + "--- SẢN PHẨM ĐƯỢC PHÉP TƯ VẤN (LẤY TỪ DATABASE) ---\n"
                    + dataWeb + "\n\n"
                    + "--- CÂU HỎI CỦA KHÁCH ---\n"
                    + cauHoi;

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
            );
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(GEMINI_URL, entity, Map.class);

            String cauTraLoi = trichTextGemini(response);
            if (cauTraLoi == null || cauTraLoi.isBlank()) {
                cauTraLoi = matchedProducts.isEmpty()
                        ? "Dạ, hiện Velora chưa tìm thấy mẫu khớp yêu cầu trong kho. Quý khách vui lòng cho biết thương hiệu hoặc tầm giá ạ."
                        : "Dạ, Velora xin gửi Quý khách các tuyệt tác đang có tại cửa hàng:";
            }

            ChatMessage aiMsg = new ChatMessage("AI", cauTraLoi, currentTimestamp, matchedProducts);
            if (maPhienChat != null && !maPhienChat.isEmpty()) {
                messagingTemplate.convertAndSend("/topic/chat/" + maPhienChat, aiMsg);
            }

            return ResponseEntity.ok(Map.of("reply", cauTraLoi, "products", matchedProducts));

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> smartFallback = getSmartFallbackAnswer(cauHoi, khoHangMapList);
            String cauTraLoiDuPhong = (String) smartFallback.get("reply");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> matchedProducts =
                    (List<Map<String, Object>>) smartFallback.get("products");

            ChatMessage fbMsg = new ChatMessage("AI", cauTraLoiDuPhong, currentTimestamp, matchedProducts);
            if (maPhienChat != null && !maPhienChat.isEmpty()) {
                messagingTemplate.convertAndSend("/topic/chat/" + maPhienChat, fbMsg);
            }
            return ResponseEntity.ok(Map.of("reply", cauTraLoiDuPhong, "products", matchedProducts));
        }
    }

    @PostMapping("/request-human")
    public ResponseEntity<?> yeuCauNhanVien(@RequestBody Map<String, String> request) {
        String maPhienChat = request.get("maPhienChat");
        String currentTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        if (maPhienChat == null || maPhienChat.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Thiếu mã phiên chat"));
        }

        humanModeMap.put(maPhienChat, true);

        String tbKhach = "Kết nối thành công! Chuyên viên Velora đã sẵn sàng hỗ trợ Quý khách.";
        messagingTemplate.convertAndSend("/topic/chat/" + maPhienChat,
                new ChatMessage("SYSTEM", tbKhach, currentTimestamp));

        Map<String, String> yeuCau = new HashMap<>();
        yeuCau.put("maPhienChat", maPhienChat);
        yeuCau.put("tenKhach", "Khách hàng VIP 00" + maPhienChat);
        messagingTemplate.convertAndSend("/topic/cvtv/requests", yeuCau);

        return ResponseEntity.ok(Map.of("status", "Đã chuyển yêu cầu thành công"));
    }

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

    @SuppressWarnings("unchecked")
    @PostMapping("/luu-lich-su")
    public ResponseEntity<?> luuLichSuChatVaoDB(@RequestBody Map<String, Object> request) {
        try {
            String maPhienKhach = String.valueOf(request.get("maPhienChat"));
            String tenKhach = String.valueOf(request.get("tieuDePhien"));
            List<Map<String, String>> danhSachTinNhan =
                    (List<Map<String, String>>) request.get("tinNhanList");

            String sqlPhien = "INSERT INTO PhienChatAI (MaDinhDanhKhach, DiaChiIP, TieuDePhien, ThoiGianBatDau, ThoiGianCapNhat) "
                    + "OUTPUT INSERTED.MaPhienChat VALUES (?, ?, ?, GETDATE(), GETDATE())";
            Integer maPhienDB = jdbcTemplate.queryForObject(
                    sqlPhien,
                    new Object[]{maPhienKhach, "127.0.0.1", tenKhach},
                    Integer.class
            );

            if (danhSachTinNhan != null && !danhSachTinNhan.isEmpty()) {
                String sqlTinNhan = "INSERT INTO TinNhanChatAI (MaPhienChat, NguoiGui, NoiDungTinNhan, ThoiGianGui) "
                        + "VALUES (?, ?, ?, GETDATE())";
                for (Map<String, String> msg : danhSachTinNhan) {
                    jdbcTemplate.update(sqlTinNhan, maPhienDB, msg.get("nguoiGui"), msg.get("noiDungTinNhan"));
                }
            }

            humanModeMap.remove(maPhienKhach);
            messagingTemplate.convertAndSend("/topic/chat/" + maPhienKhach,
                    new ChatMessage("SYSTEM", "Chuyên viên đã kết thúc phiên hỗ trợ. Cảm ơn Quý khách!", ""));

            return ResponseEntity.ok(Map.of("status", "success", "message", "Đã lưu DB thành công!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
    private String trichTextGemini(Map<String, Object> response) {
        if (response == null) return null;
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        if (candidates == null || candidates.isEmpty()) return null;
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        if (content == null) return null;
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        if (parts == null || parts.isEmpty()) return null;
        Object text = parts.get(0).get("text");
        return text == null ? null : String.valueOf(text);
    }

    /**
     * Mapping đúng thứ tự SELECT:
     * 0 MaSanPham, 1 TenSanPham, 2 GiaBan, 3 TrangThai,
     * 4 AnhDaiDien, 5 TenThuongHieu, 6 DuongDanSlug, 7 SoLuongTonKho
     */
    private Map<String, Object> mapSanPhamTuDb(Object[] sp) {
        Object idObj = getCol(sp, 0, "");
        Object tenObj = getCol(sp, 1, "Sản phẩm Velora");
        Object giaObj = getCol(sp, 2, 0);
        Object trangThaiObj = getCol(sp, 3, "CON_HANG");
        Object anhObj = getCol(sp, 4, "VeloraIcon.png");
        Object brandObj = getCol(sp, 5, "");
        Object slugObj = getCol(sp, 6, idObj);
        Object tonObj = getCol(sp, 7, 0);

        double giaVal = 0;
        try {
            giaVal = Double.parseDouble(String.valueOf(giaObj).replace(",", "").trim());
        } catch (Exception ignored) {}

        int ton = 0;
        try {
            ton = Integer.parseInt(String.valueOf(tonObj).split("\\.")[0]);
        } catch (Exception ignored) {}

        Map<String, Object> item = new HashMap<>();
        item.put("id", idObj);
        item.put("maSanPham", idObj);
        item.put("slug", slugObj);
        item.put("ten", tenObj);
        item.put("thuongHieu", brandObj);
        item.put("gia", giaVal);
        item.put("giaSo", giaVal);
        item.put("giaHienThi", (giaVal == 0 || giaVal > 100_000_000)
                ? "Giá cần được tư vấn"
                : String.format("%,.0f ₫", giaVal));
        item.put("trangThai", trangThaiObj);
        item.put("anh", anhObj);
        item.put("tonKho", ton);
        return item;
    }

    private Object getCol(Object[] row, int i, Object fallback) {
        return (row != null && row.length > i && row[i] != null) ? row[i] : fallback;
    }

    private List<Map<String, Object>> locSanPhamTheoYeuCau(String cauHoi, List<Map<String, Object>> khoHang) {
        if (khoHang == null || khoHang.isEmpty()) return new ArrayList<>();
        String input = chuanHoa(cauHoi);

        String brand = batThuongHieu(input, khoHang);
        if (brand != null) {
            List<Map<String, Object>> theoHang = new ArrayList<>();
            for (Map<String, Object> p : khoHang) {
                String th = chuanHoa(String.valueOf(p.get("thuongHieu")));
                String ten = chuanHoa(String.valueOf(p.get("ten")));
                if (th.contains(brand) || ten.contains(brand)) {
                    theoHang.add(p);
                }
            }
            if (!theoHang.isEmpty()) {
                return theoHang;
            }
        }

        List<Map<String, Object>> theoTen = new ArrayList<>();
        for (Map<String, Object> p : khoHang) {
            String ten = chuanHoa(String.valueOf(p.get("ten")));
            if (!ten.isBlank() && (input.contains(ten) || ten.contains(input))) {
                theoTen.add(p);
            }
        }
        if (!theoTen.isEmpty()) return theoTen;

        List<Map<String, Object>> theoTu = new ArrayList<>();
        String[] words = input.split("\\s+");
        for (Map<String, Object> p : khoHang) {
            String haystack = chuanHoa(p.get("ten") + " " + p.get("thuongHieu") + " " + p.get("slug"));
            for (String w : words) {
                if (w.length() >= 4 && haystack.contains(w)) {
                    theoTu.add(p);
                    break;
                }
            }
        }
        return theoTu;
    }

    private String batThuongHieu(String input, List<Map<String, Object>> khoHang) {
        Map<String, String> alias = new LinkedHashMap<>();
        alias.put("patek philippe", "patek philippe");
        alias.put("patek", "patek philippe");
        alias.put("audemars piguet", "audemars piguet");
        alias.put("audemars", "audemars piguet");
        alias.put("vacheron constantin", "vacheron constantin");
        alias.put("vacheron", "vacheron constantin");
        alias.put("jaeger-lecoultre", "jaeger-lecoultre");
        alias.put("jaeger lecoultre", "jaeger-lecoultre");
        alias.put("jlc", "jaeger-lecoultre");
        alias.put("richard mille", "richard mille");
        alias.put("franck muller", "franck muller");
        alias.put("frederique constant", "frederique constant");
        alias.put("harry winston", "harry winston");
        alias.put("van cleef", "van cleef");
        alias.put("ulysse nardin", "ulysse nardin");
        alias.put("mb&f", "mb&f");
        alias.put("mbf", "mb&f");
        alias.put("rolex", "rolex");
        alias.put("omega", "omega");
        alias.put("hublot", "hublot");
        alias.put("cartier", "cartier");
        alias.put("breguet", "breguet");
        alias.put("chopard", "chopard");
        alias.put("piaget", "piaget");
        alias.put("velora", "velora");

        for (Map.Entry<String, String> e : alias.entrySet()) {
            if (chuaCumTu(input, e.getKey())) {
                return e.getValue();
            }
        }

        Set<String> brands = new LinkedHashSet<>();
        for (Map<String, Object> p : khoHang) {
            String th = chuanHoa(String.valueOf(p.get("thuongHieu")));
            if (!th.isBlank()) brands.add(th);
        }
        List<String> sorted = new ArrayList<>(brands);
        sorted.sort((a, b) -> Integer.compare(b.length(), a.length()));
        for (String th : sorted) {
            if (chuaCumTu(input, th)) return th;
        }
        return null;
    }

    private boolean chuaCumTu(String text, String key) {
        if (text == null || key == null || key.isBlank()) return false;
        return text.equals(key)
                || text.startsWith(key + " ")
                || text.endsWith(" " + key)
                || text.contains(" " + key + " ")
                || text.contains(key);
    }

    private String chuanHoa(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .trim();
        return n.replace('&', ' ')
                .replaceAll("[^a-z0-9\\s-]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private Map<String, Object> getSmartFallbackAnswer(String cauHoi, List<Map<String, Object>> khoHang) {
        Map<String, Object> result = new HashMap<>();
        String input = chuanHoa(cauHoi);

        if (input.matches(".*(chao|hello|hi|shop).*") && input.length() < 25) {
            result.put("reply", "Dạ chào Quý khách! Velora Clock rất hân hạnh được hỗ trợ. Quý khách đang quan tâm thương hiệu nào ạ?");
            result.put("products", new ArrayList<>());
            return result;
        }

        List<Map<String, Object>> matched = locSanPhamTheoYeuCau(cauHoi, khoHang);
        if (!matched.isEmpty()) {
            result.put("reply", "Dạ, Velora xin gửi Quý khách các tuyệt tác đang có tại cửa hàng:");
            result.put("products", matched);
        } else {
            result.put("reply", "Dạ, hiện kho Velora chưa có mẫu khớp đúng yêu cầu. Quý khách cho biết thương hiệu hoặc tầm giá giúp ạ.");
            result.put("products", new ArrayList<>());
        }
        return result;
    }
}