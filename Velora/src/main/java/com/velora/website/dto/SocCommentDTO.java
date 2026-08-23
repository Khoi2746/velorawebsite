package com.velora.website.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SocCommentDTO {
    private Integer maDanhGia;
    private Integer maNguoiDung;
    private String tenNguoiDung;
    private Integer soSao;
    private String noiDung;
    private String tenSanPham;
    private Boolean isSpam;
    private LocalDateTime ngayDanhGia;
}