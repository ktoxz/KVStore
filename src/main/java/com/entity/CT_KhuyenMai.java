package com.entity;

import com.enums.LoaiKM;

public class CT_KhuyenMai {
    private KhuyenMai khuyenMai;
    private SanPham sanPham;
    private double giaTri;     // % hoặc số tiền giảm
    private LoaiKM loaiKM;     // Enum thay vì String

    public CT_KhuyenMai(KhuyenMai khuyenMai, SanPham sanPham, double giaTri, LoaiKM loaiKM) {
        this.khuyenMai = khuyenMai;
        this.sanPham = sanPham;
        this.giaTri = giaTri;
        this.loaiKM = loaiKM;
    }


    // --- Getter & Setter ---
    public KhuyenMai getKhuyenMai() { return khuyenMai; }
    public void setKhuyenMai(KhuyenMai khuyenMai) { this.khuyenMai = khuyenMai; }

    public SanPham getSanPham() { return sanPham; }
    public void setSanPham(SanPham sanPham) { this.sanPham = sanPham; }

    public double getGiaTri() { return giaTri; }
    public void setGiaTri(double giaTri) { this.giaTri = giaTri; }

    public LoaiKM getLoaiKM() { return loaiKM; }
    public void setLoaiKM(LoaiKM loaiKM) { this.loaiKM = loaiKM; }

    @Override
    public String toString() {
        return "ChiTietKhuyenMai{" +
                "KM=" + (khuyenMai != null ? khuyenMai.getTenKM() : "null") +
                ", SP=" + (sanPham != null ? sanPham.getTenSP() : "null") +
                ", GiaTri=" + giaTri +
                ", Loai=" + loaiKM +
                '}';
    }
}
