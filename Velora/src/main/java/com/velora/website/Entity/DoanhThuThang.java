package com.velora.website.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "DoanhThuThang")
@Data
public class DoanhThuThang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDoanhThuThang")
    private Integer maDoanhThuThang;

    @Column(name = "Thang")
    private Integer thang;

    @Column(name = "Nam")
    private Integer nam;

    @Column(name = "TongDoanhThu")
    private BigDecimal tongDoanhThu;

    @Column(name = "SoDonHangThanhCong")
    private Integer soDonHangThanhCong;

    @Column(name = "SoSanPhamBanRa")
    private Integer soSanPhamBanRa;

    @Column(name = "NgayCapNhat")
    private LocalDateTime ngayCapNhat;
}