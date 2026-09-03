package com.velora.website.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "DanhGia")
public class DanhGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maDanhGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaSanPham", nullable = true)
    private SanPham sanPham;

    // 🔥 THÊM TRƯỜNG NÀY ĐỂ KÍCH HOẠT HÀM setBaiViet()
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaBaiViet", nullable = true)
    private BaiVietMarketing baiViet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaNguoiDung", nullable = false)
    private NguoiDung nguoiDung;

    @Column(name = "SoSaoDanhGia", nullable = false)
    private Integer soSao;

    @Column(name = "BinhLuan", length = 500)
    private String binhLuan;

    @Column(name = "NgayDanhGia")
    private LocalDateTime ngayDanhGia;
}