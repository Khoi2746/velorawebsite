package com.velora.website.Entity;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@Entity
@Table(name = "lich_hen")
public class LichHen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_khach_hang", nullable = false)
    private String tenKhachHang;

    @Column(name = "so_dien_thoai", nullable = false)
    private String soDienThoai;

    @Column(name = "email")
    private String email;

    @Column(name = "ngay_hen", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd") // BỔ SUNG DÒNG NÀY ĐỂ BẮT LỖI NGÀY THÁNG
    private LocalDate ngayHen;

    @Column(name = "thoi_gian")
    private String thoiGian;

    @Column(name = "ghi_chu", columnDefinition = "NVARCHAR(MAX)")
    private String ghiChu;

    @ManyToOne
    @JoinColumn(name = "id_san_pham")
    private SanPham sanPham;

    @Column(name = "trang_thai")
    private Integer trangThai = 0; // 0: Chờ xác nhận, 1: Đã xác nhận, 2: Hoàn thành, 3: Đã hủy

    
}