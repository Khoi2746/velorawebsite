package com.velora.website.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "NhatKyDangNhap")
public class NhatKyDangNhap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maNhatKy;

    private String emailDangNhap;
    private String diaChiIP;
    private String thongTinThietBi;
    private String trangThaiKetQua;

    @Temporal(TemporalType.TIMESTAMP)
    private Date thoiGianDangNhap;
}