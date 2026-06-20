package com.velora.website.Request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PhieuNhapRequest {
    private Integer maNguoiYeuCau;
    private String ghiChu;
    private List<ChiTietDto> chiTietList;

    @Data
    public static class ChiTietDto {
        private Integer maSanPham;
        private Integer soLuongNhap;
        private BigDecimal giaNhap;
    }
}