package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

import com.connectDB.ConnectDB;
import com.entity.KhachHang;

public class DAO_KhachHang {
	public ArrayList<KhachHang> getAllKhachHang() {
		ArrayList<KhachHang> dskh = new ArrayList<KhachHang>();
		try {
			ConnectDB.getInstance();
			Connection con = ConnectDB.getCon();
			String sql = "Select * from KhachHang";
			Statement statement = con.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			while(rs.next()) {
				String maKH = rs.getString(1);
				String tenKH = rs.getString(2);
				boolean gioiTinh = rs.getBoolean(3);
				String sdt = rs.getString(4);
				LocalDate ngayTao = rs.getDate(5).toLocalDate();
				int diemTichLuy = rs.getInt(6);
				KhachHang kh = new KhachHang(maKH, tenKH, gioiTinh, sdt, ngayTao, diemTichLuy);
				dskh.add(kh);
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return dskh;
	}
	
	public String phatSinhMaKH() {
	    String maKH = "KH00000001";
	    try {
	        ConnectDB.getInstance();
	        Connection con = ConnectDB.getCon();
	        String sql = "SELECT MAX(CAST(SUBSTRING(maKH, 3, LEN(maKH)) AS INT)) FROM KhachHang";
	        Statement stmt = con.createStatement();
	        ResultSet rs = stmt.executeQuery(sql);
	        if (rs.next()) {
	            int lastSo = rs.getInt(1);  // lấy phần số
	            lastSo++;
	            maKH = String.format("KH%08d", lastSo);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return maKH;
	}

	
	public boolean themKH(KhachHang kh) {
		ConnectDB.getInstance();
		Connection con = ConnectDB.getCon();
		PreparedStatement stmt = null;
		int n = 0;
		try {
			stmt = con.prepareStatement("INSERT INTO KhachHang (maKH, tenKH, gioiTinh, sdt, ngayTaoTK, diemTichLuy) VALUES (?, ?, ?, ?, ?, ?)");
			String maKH = phatSinhMaKH();
			stmt.setString(1, maKH);
			stmt.setString(2, kh.getTenKH());
			stmt.setBoolean(3, kh.isGioiTinh());
			stmt.setString(4, kh.getSdt());
			stmt.setDate(5, java.sql.Date.valueOf(kh.getNgayTao()));
			stmt.setInt(6, 0);
			n = stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			try {
				stmt.close();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
		}
		
		return n>0;
	}
	
	public boolean capNhatKH(KhachHang kh) {
		ConnectDB.getInstance();
		Connection con = ConnectDB.getCon();
		PreparedStatement stmt = null;
		int n = 0;
		try {
			stmt = con.prepareStatement("UPDATE KhachHang SET tenKH=?, gioiTinh=?, diemTichLuy=? WHERE maKH=?");
			stmt.setString(1, kh.getTenKH());
			stmt.setBoolean(2, kh.isGioiTinh());
			stmt.setInt(3, kh.getDiemTichLuy());
			stmt.setString(4, kh.getMaKH());
			n = stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}finally {
			try {
				stmt.close();
			} catch (SQLException e2) {
				e2.printStackTrace();
			}
		}
		return n>0;
	}
	
	public KhachHang timKiemKH(String sdt) {
	    KhachHang kh = null;
	    ConnectDB.getInstance();
	    Connection con = ConnectDB.getCon();
	    PreparedStatement stmt = null;
	    ResultSet rs = null;
	    try {
	        stmt = con.prepareStatement("SELECT * FROM KhachHang WHERE sdt = ?");
	        stmt.setString(1, sdt);
	        rs = stmt.executeQuery();
	        if (rs.next()) {
	            kh = new KhachHang(
	                rs.getString(1),          
	                rs.getString(2),           
	                rs.getBoolean(3),         
	                rs.getString(4),
	                rs.getDate(5).toLocalDate(),
	                rs.getInt(6)               
	            );
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            if (rs != null) rs.close();
	            if (stmt != null) stmt.close();
	        } catch (SQLException e2) {
	            e2.printStackTrace();
	        }
	    }
	    return kh; 
	}

	
}
