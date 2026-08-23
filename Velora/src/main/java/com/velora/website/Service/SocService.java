package com.velora.website.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate; // 🔥 1. Import WebSocket template
import org.springframework.stereotype.Service;

import com.velora.website.Entity.CanhBaoAnNinh;
import com.velora.website.Entity.DanhGia;
import com.velora.website.Entity.DanhSachDenIP;
import com.velora.website.Entity.NguoiDung;
import com.velora.website.Entity.NhatKyDangNhap;
import com.velora.website.Repository.CanhBaoAnNinhRepository;
import com.velora.website.Repository.DanhGiaRepository;
import com.velora.website.Repository.DanhSachDenIPRepository;
import com.velora.website.Repository.NguoiDungRepository;
import com.velora.website.Repository.NhatKyDangNhapRepository;
import com.velora.website.dto.SocCommentDTO;
import com.velora.website.dto.SocUserDTO;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SocService {

    private final CanhBaoAnNinhRepository canhBaoRepo;
    private final NhatKyDangNhapRepository nhatKyRepo;
    private final DanhSachDenIPRepository blacklistRepo;
    private final NguoiDungRepository nguoiDungRepo;
    private final DanhGiaRepository danhGiaRepo; 
    
    // 🔥 2. Khai báo biến final để Spring tự động tiêm WebSocket template vào
    private final SimpMessagingTemplate messagingTemplate;

    // 🔥 3. HÀM TỰ ĐỘNG TẠO VÀ BẮN CẢNH BÁO REAL-TIME SANG SOC
    public void createAndBroadcastAlert(String ip, String loaiTanCong, String mucDo, String moTa) {
        CanhBaoAnNinh alert = new CanhBaoAnNinh();
        alert.setDiaChiIP(ip != null ? ip : "127.0.0.1");
        alert.setLoaiTanCong(loaiTanCong); // Ví dụ: "XSS_ATTACK", "SQL_INJECTION"
        alert.setMucDoNguyHiem(mucDo);       // Ví dụ: "NGHIEM_TRONG"
        alert.setMoTaChiTiet(moTa);
        alert.setNgayTao(new Date());
        alert.setDaXuLy(false);

        // Lưu vào cơ sở dữ liệu SQL Server
        CanhBaoAnNinh savedAlert = canhBaoRepo.save(alert);

        // Bắn tín hiệu real-time qua WebSocket tới kênh `/topic/soc-alerts` mà trang VueJS đang lắng nghe
        messagingTemplate.convertAndSend("/topic/soc-alerts", savedAlert);
    }

    // Lấy danh sách cảnh báo
    public List<CanhBaoAnNinh> getRecentAlerts() {
        return canhBaoRepo.findTop50ByOrderByNgayTaoDesc();
    }

    // Lấy lịch sử truy cập
    public List<NhatKyDangNhap> getRecentLogs() {
        return nhatKyRepo.findTop100ByOrderByThoiGianDangNhapDesc();
    }

    // Cô lập IP (Đưa vào Blacklist)
    public void blockIp(String ip, Integer maCanhBao) {
        if (!blacklistRepo.existsByDiaChiIP(ip)) {
            DanhSachDenIP blacklist = new DanhSachDenIP();
            blacklist.setDiaChiIP(ip);
            blacklist.setLyDoChan("Cô lập thủ công từ SOC Dashboard");
            blacklist.setHinhThucChan("VINH_VIEN");
            blacklist.setThoiGianBatDau(new Date());
            blacklistRepo.save(blacklist);
        }
        
        // Đánh dấu cảnh báo đã xử lý
        if (maCanhBao != null) {
            canhBaoRepo.findById(maCanhBao).ifPresent(alert -> {
                alert.setDaXuLy(true);
                canhBaoRepo.save(alert);
            });
        }
    }

    // Lấy User Vi Phạm
    public List<SocUserDTO> getLockedUsers() {
        List<NguoiDung> users = nguoiDungRepo.findByTrangThaiOrThoiGianCamBinhLuanIsNotNull("BI_KHOA");
        return users.stream().map(u -> {
            SocUserDTO dto = new SocUserDTO();
            dto.setMaNguoiDung(u.getMaNguoiDung());
            dto.setHoTen(u.getHoTen());
            dto.setEmail(u.getEmail());
            dto.setTrangThai(u.getTrangThai());
            dto.setThoiGianCamBinhLuan(u.getThoiGianCamBinhLuan());
            
            if ("BI_KHOA".equals(u.getTrangThai()) && u.getSoLanViPham() != null && u.getSoLanViPham() >= 5) {
                dto.setLyDoViPham("Xác thực thất bại " + u.getSoLanViPham() + " lần (Anti Brute-force)");
            } else if ("BI_KHOA".equals(u.getTrangThai())) {
                dto.setLyDoViPham("Đình chỉ thủ công bởi Quản trị viên");
            } else {
                dto.setLyDoViPham("Vi phạm tiêu chuẩn cộng đồng (Cấm Comment)");
            }
            dto.setThoiGianViPham(LocalDateTime.now()); 
            return dto;
        }).collect(Collectors.toList());
    }

    // Lấy và phân tích Comment mạo danh/XSS
    public List<SocCommentDTO> getMonitoredComments() {
        List<DanhGia> comments = danhGiaRepo.findAll(); 
        return comments.stream().map(c -> {
            SocCommentDTO dto = new SocCommentDTO();
            dto.setMaDanhGia(c.getMaDanhGia());
            dto.setMaNguoiDung(c.getNguoiDung().getMaNguoiDung());
            dto.setTenNguoiDung(c.getNguoiDung().getHoTen());
            dto.setSoSao(c.getSoSao()); 
            dto.setNoiDung(c.getBinhLuan());
            dto.setTenSanPham(c.getSanPham().getTenSanPham());
            dto.setNgayDanhGia(c.getNgayDanhGia());
            
            String content = c.getBinhLuan() != null ? c.getBinhLuan().toLowerCase() : "";
            boolean isSpam = content.contains("<script>") || content.contains("http") || content.contains("lừa đảo") || content.contains("hàng giả");
            dto.setIsSpam(isSpam);
            
            return dto;
        }).collect(Collectors.toList());
    }

    public void updateUserStatus(Integer userId, String action) {
        nguoiDungRepo.findById(userId).ifPresent(user -> {
            switch (action) {
                case "LOCK": user.setTrangThai("BI_KHOA"); break;
                case "UNLOCK": 
                    user.setTrangThai("HOAT_DONG"); 
                    user.setSoLanViPham(0); 
                    break;
                case "BAN_COMMENT": user.setThoiGianCamBinhLuan(LocalDateTime.now()); break;
                case "UNBAN_COMMENT": user.setThoiGianCamBinhLuan(null); break;
            }
            nguoiDungRepo.save(user);
        });
    }

    public void deleteComment(Integer commentId) {
        danhGiaRepo.deleteById(commentId);
    }
}