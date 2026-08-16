package com.velora.website.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; 
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List; 

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

    @Column(name = "Email")
    private String email;

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

    @Column(name = "GhiChuDonHang")
    private String ghiChuDonHang;

    @Column(name = "NgayTao")
    private LocalDateTime ngayTao;

    @Column(name = "NgayCapNhat")
    private LocalDateTime ngayCapNhat;

    // ======================================================================
    // 🔥 ĐOẠN CODE THÊM VÀO ĐỂ KÉO DANH SÁCH SẢN PHẨM KHÁCH MUA LÊN VUEJS
    // ======================================================================
    @OneToMany(mappedBy = "donHang", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnoreProperties("donHang") 
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ChiTietDonHang> chiTietDonHangs;

    @PrePersist
    protected void onCreate() {
        this.ngayTao = LocalDateTime.now();
        this.ngayCapNhat = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.ngayCapNhat = LocalDateTime.now();
    }
}