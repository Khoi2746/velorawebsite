package com.velora.website.Request;

public class DanhGiaRequest {
    
    private Integer maSanPham;
    private Integer maNguoiDung;
    private Integer soSaoDanhGia; // Đã đổi thành soSaoDanhGia
    private String binhLuan;

    // --- GETTERS & SETTERS ---
    
    public Integer getMaSanPham() { 
        return maSanPham; 
    }
    
    public void setMaSanPham(Integer maSanPham) { 
        this.maSanPham = maSanPham; 
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