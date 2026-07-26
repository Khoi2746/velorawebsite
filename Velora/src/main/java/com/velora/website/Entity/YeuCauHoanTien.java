package com.velora.website.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "YeuCauHoanTien")
@Data
public class YeuCauHoanTien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "MaDonHangCode")
    private String maDonHangCode;

    @Column(name = "HoTen")
    private String hoTen;

    @Column(name = "SoDienThoai")
    private String soDienThoai;

    @Column(name = "Email")
    private String email;

    @Column(name = "DiaChi")
    private String diaChi;

    @Column(name = "TenNganHang")
    private String tenNganHang;

    @Column(name = "SoTaiKhoan")
    private String soTaiKhoan;

    @Column(name = "TenChuTaiKhoan")
    private String tenChuTaiKhoan;

    @Column(name = "LyDo")
    private String lyDo;

    @Column(name = "DanhSachAnh", columnDefinition = "NVARCHAR(MAX)")
    private String danhSachAnh;

    @Column(name = "TrangThai")
    private String trangThai; // CHO_DUYET, DA_HOAN_TIEN, TU_CHOI_HOAN

    @Column(name = "GhiChuAdmin")
    private String ghiChuAdmin;

    @Column(name = "TongTien")
    private BigDecimal tongTien;

    @Column(name = "PhuongThucThanhToan")
    private String phuongThucThanhToan;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() {
        this.ngayTao = LocalDateTime.now();
    }
}