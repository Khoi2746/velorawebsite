package com.velora.website.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "ChiTietPhieuNhap")
@Data
public class ChiTietPhieuNhap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaChiTietPhieuNhap")
    private Integer maChiTietPhieuNhap;

    @Column(name = "MaPhieuNhap")
    private Integer maPhieuNhap;

    @Column(name = "MaSanPham")
    private Integer maSanPham;

    @Column(name = "SoLuongNhap")
    private Integer soLuongNhap;

    @Column(name = "GiaNhap")
    private BigDecimal giaNhap;
}