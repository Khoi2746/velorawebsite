package com.velora.website.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "BaiVietMarketing")
public class BaiVietMarketing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maBaiViet;
    
    private Integer nguoiDang;
    private String tieuDe;
    private String noiDung;
    private String anhBia;
    
    private String maGiamGia;
    private Double phanTramGiam;
    private Integer soLuotGioiHan;
    private Integer soLuotDaDung;
    private Date hanSuDung;
    
    private String trangThai;
    private Date ngayTao;
}