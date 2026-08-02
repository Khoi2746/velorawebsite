package com.velora.website.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "DanhGia")
public class DanhGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDanhGia")
    private Integer maDanhGia;

    @Column(name = "SoSaoDanhGia")
    private Integer soSaoDanhGia;

    @Column(name = "BinhLuan", columnDefinition = "NVARCHAR(MAX)")
    private String binhLuan;

    @Column(name = "NgayDanhGia")
    private LocalDateTime ngayDanhGia = LocalDateTime.now(); // Tự động lấy giờ hiện tại

    // Liên kết n-1 với bảng SanPham
    @ManyToOne
    @JoinColumn(name = "MaSanPham")
    private SanPham sanPham;

    // Liên kết n-1 với bảng NguoiDung
    @ManyToOne
    @JoinColumn(name = "MaNguoiDung")
    private NguoiDung nguoiDung;

    // --- GETTERS & SETTERS ---
    public Integer getMaDanhGia() { return maDanhGia; }
    public void setMaDanhGia(Integer maDanhGia) { this.maDanhGia = maDanhGia; }

    public Integer getSoSao() { return soSaoDanhGia; }
    public void setSoSao(Integer soSao) { this.soSaoDanhGia = soSao; }

    public String getBinhLuan() { return binhLuan; }
    public void setBinhLuan(String binhLuan) { this.binhLuan = binhLuan; }

    public LocalDateTime getNgayDanhGia() { return ngayDanhGia; }
    public void setNgayDanhGia(LocalDateTime ngayDanhGia) { this.ngayDanhGia = ngayDanhGia; }

    public SanPham getSanPham() { return sanPham; }
    public void setSanPham(SanPham sanPham) { this.sanPham = sanPham; }

    public NguoiDung getNguoiDung() { return nguoiDung; }
    public void setNguoiDung(NguoiDung nguoiDung) { this.nguoiDung = nguoiDung; }
}