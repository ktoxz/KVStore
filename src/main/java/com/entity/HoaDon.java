package com.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HoaDon {
    private String maHoaDon;
    private LocalDate ngayGiaoDich;
    private String thongTinChung;
    private double tienKhach;
    private double thue;
    private String maKH;
    private String maNV;
    private Integer maKM; // Nullable

    // Danh sách chi tiết hóa đơn
    private List<CT_HoaDon> chiTietList = new ArrayList<>();

    public HoaDon() {
    }

    public HoaDon(String maHoaDon, LocalDate ngayGiaoDich, String thongTinChung,
                  double tienKhach, double thue, String maKH, String maNV, Integer maKM) {
        this.maHoaDon = maHoaDon;
        this.ngayGiaoDich = ngayGiaoDich;
        this.thongTinChung = thongTinChung;
        this.tienKhach = tienKhach;
        this.thue = thue;
        this.maKH = maKH;
        this.maNV = maNV;
        this.maKM = maKM;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public LocalDate getNgayGiaoDich() {
        return ngayGiaoDich;
    }

    public void setNgayGiaoDich(LocalDate ngayGiaoDich) {
        this.ngayGiaoDich = ngayGiaoDich;
    }

    public String getThongTinChung() {
        return thongTinChung;
    }

    public void setThongTinChung(String thongTinChung) {
        this.thongTinChung = thongTinChung;
    }

    public double getTienKhach() {
        return tienKhach;
    }

    public void setTienKhach(double tienKhach) {
        this.tienKhach = tienKhach;
    }

    public double getThue() {
        return thue;
    }

    public void setThue(double thue) {
        this.thue = thue;
    }

    public String getMaKH() {
        return maKH;
    }

    public void setMaKH(String maKH) {
        this.maKH = maKH;
    }

    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public Integer getMaKM() {
        return maKM;
    }

    public void setMaKM(Integer maKM) {
        this.maKM = maKM;
    }

    public List<CT_HoaDon> getChiTietList() {
        return chiTietList;
    }

    public void setChiTietList(List<CT_HoaDon> chiTietList) {
        this.chiTietList = chiTietList;
    }

    public void addChiTiet(CT_HoaDon chiTiet) {
        this.chiTietList.add(chiTiet);
    }

    @Override
    public String toString() {
        return "HoaDon [maHoaDon=" + maHoaDon + ", ngayGiaoDich=" + ngayGiaoDich + ", thongTinChung="
                + thongTinChung + ", tienKhach=" + tienKhach + ", thue=" + thue + ", maKH=" + maKH + ", maNV=" + maNV
                + ", maKM=" + maKM + "]";
    }
}

