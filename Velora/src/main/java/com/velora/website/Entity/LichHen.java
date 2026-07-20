package com.velora.website.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "lich_hen")
@Getter
@Setter
@NoArgsConstructor
public class LichHen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_khach_hang", nullable = false)
    private String tenKhachHang;

    @Column(name = "so_dien_thoai", nullable = false)
    private String soDienThoai;

    @Column(name = "email")
    private String email;

    @Column(name = "ngay_hen", nullable = false)
    private LocalDate ngayHen;

    @Column(name = "thoi_gian")
    private String thoiGian;

    @Column(name = "ghi_chu")
    private String ghiChu;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_san_pham")
    private SanPham sanPham; 

    @Column(name = "trang_thai")
    private Integer trangThai = 0;
}