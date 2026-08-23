package com.velora.website.dto;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class SocUserDTO {
    private Integer maNguoiDung;
    private String hoTen;
    private String email;
    private String trangThai;
    private LocalDateTime thoiGianCamBinhLuan;
    private String lyDoViPham; 
    private LocalDateTime thoiGianViPham;
}