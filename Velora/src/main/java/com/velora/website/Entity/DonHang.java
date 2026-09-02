package com.velora.website.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; 
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List; 

// =========================================================================
// [PHẦN 1: KHAI BÁO ENTITY VÀ LIÊN KẾT BẢNG CƠ SỞ DỮ LIỆU]
// =========================================================================
@Entity                                                                  // Đánh dấu lớp này là một thực thể JPA (JPA Entity) ánh xạ trực tiếp vào CSDL
@Table(name = "DonHang")                                                 // Tên bảng tương ứng trong SQL Server / MySQL là 'DonHang'
@Data                                                                    // Lombok: Tự động sinh toàn bộ Getter, Setter, toString, equals, hashCode
public class DonHang {

    // =====================================================================
    // [PHẦN 2: KHÓA CHÍNH VÀ CÁC MÃ ĐỊNH DANH]
    // =====================================================================

    @Id                                                                  // Đánh dấu là Khóa chính (Primary Key) của bảng
    @GeneratedValue(strategy = GenerationType.IDENTITY)                  // Giá trị tự động tăng (Auto Increment / Identity trong SQL)
    @Column(name = "MaDonHang")                                          // Tên cột khóa chính trong bảng SQL
    private Integer maDonHang;                                           // Mã đơn hàng dạng số nguyên (ID nội bộ hệ thống)

    @Column(name = "MaNguoiDung")                                        // ID của khách hàng đặt đơn (liên kết với bảng NguoiDung)
    private Integer maNguoiDung;

    @Column(name = "MaDonHangCode")                                      // Mã đơn hàng dạng chuỗi hiển thị cho khách (Ví dụ: VELORA1001)
    private String maDonHangCode;

    // =====================================================================
    // [PHẦN 3: THÔNG TIN TIỀN TỆ VÀ THANH TOÁN]
    // =====================================================================

    // DÒNG MẶC ĐỊNH: Tổng tiền đơn hàng kiểu BigDecimal (chính xác tuyệt đối cho tài chính)
    @Column(name = "TongTien")
    private BigDecimal tongTien;
    // THAY THẾ: Sử dụng kiểu Double đơn giản nếu CSDL dùng kiểu float/double:
    // @Column(name = "TongTien")
    // private Double tongTien;

    @Column(name = "PhuongThucThanhToan")                                // Hình thức thanh toán: COD, VNPAY, CHUYEN_KHOAN_QR
    private String phuongThucThanhToan;

    @Column(name = "TrangThaiThanhToan")                                 // Tình trạng tiền: 'CHUA_THANH_TOAN' hoặc 'DA_THANH_TOAN'
    private String trangThaiThanhToan;

    // =====================================================================
    // [PHẦN 4: THÔNG TIN GIAO NHẬN VÀ KHÁCH HÀNG]
    // =====================================================================

    @Column(name = "TenNguoiNhan")                                       // Họ và tên người nhận hàng
    private String tenNguoiNhan;

    @Column(name = "SoDienThoaiGiaoHang")                                // Số điện thoại liên hệ nhận hàng
    private String soDienThoaiGiaoHang;

    @Column(name = "Email")                                              // Email khách hàng nhận thông báo đơn và OTP hoàn tiền
    private String email;

    @Column(name = "DiaChiGiaoHang")                                     // Địa chỉ giao nhận chi tiết
    private String diaChiGiaoHang;

    @Column(name = "GhiChuDonHang")                                      // Ghi chú thêm của khách khi đặt hàng
    private String ghiChuDonHang;

    // =====================================================================
    // [PHẦN 5: TRẠNG THÁI VẬN HÀNH & QUY TRÌNH HỦY ĐƠN]
    // =====================================================================

    @Column(name = "TrangThaiDonHang")                                   // Trạng thái đơn: CHO_XU_LY, CHUAN_BI_HANG, DANG_GIAO, DA_GIAO, DA_HUY, YEU_CAU_HOAN_TIEN
    private String trangThaiDonHang;

    @Column(name = "LyDoHuyDon")                                         // Lưu vết lý do khi khách hàng hoặc admin hủy đơn
    private String lyDoHuyDon;

    // =====================================================================
    // [PHẦN 6: THỜI GIAN HỆ THỐNG (AUDITING TIMESTAMPS)]
    // =====================================================================

    @Column(name = "NgayTao")                                            // Thời điểm đơn hàng được tạo
    private LocalDateTime ngayTao;

    @Column(name = "NgayCapNhat")                                        // Thời điểm đơn hàng được chỉnh sửa/cập nhật trạng thái gần nhất
    private LocalDateTime ngayCapNhat;

    // =====================================================================
    // [PHẦN 7: QUAN HỆ 1 - NHIỀU (1 ĐƠN HÀNG CÓ NHIỀU CHI TIẾT SẢN PHẨM)]
    // =====================================================================

    // DÒNG MẶC ĐỊNH: Tự động kéo luôn danh sách sản phẩm khi load đơn hàng (FetchType.EAGER)
    @OneToMany(mappedBy = "donHang", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnoreProperties("donHang")                                     // Ngăn chặn vòng lặp vô tận khi Jackson tuần tự hóa JSON (Infinite Recursion)
    @ToString.Exclude                                                    // Ngăn chặn Lombok toString() gọi đệ quy gây tràn bộ nhớ StackOverflowError
    @EqualsAndHashCode.Exclude                                           // Loại trừ khỏi hàm so sánh equals/hashCode để tránh đệ quy
    private List<ChiTietDonHang> chiTietDonHangs;                        // Danh sách các sản phẩm và số lượng tương ứng trong đơn

    // THAY THẾ: Sử dụng LAZY để tối ưu hiệu năng nếu chỉ muốn tải sản phẩm khi cần thiết:
    // @OneToMany(mappedBy = "donHang", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    // @JsonIgnoreProperties("donHang")
    // @ToString.Exclude
    // @EqualsAndHashCode.Exclude
    // private List<ChiTietDonHang> chiTietDonHangs;

    // =====================================================================
    // [PHẦN 8: HOOKS VÒNG ĐỜI ENTITY (TỰ ĐỘNG GÁN NGÀY GIỜ HỆ THỐNG)]
    // =====================================================================

    /**
     * Tự động chạy trước khi câu lệnh INSERT được thực thi vào CSDL
     */
    @PrePersist
    protected void onCreate() {
        this.ngayTao = LocalDateTime.now();                              // Gán ngày tạo bằng giờ hiện tại của hệ thống
        this.ngayCapNhat = LocalDateTime.now();                          // Khởi tạo ngày cập nhật bằng ngày tạo
    }

    /**
     * Tự động chạy trước khi câu lệnh UPDATE được thực thi vào CSDL
     */
    @PreUpdate
    protected void onUpdate() {
        this.ngayCapNhat = LocalDateTime.now();                          // Tự động làm mới ngày cập nhật thành thời điểm hiện tại
    }
}