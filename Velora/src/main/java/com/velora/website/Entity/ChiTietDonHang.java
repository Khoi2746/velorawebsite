package com.velora.website.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "ChiTietDonHang")
@Data
public class ChiTietDonHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaChiTietDonHang")
    private Integer maChiTietDonHang;

    @Column(name = "SoLuong")
    private Integer soLuong;

    @Column(name = "GiaLucMua")
    private BigDecimal giaLucMua;

    // 🔥 LIÊN KẾT NGƯỢC LẠI VỚI BẢNG ĐƠN HÀNG
    @ManyToOne
    @JoinColumn(name = "MaDonHang")
    @JsonIgnoreProperties("chiTietDonHangs")
    private DonHang donHang;

    // 🔥 LIÊN KẾT VỚI BẢNG SẢN PHẨM ĐỂ LẤY ĐƯỢC TÊN VÀ ẢNH ĐỒNG HỒ
    @ManyToOne
    @JoinColumn(name = "MaSanPham")
    private SanPham sanPham;
}