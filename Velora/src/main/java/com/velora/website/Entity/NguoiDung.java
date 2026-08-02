package com.velora.website.Entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "NguoiDung") 
public class NguoiDung {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaNguoiDung") 
    // 🔥 SỬA CHỖ NÀY: Ép buộc phải là Integer để đồng bộ hóa hoàn toàn với JpaRepository<NguoiDung, Integer>
    private Integer maNguoiDung;

    @Column(name = "HoTen", nullable = false)
    private String hoTen;

    @Column(name = "Email", nullable = false, unique = true)
    private String email;

    @Column(name = "MatKhauMaHoa", nullable = false) 
    private String matKhauMaHoa;

    @Column(name = "SoDienThoai")
    private String soDienThoai;

    @Column(name = "DiaChi")
    private String diaChi;

    @Column(name = "TrangThai")
    private String trangThai; 

    // Anh đã gỡ @Temporal vì Spring Boot mới khuyên dùng luôn LocalDateTime thay cho Date, 
    // nhưng nếu em xài Date thì chỉ cần @Column là Hibernate tự map được.
    @Column(name = "NgayTao")
    private Date ngayTao;

    @Column(name = "NgayCapNhat")
    private Date ngayCapNhat;

    // --- ĐÃ CHUYỂN CỘT PHẠT LÊN TRÊN ---
    @Column(name = "SoLanViPham")
    private Integer soLanViPham;

    @Column(name = "ThoiGianCamBinhLuan")
    private LocalDateTime thoiGianCamBinhLuan;

    // --- ĐÃ CHUYỂN @ManyToMany XUỐNG ĐÚNG CHỖ ---
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "NguoiDung_VaiTro",
        joinColumns = @JoinColumn(name = "MaNguoiDung"), 
        inverseJoinColumns = @JoinColumn(name = "MaVaiTro")
    )
    @JsonIgnoreProperties("nguoiDungs") 
    private List<VaiTro> vaiTros;

    // ==========================================
    //          GETTERS & SETTERS DƯỚI NÀY
    // ==========================================

    public Integer getMaNguoiDung() { return maNguoiDung; }
    public void setMaNguoiDung(Integer maNguoiDung) { this.maNguoiDung = maNguoiDung; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMatKhauMaHoa() { return matKhauMaHoa; }
    public void setMatKhauMaHoa(String matKhauMaHoa) { this.matKhauMaHoa = matKhauMaHoa; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public Date getNgayTao() { return ngayTao; }
    public void setNgayTao(Date ngayTao) { this.ngayTao = ngayTao; }

    public Date getNgayCapNhat() { return ngayCapNhat; }
    public void setNgayCapNhat(Date ngayCapNhat) { this.ngayCapNhat = ngayCapNhat; }

    public Integer getSoLanViPham() { return soLanViPham; }
    public void setSoLanViPham(Integer soLanViPham) { this.soLanViPham = soLanViPham; }

    public LocalDateTime getThoiGianCamBinhLuan() { return thoiGianCamBinhLuan; }
    public void setThoiGianCamBinhLuan(LocalDateTime thoiGianCamBinhLuan) { this.thoiGianCamBinhLuan = thoiGianCamBinhLuan; }

    public List<VaiTro> getVaiTros() { return vaiTros; }
    public void setVaiTros(List<VaiTro> vaiTros) { this.vaiTros = vaiTros; }
}