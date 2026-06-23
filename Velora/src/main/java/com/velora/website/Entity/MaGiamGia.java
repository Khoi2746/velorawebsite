package com.velora.website.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ma_giam_gia", indexes = {
    @Index(name = "idx_ma_code", columnList = "ma_code") // Đánh index để truy vấn tốc độ cao
})
public class MaGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_code", unique = true, nullable = false, length = 50)
    private String maCode;

    @Column(name = "phan_tram_giam", nullable = false)
    private Double phanTramGiam;

    @Column(name = "gioi_han_su_dung")
    private Integer gioiHanSuDung;
    
    @Column(name = "so_luot_da_dung")
    private Integer soLuotDaDung = 0;

    @Column(name = "ngay_het_han")
    private LocalDateTime ngayHetHan;
}