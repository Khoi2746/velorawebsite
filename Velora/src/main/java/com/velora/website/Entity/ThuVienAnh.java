package com.velora.website.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference; // 🔥 Nhớ import thư viện này
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "ThuVienAnh")
@Data
public class ThuVienAnh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maAnh;

    @Column(name = "duongDanAnh")
    private String duongDanAnh;

    @ManyToOne
    @JoinColumn(name = "maSanPham")
    @JsonBackReference // 🔥 Chặn đứng vòng lặp vô hạn JSON giữa SanPham và ThuVienAnh
    private SanPham sanPham;
}