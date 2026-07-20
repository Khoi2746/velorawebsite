package com.velora.website.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "BaoHanh")
@Data
public class BaoHanh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maBaoHanh;

    private Integer maNguoiDung;

    private String maDonHangCode;

    private String hoTen;

    private String soDienThoai;

    private String loaiSanPham;

    @Column(columnDefinition = "TEXT")
    private String moTaLoi;

    private String trangThai;

    private LocalDateTime ngayGui;

    @PrePersist
    public void prePersist() {
        if (ngayGui == null)
            ngayGui = LocalDateTime.now();

        if (trangThai == null)
            trangThai = "CHO_XU_LY";
    }
}
