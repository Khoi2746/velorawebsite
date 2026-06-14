package com.velora.website.Entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ThuongHieu")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThuongHieu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaThuongHieu")
    private Integer maThuongHieu;

    @Column(name = "TenThuongHieu", nullable = false, unique = true, length = 100)
    private String tenThuongHieu;

    @Column(name = "LogoThuongHieu", length = 255)
    private String logoThuongHieu;

    @Column(name = "MoTaNgan", columnDefinition = "NVARCHAR(500)")
    private String moTaNgan;

    @Column(name = "WebsiteThuongHieu", length = 255)
    private String websiteThuongHieu;

    // Sử dụng Boolean cho kiểu BIT trong SQL Server (1 = true, 0 = false)
    @Column(name = "TrangThai")
    private Boolean trangThai = true;
}