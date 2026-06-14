package com.velora.website.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Entity
@Table(name = "NguoiDung")
@Data // Tự động sinh Getter/Setter nếu em có cài thư viện Lombok
public class NguoiDung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaNguoiDung")
    private Integer maNguoiDung;

    @Column(name = "HoTen", columnDefinition = "NVARCHAR(255)")
    private String hoTen;

    @Column(name = "Email", nullable = false, unique = true)
    private String email;

    @Column(name = "MatKhauMaHoa", nullable = false)
    private String matKhauMaHoa;

    @Column(name = "SoDienThoai")
    private String soDienThoai;

    @Column(name = "DiaChi", columnDefinition = "NVARCHAR(MAX)")
    private String diaChi;

    @Column(name = "TrangThai")
    private String trangThai;

    @Column(name = "VaiTro", columnDefinition = "NVARCHAR(50)")
    private String vaiTro; // Chứa giá trị "ADMIN" hoặc "USER"

    @Column(name = "NgayTao")
    private Date ngayTao;

    @Column(name = "NgayCapNhat")
    private Date ngayCapNhat;
}