package com.velora.website.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "CanhBaoAnNinh")
public class CanhBaoAnNinh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maCanhBao;

    private String diaChiIP;
    private String thongTinThietBi;
    private String loaiTanCong;
    private String moTaChiTiet;
    private String mucDoNguyHiem;
    private Boolean daXuLy;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date ngayTao;
}