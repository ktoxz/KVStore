package com.entity;

import java.sql.Date;

public class ChiTietKhuyenMai {
    private int maKM;
    private String maSP;
    private double tiLe;
    private Date ngayApDung;

    public ChiTietKhuyenMai() {}

    public ChiTietKhuyenMai(int maKM, String maSP, double tiLe, Date ngayApDung) {
        this.maKM = maKM;
        this.maSP = maSP;
        this.tiLe = tiLe;
        this.ngayApDung = ngayApDung;
    }

    public int getMaKM() { return maKM; }
    public String getMaSP() { return maSP; }
    public double getTiLe() { return tiLe; }
    public Date getNgayApDung() { return ngayApDung; }

    public void setMaKM(int maKM) { this.maKM = maKM; }
    public void setMaSP(String maSP) { this.maSP = maSP; }
    public void setTiLe(double tiLe) { this.tiLe = tiLe; }
    public void setNgayApDung(Date ngayApDung) { this.ngayApDung = ngayApDung; }
}
