package com.velora.website.Entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "loai_san_pham")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoaiSanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ma_loai")
    private Long maLoai;

    @Column(name = "ten_loai", nullable = false, length = 100)
    private String tenLoai;

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String moTa;
}