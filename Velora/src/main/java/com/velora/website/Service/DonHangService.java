package com.velora.website.Service; // Khai báo package chứa tầng xử lý nghiệp vụ logic (Business Service)

import com.velora.website.Entity.DonHang;
import com.velora.website.Entity.DoanhThuNgay;
import com.velora.website.Entity.DoanhThuThang;
import com.velora.website.Repository.DonHangRepository;
import com.velora.website.Repository.DoanhThuNgayRepository;
import com.velora.website.Repository.DoanhThuThangRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate; // Công cụ gửi tin nhắn WebSocket realtime
import org.springframework.stereotype.Service; // Annotation đánh dấu đây là Spring Service Bean
import org.springframework.transaction.annotation.Transactional; // Đảm bảo tính toàn vẹn dữ liệu trong giao dịch CSDL

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service // Khai báo Service để Spring tự động quản lý vòng đời và tiêm (Inject) vào Controller
public class DonHangService {

    // Khai báo các Repository và WebSocket template cần sử dụng
    private final DonHangRepository donHangRepository;
    private final DoanhThuNgayRepository doanhThuNgayRepository;
    private final DoanhThuThangRepository doanhThuThangRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Constructor Injection: Tiêm các dependency vào Service
    public DonHangService(DonHangRepository donHangRepository, 
                          DoanhThuNgayRepository doanhThuNgayRepository,
                          DoanhThuThangRepository doanhThuThangRepository,
                          SimpMessagingTemplate messagingTemplate) {
        this.donHangRepository = donHangRepository;
        this.doanhThuNgayRepository = doanhThuNgayRepository;
        this.doanhThuThangRepository = doanhThuThangRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Hàm lấy toàn bộ danh sách đơn hàng trong hệ thống
     */
    public List<DonHang> layTatCaDonHang() {
        return donHangRepository.findAll();
    }

    /**
     * Hàm tạo đơn hàng mới và tự động cộng dồn doanh thu nếu đơn đó đã thanh toán ngay
     */
    @Transactional // Đảm bảo lưu đơn và cập nhật bảng doanh thu thành công cùng lúc, nếu lỗi sẽ tự Rollback
    public DonHang taoDonHangMoi(DonHang donHangMoi) {
        DonHang savedDonHang = donHangRepository.save(donHangMoi); // Lưu đơn vào bảng DonHang

        // Nếu đơn hàng đã thanh toán ngay lúc tạo (VD: Chuyển khoản QR xong)
        if ("DA_THANH_TOAN".equalsIgnoreCase(savedDonHang.getTrangThaiThanhToan())) {
            capNhatBangThongKe(savedDonHang); // Cộng dồn tiền vào bảng DoanhThuNgay và DoanhThuThang
            messagingTemplate.convertAndSend("/topic/statistics", "UPDATE_STATS"); // Bắn tín hiệu WebSocket cho Admin load lại biểu đồ
        }

        return savedDonHang;
    }

    /**
     * Hàm cập nhật trạng thái giao hàng hoặc trạng thái thanh toán của đơn hàng
     */
    @Transactional
    public DonHang capNhatTrangThaiDonHang(Integer idDonHang, String trangThaiDonHang, String trangThaiThanhToan) {
        // Tìm đơn theo ID, nếu không thấy thì ném ra ngoại lệ RuntimeException
        DonHang donHang = donHangRepository.findById(idDonHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng mã: " + idDonHang));

        // Kiểm tra xem đơn này trước đó đã từng được tính doanh thu chưa (đã giao hoặc đã thanh toán trước đó)
        boolean wasThanhCongTruocDo = "DA_GIAO".equalsIgnoreCase(donHang.getTrangThaiDonHang()) 
                                   || "DA_THANH_TOAN".equalsIgnoreCase(donHang.getTrangThaiThanhToan());

        // Cập nhật trạng thái đơn hàng nếu có truyền vào
        if (trangThaiDonHang != null && !trangThaiDonHang.isEmpty()) {
            donHang.setTrangThaiDonHang(trangThaiDonHang);
        }
        // Cập nhật trạng thái thanh toán nếu có truyền vào
        if (trangThaiThanhToan != null && !trangThaiThanhToan.isEmpty()) {
            donHang.setTrangThaiThanhToan(trangThaiThanhToan);
        }

        DonHang updatedDonHang = donHangRepository.save(donHang); // Lưu trạng thái mới vào CSDL

        boolean isGiaoThanhCong = "DA_GIAO".equalsIgnoreCase(trangThaiDonHang) || "HOAN_TAT".equalsIgnoreCase(trangThaiDonHang);
        boolean isThanhToanXong = "DA_THANH_TOAN".equalsIgnoreCase(trangThaiThanhToan);

        // Chỉ cộng doanh thu khi đơn hàng chuyển sang thành công LẦN ĐẦU TIÊN (tránh cộng trùng lặp)
        if ((isGiaoThanhCong || isThanhToanXong) && !wasThanhCongTruocDo) {
            capNhatBangThongKe(updatedDonHang);
            System.out.println("🔥 Có biến động doanh thu! Đã chốt sổ Database và đang phát tín hiệu cập nhật biểu đồ...");
            messagingTemplate.convertAndSend("/topic/statistics", "UPDATE_STATS"); // Bắn tín hiệu WebSocket realtime
        }

        return updatedDonHang;
    }

    /**
     * Hàm nội bộ tự động tính toán và lưu doanh thu vào 2 bảng DoanhThuNgay & DoanhThuThang
     */
    private void capNhatBangThongKe(DonHang donHang) {
        LocalDate ngayHienTai = LocalDate.now(); // Lấy ngày hôm nay
        int thang = ngayHienTai.getMonthValue(); // Lấy tháng hiện tại
        int nam = ngayHienTai.getYear();         // Lấy năm hiện tại

        BigDecimal tienDonHang = donHang.getTongTien(); // Số tiền của đơn hàng
        int soLuongSp = 1;                              // Số lượng sản phẩm mặc định là 1

        // ---------------------------------------------------------------------
        // 1. Xử lý bảng DoanhThuNgay (Quét danh sách tìm ngày hôm nay)
        // ---------------------------------------------------------------------
        List<DoanhThuNgay> tatCaDoanhThuNgay = doanhThuNgayRepository.findAll();
        DoanhThuNgay doanhThuNgay = null;
        
        for (DoanhThuNgay dt : tatCaDoanhThuNgay) {
            if (dt.getNgay() != null && dt.getNgay().equals(ngayHienTai)) {
                doanhThuNgay = dt; // Đã có bản ghi của ngày hôm nay
                break;
            }
        }

        // Nếu hôm nay là đơn đầu tiên (chưa có bản ghi ngày hôm nay) -> Tạo mới
        if (doanhThuNgay == null) {
            doanhThuNgay = new DoanhThuNgay();
            doanhThuNgay.setNgay(ngayHienTai);
            doanhThuNgay.setTongDoanhThu(BigDecimal.ZERO);
            doanhThuNgay.setSoDonHangThanhCong(0);
            doanhThuNgay.setSoSanPhamBanRa(0);
        }
        
        // Cộng dồn tiền, số đơn và số sản phẩm bán ra trong ngày
        doanhThuNgay.setTongDoanhThu(doanhThuNgay.getTongDoanhThu().add(tienDonHang));
        doanhThuNgay.setSoDonHangThanhCong(doanhThuNgay.getSoDonHangThanhCong() + 1);
        doanhThuNgay.setSoSanPhamBanRa(doanhThuNgay.getSoSanPhamBanRa() + soLuongSp);
        doanhThuNgayRepository.save(doanhThuNgay); // Lưu vào CSDL

        // ---------------------------------------------------------------------
        // 2. Xử lý bảng DoanhThuThang (Quét danh sách tìm tháng/năm hiện tại)
        // ---------------------------------------------------------------------
        List<DoanhThuThang> tatCaDoanhThuThang = doanhThuThangRepository.findAll();
        DoanhThuThang doanhThuThang = null;

        for (DoanhThuThang dt : tatCaDoanhThuThang) {
            if (dt.getThang() == thang && dt.getNam() == nam) {
                doanhThuThang = dt; // Đã có bản ghi của tháng này
                break;
            }
        }

        // Nếu tháng này chưa có bản ghi nào -> Tạo mới
        if (doanhThuThang == null) {
            doanhThuThang = new DoanhThuThang();
            doanhThuThang.setThang(thang);
            doanhThuThang.setNam(nam);
            doanhThuThang.setTongDoanhThu(BigDecimal.ZERO);
            doanhThuThang.setSoDonHangThanhCong(0);
            doanhThuThang.setSoSanPhamBanRa(0);
        }
                
        // Cộng dồn tiền, số đơn và số sản phẩm bán ra trong tháng
        doanhThuThang.setTongDoanhThu(doanhThuThang.getTongDoanhThu().add(tienDonHang));
        doanhThuThang.setSoDonHangThanhCong(doanhThuThang.getSoDonHangThanhCong() + 1);
        doanhThuThang.setSoSanPhamBanRa(doanhThuThang.getSoSanPhamBanRa() + soLuongSp);
        doanhThuThangRepository.save(doanhThuThang); // Lưu vào CSDL
    }

    // -------------------------------------------------------------------------
    // CÁC KỊCH BẢN THAY THẾ & MỞ RỘNG LOGIC DOANH THU:
    // -------------------------------------------------------------------------

    // 1. Trừ doanh thu khi đơn hàng bị HỦY sau khi đã giao/thanh toán (Hoàn tiền):
    // public void truDoanhThuKhiHuy(DonHang donHang) {
    //     LocalDate ngayHienTai = LocalDate.now();
    //     // Tìm DoanhThuNgay và trừ: dtNgay.setTongDoanhThu(dtNgay.getTongDoanhThu().subtract(donHang.getTongTien()));
    //     // dtNgay.setSoDonHangThanhCong(dtNgay.getSoDonHangThanhCong() - 1);
    // }

    // 2. Tính chính xác số lượng sản phẩm từ bảng ChiTietDonHang thay vì gán mặc định bằng 1:
    // int soLuongSp = 0;
    // if (donHang.getChiTietDonHangs() != null) {
    //     for (ChiTietDonHang ct : donHang.getChiTietDonHangs()) {
    //         soLuongSp += ct.getSoLuong();
    //     }
    // }
    // if (soLuongSp == 0) soLuongSp = 1;
}