package com.velora.website.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "DoanhThuNgay")
@Data
public class DoanhThuNgay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDoanhThuNgay")
    private Integer maDoanhThuNgay;

    @Column(name = "Ngay")
    private LocalDate ngay;

    @Column(name = "TongDoanhThu")
    private BigDecimal tongDoanhThu;

    @Column(name = "SoDonHangThanhCong")
    private Integer soDonHangThanhCong;

    @Column(name = "SoSanPhamBanRa")
    private Integer soSanPhamBanRa;

    @Column(name = "NgayCapNhat")
    private LocalDateTime ngayCapNhat;
}