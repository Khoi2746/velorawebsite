package com.velora.website.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// =========================================================================
// [PHẦN 1: KHAI BÁO ENTITY VÀ ÁNH XẠ BẢNG CƠ SỞ DỮ LIỆU]
// =========================================================================
@Entity                                                                  // Khai báo lớp này là một thực thể JPA Entity được quản lý bởi Hibernate
@Table(name = "YeuCauHoanTien")                                          // Ánh xạ tương ứng với bảng 'YeuCauHoanTien' trong CSDL
@Data                                                                    // Lombok: Tự động tạo Getter, Setter, toString, equals và hashCode
public class YeuCauHoanTien {

    // =====================================================================
    // [PHẦN 2: KHÓA CHÍNH VÀ ĐỊNH DANH ĐƠN HÀNG]
    // =====================================================================

    @Id                                                                  // Đánh dấu thuộc tính là Khóa chính (Primary Key) của bảng
    @GeneratedValue(strategy = GenerationType.IDENTITY)                  // Tự động tăng giá trị ID trong CSDL (Identity / Auto Increment)
    @Column(name = "Id")                                                 // Tên cột khóa chính trong bảng CSDL
    private Integer id;                                                  // ID bản ghi yêu cầu hoàn tiền

    @Column(name = "MaDonHangCode")                                      // Mã đơn hàng hiển thị (Ví dụ: VELORA1001)
    private String maDonHangCode;

    // =====================================================================
    // [PHẦN 3: THÔNG TIN KHÁCH HÀNG LIÊN HỆ]
    // =====================================================================

    @Column(name = "HoTen")                                              // Họ và tên người gửi yêu cầu hoàn tiền
    private String hoTen;

    @Column(name = "SoDienThoai")                                        // Số điện thoại liên hệ của khách hàng
    private String soDienThoai;

    @Column(name = "Email")                                              // Email nhận mã OTP xác thực và nhận thông báo kết quả duyệt
    private String email;

    @Column(name = "DiaChi")                                             // Địa chỉ nhận hàng / thu hồi sản phẩm
    private String diaChi;

    // =====================================================================
    // [PHẦN 4: THÔNG TIN TÀI KHOẢN NGÂN HÀNG THỤ HƯỞNG]
    // =====================================================================

    @Column(name = "TenNganHang")                                        // Tên ngân hàng nhận tiền hoàn (Ví dụ: MBBank, Vietcombank, Techcombank)
    private String tenNganHang;

    @Column(name = "SoTaiKhoan")                                         // Số tài khoản ngân hàng của khách
    private String soTaiKhoan;

    @Column(name = "TenChuTaiKhoan")                                     // Tên chủ sở hữu tài khoản ngân hàng (Viết hoa không dấu)
    private String tenChuTaiKhoan;

    // =====================================================================
    // [PHẦN 5: LÝ DO HOÀN HÀNG & MINH CHỨNG HÌNH ẢNH]
    // =====================================================================

    @Column(name = "LyDo")                                               // Lý do khách hàng muốn trả hàng và bồi hoàn tiền
    private String lyDo;

    // DÒNG MẶC ĐỊNH: Lưu danh sách mảng ảnh dưới dạng chuỗi JSON văn bản dài (NVARCHAR(MAX) trong SQL Server)
    @Column(name = "DanhSachAnh", columnDefinition = "NVARCHAR(MAX)")
    private String danhSachAnh;
    // THAY THẾ: Dùng cho MySQL / PostgreSQL (kiểu TEXT hoặc LONGTEXT):
    // @Column(name = "DanhSachAnh", columnDefinition = "LONGTEXT")
    // private String danhSachAnh;

    // =====================================================================
    // [PHẦN 6: TRẠNG THÁI KIỂM DUYỆT & GHI CHÚ QUẢN TRỊ VIÊN]
    // =====================================================================

    @Column(name = "TrangThai")                                          // Trạng thái duyệt: 'CHO_DUYET', 'DA_HOAN_TIEN', 'TU_CHOI_HOAN'
    private String trangThai;

    @Column(name = "GhiChuAdmin")                                        // Lưu vết lý do từ chối hoặc phản hồi từ Quản trị viên
    private String ghiChuAdmin;

    // =====================================================================
    // [PHẦN 7: THÔNG TIN TÀI CHÍNH CỦA ĐƠN HÀNG GỐC]
    // =====================================================================

    // DÒNG MẶC ĐỊNH: Số tiền cần hoàn trả kiểu BigDecimal (chính xác tuyệt đối cho số tiền lớn)
    @Column(name = "TongTien")
    private BigDecimal tongTien;
    // THAY THẾ: Sử dụng kiểu Double nếu hệ thống tính toán đơn giản:
    // @Column(name = "TongTien")
    // private Double tongTien;

    @Column(name = "PhuongThucThanhToan")                                // Phương thức thanh toán ban đầu (VNPAY, CHUYEN_KHOAN_QR, COD)
    private String phuongThucThanhToan;

    // =====================================================================
    // [PHẦN 8: THỜI GIAN TẠO YÊU CẦU & HOOKS VÒNG ĐỜI ENTITY]
    // =====================================================================

    @Column(name = "NgayTao")                                            // Thời điểm gửi yêu cầu hoàn tiền
    private LocalDateTime ngayTao;

    /**
     * Tự động chạy và gán ngày giờ hiện tại của hệ thống trước khi INSERT vào CSDL
     */
    @PrePersist
    protected void onCreate() {
        this.ngayTao = LocalDateTime.now();                              // Gán ngày giờ tạo bằng thời điểm thực tế
    }
}