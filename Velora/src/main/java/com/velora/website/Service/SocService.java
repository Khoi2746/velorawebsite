package com.velora.website.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    
    private final SimpMessagingTemplate messagingTemplate;

    public void createAndBroadcastAlert(String ip, String loaiTanCong, String mucDo, String moTa) {
        CanhBaoAnNinh alert = new CanhBaoAnNinh();
        alert.setDiaChiIP(ip != null ? ip : "127.0.0.1");
        alert.setLoaiTanCong(loaiTanCong); 
        alert.setMucDoNguyHiem(mucDo);       
        alert.setMoTaChiTiet(moTa);
        alert.setNgayTao(new Date());
        alert.setDaXuLy(false);

        CanhBaoAnNinh savedAlert = canhBaoRepo.save(alert);
        messagingTemplate.convertAndSend("/topic/soc-alerts", savedAlert);
    }

    public List<CanhBaoAnNinh> getRecentAlerts() {
        return canhBaoRepo.findTop50ByOrderByNgayTaoDesc();
    }

    public List<NhatKyDangNhap> getRecentLogs() {
        return nhatKyRepo.findTop100ByOrderByThoiGianDangNhapDesc();
    }

    public void blockIp(String ip, Integer maCanhBao) {
        if (!blacklistRepo.existsByDiaChiIP(ip)) {
            DanhSachDenIP blacklist = new DanhSachDenIP();
            blacklist.setDiaChiIP(ip);
            blacklist.setLyDoChan("Cô lập thủ công từ SOC Dashboard");
            blacklist.setHinhThucChan("VINH_VIEN");
            blacklist.setThoiGianBatDau(new Date());
            blacklistRepo.save(blacklist);
        }
        
        if (maCanhBao != null) {
            canhBaoRepo.findById(maCanhBao).ifPresent(alert -> {
                alert.setDaXuLy(true);
                canhBaoRepo.save(alert);
            });
        }
    }

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
            switch (action.toUpperCase()) {
                case "LOCK": 
                    user.setTrangThai("BI_KHOA"); 
                    // Nếu admin bấm nút Khóa -> Khóa vĩnh viễn (100 năm)
                    user.setThoiGianCamBinhLuan(LocalDateTime.now().plusYears(100));
                    break;
                case "UNLOCK": 
                    user.setTrangThai("HOAT_DONG"); 
                    user.setSoLanViPham(0); 
                    // 🔥 CHÌA KHÓA Ở ĐÂY: Bắt buộc phải rửa sạch án phạt cũ
                    user.setThoiGianCamBinhLuan(null); 
                    break;
                case "BAN_COMMENT": 
                    // Cấm 3 phút
                    user.setThoiGianCamBinhLuan(LocalDateTime.now().plusMinutes(3)); 
                    break;
                case "UNBAN_COMMENT": 
                    user.setThoiGianCamBinhLuan(null); 
                    break;
            }
            nguoiDungRepo.save(user);
        });
    }

    public void deleteComment(Integer commentId) {
        danhGiaRepo.deleteById(commentId);
    }
}