package com.velora.website.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "DanhSachDenIP")
public class DanhSachDenIP {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer maDanhSachDen;

    private String diaChiIP;
    private String lyDoChan;
    private String hinhThucChan;

    @Temporal(TemporalType.TIMESTAMP)
    private Date thoiGianBatDau;

    @Temporal(TemporalType.TIMESTAMP)
    private Date thoiGianHetHan;
}