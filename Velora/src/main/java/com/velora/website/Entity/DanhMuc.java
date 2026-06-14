package com.velora.website.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "DanhMuc")
@Data
public class DanhMuc {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maDanhMuc;

    @Column(nullable = false, unique = true)
    private String tenDanhMuc;

    @Column(nullable = false, unique = true)
    private String duongDanSlug;

    private String trangThai;

    // Quan hệ 1-nhiều: Một danh mục có nhiều sản phẩm
    // Dùng @JsonIgnore để tránh bị lỗi vòng lặp vô tận khi API trả về JSON
    @OneToMany(mappedBy = "danhMuc")
    @JsonIgnore
    private List<SanPham> sanPhams;
}