package com.entity;

import java.sql.Date;
import java.util.Objects;

public class KhuyenMai {
    private int maKM;          // IDENTITY
    private String tenKM;
    private String moTaKM;
    private Date ngayBatDau;
    private Date ngayKetThuc;

    public KhuyenMai() {}

    public KhuyenMai(int maKM, String tenKM, String moTaKM,
                     Date ngayBatDau, Date ngayKetThuc) {
        this.maKM = maKM;
        this.tenKM = tenKM;
        this.moTaKM = moTaKM;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
    }

    public KhuyenMai(String tenKM, String moTaKM,
                     Date ngayBatDau, Date ngayKetThuc, String loaiKM) {
        this.tenKM = tenKM;
        this.moTaKM = moTaKM;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
    }

    public KhuyenMai(int maKM) {
        this.maKM = maKM;
    }


    public int getMaKM() { return maKM; }
    public String getTenKM() { return tenKM; }
    public String getMoTaKM() { return moTaKM; }
    public Date getNgayBatDau() { return ngayBatDau; }
    public Date getNgayKetThuc() { return ngayKetThuc; }


    public void setMaKM(int maKM) { this.maKM = maKM; }
    public void setTenKM(String tenKM) { this.tenKM = tenKM; }
    public void setMoTaKM(String moTaKM) { this.moTaKM = moTaKM; }
    public void setNgayBatDau(Date ngayBatDau) { this.ngayBatDau = ngayBatDau; }
    public void setNgayKetThuc(Date ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof KhuyenMai)) return false;
        KhuyenMai that = (KhuyenMai) o;
        return maKM == that.maKM;
    }

    @Override public int hashCode() {
        return Objects.hash(maKM);
    }

    @Override public String toString() {
        return "KhuyenMai{" +
                "maKM=" + maKM +
                ", tenKM='" + tenKM + '\'' +
                ", ngayBatDau=" + ngayBatDau +
                ", ngayKetThuc=" + ngayKetThuc +
                '}';
    }
}
