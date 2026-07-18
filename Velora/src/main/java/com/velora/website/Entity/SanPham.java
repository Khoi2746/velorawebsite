package com.velora.website.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "SanPham")
@Data
public class SanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maSanPham;

    private String tenSanPham;
    private String duongDanSlug;
    private BigDecimal giaBan;
    private Integer soLuongTonKho;
    private String anhDaiDien;
    private String loaiMay;
    private String chatLieuDay;
    private String chatLieuKinh;
    private String duongKinhMat;
    private String doChongNuoc;
    private String moTaChiTiet;
    private String trangThai;
    private String gioiTinh;

    public String getGioiTinh() {
    return gioiTinh;
}

public void setGioiTinh(String gioiTinh) {
    this.gioiTinh = gioiTinh;
}

    @ManyToOne
    @JoinColumn(name = "maThuongHieu")
    private ThuongHieu thuongHieu; // Quan hệ với bảng Thương Hiệu

    @ManyToOne
    @JoinColumn(name = "maDanhMuc")
    private DanhMuc danhMuc;

    @ManyToOne
@JoinColumn(name = "ma_loai") // Khớp chính xác với tên cột ma_loai trong SQL
private LoaiSanPham loaiSanPham;
}
