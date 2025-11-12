package com.entity;

import com.dao.DAO_KhachHang;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HoaDon {
    private String maHoaDon;
    private LocalDate ngayGiaoDich;
    private double tienKhach;
    private double thue;
    private KhachHang khachHang; // Thông tin khách hàng
    private NhanVien nhanVien;

    // Danh sách chi tiết hóa đơn
    private List<CT_HoaDon> chiTietList = new ArrayList<>();

    public HoaDon() {
    }

    public HoaDon(String maHoaDon, LocalDate ngayGiaoDich, double tienKhach, double thue, KhachHang khachHang, NhanVien nhanVien) {
        this.maHoaDon = maHoaDon;
        this.ngayGiaoDich = ngayGiaoDich;
        this.tienKhach = tienKhach;
        this.thue = thue;
        this.khachHang = khachHang;
        this.nhanVien = nhanVien;
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

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public void setKhachHang(String maKH) {
        DAO_KhachHang dao_KhachHang = new DAO_KhachHang();
        this.khachHang = new KhachHang();
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
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
        return "HoaDon{" +
                "maHoaDon='" + maHoaDon + '\'' +
                ", ngayGiaoDich=" + ngayGiaoDich +
                ", tienKhach=" + tienKhach +
                ", thue=" + thue +
                ", khachHang=" + khachHang +
                ", nhanVien=" + nhanVien +
                ", chiTietList=" + chiTietList +
                '}';
    }
}

