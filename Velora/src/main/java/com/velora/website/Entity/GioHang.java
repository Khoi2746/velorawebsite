package com.velora.website.Entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "GioHang")
public class GioHang {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaGioHang")
    private Integer maGioHang;

    @Column(name = "MaNguoiDung")
    private Integer maNguoiDung;

    @Column(name = "MaSanPham")
    private Integer maSanPham;

    @Column(name = "SoLuong")
    private Integer soLuong;

    @Column(name = "NgayThem", insertable = false, updatable = false)
    private Date ngayThem;

    // ==========================================
    // GETTERS
    // ==========================================
    public Integer getMaGioHang() {
        return maGioHang;
    }

    public Integer getMaNguoiDung() {
        return maNguoiDung;
    }

    public Integer getMaSanPham() {
        return maSanPham;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public Date getNgayThem() {
        return ngayThem;
    }

    // ==========================================
    // SETTERS
    // ==========================================
    public void setMaGioHang(Integer maGioHang) {
        this.maGioHang = maGioHang;
    }

    public void setMaNguoiDung(Integer maNguoiDung) {
        this.maNguoiDung = maNguoiDung;
    }

    public void setMaSanPham(Integer maSanPham) {
        this.maSanPham = maSanPham;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public void setNgayThem(Date ngayThem) {
        this.ngayThem = ngayThem;
    }
}