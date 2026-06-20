package com.velora.website.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "PhieuNhapKho")
@Data
public class PhieuNhapKho {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaPhieuNhap")
    private Integer maPhieuNhap;

    @Column(name = "MaPhieuNhapCode")
    private String maPhieuNhapCode;

    @Column(name = "MaNguoiYeuCau")
    private Integer maNguoiYeuCau;

    @Column(name = "MaNguoiDuyet")
    private Integer maNguoiDuyet;

    // Đã xóa "insertable = false, updatable = false" và ép kiểu trả về chuỗi ngày tháng chuẩn
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "NgayYeuCau")
    private LocalDateTime ngayYeuCau;

    // Ép kiểu chuẩn cho cả ngày duyệt
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "NgayDuyet")
    private LocalDateTime ngayDuyet;

    @Column(name = "TrangThai")
    private String trangThai;

    @Column(name = "LyDoTuChoi")
    private String lýDoTuChoi;

    @Column(name = "GhiChu")
    private String ghiChu;
}