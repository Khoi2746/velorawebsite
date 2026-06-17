package com.velora.website.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "DonHang")
@Data
public class DonHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDonHang")
    private Integer maDonHang;

    @Column(name = "MaNguoiDung")
    private Integer maNguoiDung;

    @Column(name = "MaDonHangCode")
    private String maDonHangCode;

    @Column(name = "TongTien")
    private BigDecimal tongTien;

    @Column(name = "TenNguoiNhan")
    private String tenNguoiNhan;

    @Column(name = "SoDienThoaiGiaoHang")
    private String soDienThoaiGiaoHang;

    @Column(name = "DiaChiGiaoHang")
    private String diaChiGiaoHang;

    @Column(name = "TrangThaiDonHang")
    private String trangThaiDonHang;

    @Column(name = "LyDoHuyDon")
    private String lyDoHuyDon;

    @Column(name = "PhuongThucThanhToan")
    private String phuongThucThanhToan;

    @Column(name = "TrangThaiThanhToan")
    private String trangThaiThanhToan;

    @Column(name = "NgayTao", insertable = false, updatable = false)
    private LocalDateTime ngayTao;

    @Column(name = "NgayCapNhat", insertable = false, updatable = false)
    private LocalDateTime ngayCapNhat;
}