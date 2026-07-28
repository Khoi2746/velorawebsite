package com.velora.website.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "BaoHanh")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BaoHanh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaBaoHanh")
    private Integer maBaoHanh;

    @Column(name = "MaNguoiDung", nullable = false)
    private Integer maNguoiDung;

    @Column(name = "MaDonHangCode", length = 20, nullable = false)
    private String maDonHangCode;

    @Column(name = "HoTen", length = 100)
    private String hoTen;

    @Column(name = "SoDienThoai", length = 15)
    private String soDienThoai;

    @Column(name = "LoaiSanPham", length = 100, nullable = false)
    private String loaiSanPham;

    @Column(name = "MoTaLoi", columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String moTaLoi;

    @Column(name = "TrangThai", length = 50, nullable = false)
    private String trangThai; // CHO_XU_LY, CHO_XAC_NHAN_HEN, DA_TIEP_NHAN, DANG_XU_LY, HOAN_TAT, DA_HUY, YEU_CAU_DOI_LICH

    @Column(name = "NgayGui", nullable = false, updatable = false)
    private LocalDateTime ngayGui;

    @Column(name = "ThoiGianHen")
    private LocalDateTime thoiGianHen; // Thời gian do Admin đề xuất

    @Column(name = "ThoiGianKhachMongMuon", length = 100)
    private String thoiGianKhachMongMuon; // Thời gian khách tự đề xuất khi muốn đổi lịch

    @PrePersist
    public void prePersist() {
        if (this.ngayGui == null) this.ngayGui = LocalDateTime.now();
        if (this.trangThai == null || this.trangThai.isBlank()) this.trangThai = "CHO_XU_LY";
    }
}