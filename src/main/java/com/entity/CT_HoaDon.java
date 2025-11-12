package com.entity;

import com.dao.DAO_CT_KhuyenMai;

public class CT_HoaDon {
    private String maHoaDon;
    private SanPham sanPham;
    private int soLuong;

    private String tenSP;
    private double giaSP;

    public CT_HoaDon() {}

    public CT_HoaDon(String maHoaDon, SanPham sanPham, int soLuong) {
        this.maHoaDon = maHoaDon;
        this.sanPham = sanPham;
        this.soLuong = soLuong;
    }

    public CT_HoaDon(String maHoaDon, SanPham sanPham, int soLuong, String tenSP, double giaSP) {
        this.maHoaDon = maHoaDon;
        this.sanPham = sanPham;
        this.soLuong = soLuong;
        this.tenSP = tenSP;
        this.giaSP = giaSP;
    }

    public String getMaHoaDon() { return maHoaDon; }
    public SanPham getSanPham() { return sanPham; }
    public void setSanPham(SanPham sanPham) { this.sanPham = sanPham; }
    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
    public String getTenSP() { return tenSP; }
    public void setTenSP(String tenSP) { this.tenSP = tenSP; }
    public double getGiaSP() { return giaSP; }
    public void setGiaSP(double giaSP) { this.giaSP = giaSP; }

    // Thành tiền gốc
    public double getThanhTien() {
        return soLuong * giaSP;
    }

    // Thành tiền sau khi áp dụng khuyến mãi
    public double getThanhTienSauKM() {
        double tong = getThanhTien();
        try {
            DAO_CT_KhuyenMai daoKM = new DAO_CT_KhuyenMai();
            var km = daoKM.findBestForProduct(sanPham.getMaSP());
            if (km != null) {
                switch (km.getLoaiKM()) {
                    case GiamGiaPhanTramSP -> tong -= tong * km.getGiaTri() / 100.0;
                    case GiamGiaTienSP -> tong -= km.getGiaTri();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Math.max(tong, 0);
    }

    @Override
    public String toString() {
        return "CT_HoaDon [maHoaDon=" + maHoaDon +
                ", maSP=" + (sanPham != null ? sanPham.getMaSP() : "null") +
                ", soLuong=" + soLuong + "]";
    }
}
