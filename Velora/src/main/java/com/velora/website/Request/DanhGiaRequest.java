package com.velora.website.Request;

public class DanhGiaRequest {
    
    private Integer maSanPham;
    private Integer maBaiViet; 
    private String loaiDanhGia; 
    private Integer maNguoiDung;
    private Integer soSaoDanhGia;
    private String binhLuan;

    public Integer getMaSanPham() { 
        return maSanPham; 
    }
    
    public void setMaSanPham(Integer maSanPham) { 
        this.maSanPham = maSanPham; 
    }

    public Integer getMaBaiViet() {
        return maBaiViet;
    }

    public void setMaBaiViet(Integer maBaiViet) {
        this.maBaiViet = maBaiViet;
    }

    public String getLoaiDanhGia() {
        return loaiDanhGia;
    }

    public void setLoaiDanhGia(String loaiDanhGia) {
        this.loaiDanhGia = loaiDanhGia;
    }

    public Integer getMaNguoiDung() { 
        return maNguoiDung; 
    }
    
    public void setMaNguoiDung(Integer maNguoiDung) { 
        this.maNguoiDung = maNguoiDung; 
    }

    public Integer getSoSaoDanhGia() { 
        return soSaoDanhGia; 
    }
    
    public void setSoSaoDanhGia(Integer soSaoDanhGia) { 
        this.soSaoDanhGia = soSaoDanhGia; 
    }

    public String getBinhLuan() { 
        return binhLuan; 
    }
    
    public void setBinhLuan(String binhLuan) { 
        this.binhLuan = binhLuan; 
    }
}