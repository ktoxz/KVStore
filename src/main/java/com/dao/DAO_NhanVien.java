package com.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.connectDB.ConnectDB;
import com.entity.NhanVien;

public class DAO_NhanVien {
    /**
     * Lấy mã nhân viên đầu tiên trong database
     * @return Mã nhân viên hoặc null nếu không có
     */
    public NhanVien getFirstNV() {
    	
    	NhanVien nv = new NhanVien();
    	String sql = "SELECT TOP 1 * FROM NhanVien";
    	Connection con = ConnectDB.getCon();
    	if (con == null) {
            System.err.println("Kết nối DB chưa được thiết lập!");
            return nv;
        }
    	
        try (
    		Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
    		){
            

            if (rs.next()) {
                String maNV = rs.getString(1);
                String tenNV = rs.getString(2);
                boolean gioiTinh = rs.getBoolean(4);
                String email = rs.getString(5);
                String sdt = rs.getString(6);
                LocalDate ngayTaoTaiKhoan = rs.getDate(7).toLocalDate();
                String chucVu = rs.getString(8);
                nv = new NhanVien(maNV, tenNV, gioiTinh, email, sdt, ngayTaoTaiKhoan,chucVu);
            }

            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nv;
    }
    
    
    public List<NhanVien> getAllNV() {
    	List<NhanVien> ls = new ArrayList<NhanVien>();
    	String sql = "SELECT * FROM NhanVien";
    	Connection con = ConnectDB.getCon();
    	
    	if (con == null) {
            System.err.println("Kết nối DB chưa được thiết lập!");
            return ls;
        }
    	
        try (
        	Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
        	){
           

            while(rs.next()) {
                String maNV = rs.getString(1);
                String tenNV = rs.getString(2);
                boolean gioiTinh = rs.getBoolean(4);
                String email = rs.getString(5);
                String sdt = rs.getString(6);
                LocalDate ngayTaoTaiKhoan = rs.getDate(7).toLocalDate();
                String chucVu = rs.getString(8);
                NhanVien nv = new NhanVien(maNV, tenNV, gioiTinh, email, sdt, ngayTaoTaiKhoan,chucVu);
                ls.add(nv);
            }

            rs.close();
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return ls;
    }
}
