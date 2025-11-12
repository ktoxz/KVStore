package com.entity;

// Bỏ import java.sql.Date;

public class ChiTietKhuyenMai {
    private KhuyenMai km;
    private SanPham sp;
    private double tiLe;
    private String loaiKM; // ✅ THAY ĐỔI: Thêm loaiKM

    // ✅ THAY ĐỔI: Bỏ ngayApDung khỏi constructor
    public ChiTietKhuyenMai(KhuyenMai km, SanPham sp, double tiLe, String loaiKM) {
        this.km = km;
        this.sp = sp;
        this.tiLe = tiLe;
        this.loaiKM = loaiKM;
    }

    public ChiTietKhuyenMai() {
        // constructor rỗng
    }

    // --- Getter / Setter ---

    public KhuyenMai getKm() {
        return km;
    }

    public void setKm(KhuyenMai km) {
        this.km = km;
    }

    public SanPham getSp() {
        return sp;
    }

    public void setSp(SanPham sp) {
        this.sp = sp;
    }

    public double getTiLe() {
        return tiLe;
    }

    public void setTiLe(double tiLe) {
        this.tiLe = tiLe;
    }

    public String getLoaiKM() {
        return loaiKM;
    }

    public void setLoaiKM(String loaiKM) {
        this.loaiKM = loaiKM;
    }
    
    // Bỏ getter/setter cho ngayApDung
}