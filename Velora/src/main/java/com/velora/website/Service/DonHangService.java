package com.velora.website.Service;

import com.velora.website.Entity.DonHang;
import com.velora.website.Entity.DoanhThuNgay;
import com.velora.website.Entity.DoanhThuThang;
import com.velora.website.Repository.DonHangRepository;
import com.velora.website.Repository.DoanhThuNgayRepository;
import com.velora.website.Repository.DoanhThuThangRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class DonHangService {

    private final DonHangRepository donHangRepository;
    private final DoanhThuNgayRepository doanhThuNgayRepository;
    private final DoanhThuThangRepository doanhThuThangRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public DonHangService(DonHangRepository donHangRepository, 
                          DoanhThuNgayRepository doanhThuNgayRepository,
                          DoanhThuThangRepository doanhThuThangRepository,
                          SimpMessagingTemplate messagingTemplate) {
        this.donHangRepository = donHangRepository;
        this.doanhThuNgayRepository = doanhThuNgayRepository;
        this.doanhThuThangRepository = doanhThuThangRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public List<DonHang> layTatCaDonHang() {
        return donHangRepository.findAll();
    }

    @Transactional
    public DonHang taoDonHangMoi(DonHang donHangMoi) {
        DonHang savedDonHang = donHangRepository.save(donHangMoi);

        if ("DA_THANH_TOAN".equalsIgnoreCase(savedDonHang.getTrangThaiThanhToan())) {
            capNhatBangThongKe(savedDonHang);
            messagingTemplate.convertAndSend("/topic/statistics", "UPDATE_STATS");
        }

        return savedDonHang;
    }

    @Transactional
    public DonHang capNhatTrangThaiDonHang(Integer idDonHang, String trangThaiDonHang, String trangThaiThanhToan) {
        DonHang donHang = donHangRepository.findById(idDonHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng mã: " + idDonHang));

        boolean wasThanhCongTruocDo = "DA_GIAO".equalsIgnoreCase(donHang.getTrangThaiDonHang()) 
                                   || "DA_THANH_TOAN".equalsIgnoreCase(donHang.getTrangThaiThanhToan());

        if (trangThaiDonHang != null && !trangThaiDonHang.isEmpty()) {
            donHang.setTrangThaiDonHang(trangThaiDonHang);
        }
        if (trangThaiThanhToan != null && !trangThaiThanhToan.isEmpty()) {
            donHang.setTrangThaiThanhToan(trangThaiThanhToan);
        }

        DonHang updatedDonHang = donHangRepository.save(donHang);

        boolean isGiaoThanhCong = "DA_GIAO".equalsIgnoreCase(trangThaiDonHang) || "HOAN_TAT".equalsIgnoreCase(trangThaiDonHang);
        boolean isThanhToanXong = "DA_THANH_TOAN".equalsIgnoreCase(trangThaiThanhToan);

        if ((isGiaoThanhCong || isThanhToanXong) && !wasThanhCongTruocDo) {
            capNhatBangThongKe(updatedDonHang);
            System.out.println("🔥 Có biến động doanh thu! Đã chốt sổ Database và đang phát tín hiệu cập nhật biểu đồ...");
            messagingTemplate.convertAndSend("/topic/statistics", "UPDATE_STATS");
        }

        return updatedDonHang;
    }

    private void capNhatBangThongKe(DonHang donHang) {
        LocalDate ngayHienTai = LocalDate.now();
        int thang = ngayHienTai.getMonthValue();
        int nam = ngayHienTai.getYear();

        BigDecimal tienDonHang = donHang.getTongTien();
        int soLuongSp = 1; 

        // 1. Xử lý bảng DoanhThuNgay (Dùng vòng lặp quét danh sách thay vì findByNgay để né lỗi Repository)
        List<DoanhThuNgay> tatCaDoanhThuNgay = doanhThuNgayRepository.findAll();
        DoanhThuNgay doanhThuNgay = null;
        
        for (DoanhThuNgay dt : tatCaDoanhThuNgay) {
            if (dt.getNgay() != null && dt.getNgay().equals(ngayHienTai)) {
                doanhThuNgay = dt;
                break;
            }
        }

        if (doanhThuNgay == null) {
            doanhThuNgay = new DoanhThuNgay();
            doanhThuNgay.setNgay(ngayHienTai);
            doanhThuNgay.setTongDoanhThu(BigDecimal.ZERO);
            doanhThuNgay.setSoDonHangThanhCong(0);
            doanhThuNgay.setSoSanPhamBanRa(0);
        }
        
        doanhThuNgay.setTongDoanhThu(doanhThuNgay.getTongDoanhThu().add(tienDonHang));
        doanhThuNgay.setSoDonHangThanhCong(doanhThuNgay.getSoDonHangThanhCong() + 1);
        doanhThuNgay.setSoSanPhamBanRa(doanhThuNgay.getSoSanPhamBanRa() + soLuongSp);
        doanhThuNgayRepository.save(doanhThuNgay);

        // 2. Xử lý bảng DoanhThuThang (Dùng vòng lặp quét danh sách thay vì findByThangAndNam)
        List<DoanhThuThang> tatCaDoanhThuThang = doanhThuThangRepository.findAll();
        DoanhThuThang doanhThuThang = null;

        for (DoanhThuThang dt : tatCaDoanhThuThang) {
            if (dt.getThang() == thang && dt.getNam() == nam) {
                doanhThuThang = dt;
                break;
            }
        }

        if (doanhThuThang == null) {
            doanhThuThang = new DoanhThuThang();
            doanhThuThang.setThang(thang);
            doanhThuThang.setNam(nam);
            doanhThuThang.setTongDoanhThu(BigDecimal.ZERO);
            doanhThuThang.setSoDonHangThanhCong(0);
            doanhThuThang.setSoSanPhamBanRa(0);
        }
                
        doanhThuThang.setTongDoanhThu(doanhThuThang.getTongDoanhThu().add(tienDonHang));
        doanhThuThang.setSoDonHangThanhCong(doanhThuThang.getSoDonHangThanhCong() + 1);
        doanhThuThang.setSoSanPhamBanRa(doanhThuThang.getSoSanPhamBanRa() + soLuongSp);
        doanhThuThangRepository.save(doanhThuThang);
    }
}