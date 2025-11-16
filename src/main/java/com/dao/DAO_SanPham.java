package com.dao;

import com.entity.SanPham;
import com.connectDB.ConnectDB;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class DAO_SanPham {

	// =========================
	// ĐẾM SẢN PHẨM (CÓ ÁP DỤNG TỪ KHÓA TÌM KIẾM)
	// =========================
	public int countSanPham(String keyword) {
		// Wrapper cũ gọi hàm mới có đầy đủ tham số lọc
		return countSanPham(keyword, null, null, null, null);
	}

	public int countSanPham(String keyword, String loaiFilter, Boolean trangThaiFilter, Double giaMin, Double giaMax) {
		Connection con = ConnectDB.getCon();
		if (con == null)
			return 0;

		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM SanPham WHERE 1 = 1");
		ArrayList<Object> params = new ArrayList<>();

		// Từ khóa chung
		if (keyword != null && !keyword.trim().isEmpty()) {
			sql.append(" AND (maSP LIKE ?").append(" OR tenSP LIKE ?").append(" OR moTaSP LIKE ?")
					.append(" OR loaiSP LIKE ?").append(" OR CAST(giaSP AS NVARCHAR(50)) LIKE ?")
					.append(" OR (CASE WHEN tinhTrangSP = 1 THEN N'Đang hoạt động' ELSE N'Ngừng' END) LIKE ?)");
			String kw = "%" + keyword.trim() + "%";
			for (int i = 0; i < 6; i++) {
				params.add(kw);
			}
		}

		// Lọc theo loại
		if (loaiFilter != null && !loaiFilter.trim().isEmpty()) {
			sql.append(" AND loaiSP = ?");
			params.add(loaiFilter);
		}

		// Lọc theo trạng thái
		if (trangThaiFilter != null) {
			sql.append(" AND tinhTrangSP = ?");
			params.add(trangThaiFilter ? 1 : 0);
		}

		// Lọc theo giá
		if (giaMin != null) {
			sql.append(" AND giaSP >= ?");
			params.add(giaMin);
		}
		if (giaMax != null) {
			sql.append(" AND giaSP <= ?");
			params.add(giaMax);
		}

		try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
			int i = 1;
			for (Object p : params) {
				if (p instanceof String) {
					ps.setString(i++, (String) p);
				} else if (p instanceof Integer) {
					ps.setInt(i++, (Integer) p);
				} else if (p instanceof Double) {
					ps.setDouble(i++, (Double) p);
				}
			}
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getInt(1);
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi đếm sản phẩm: " + e.getMessage());
		}
		return 0;
	}

	// =========================
	// LẤY 1 TRANG SẢN PHẨM (PHÂN TRANG + TÌM KIẾM)
	// =========================
	public List<SanPham> getSanPhamPage(String keyword, int page, int pageSize) {
		// Wrapper cũ gọi hàm mới có đầy đủ tham số lọc
		return getSanPhamPage(keyword, null, null, null, null, page, pageSize);
	}

	public List<SanPham> getSanPhamPage(String keyword, String loaiFilter, Boolean trangThaiFilter, Double giaMin,
			Double giaMax, int page, int pageSize) {
		List<SanPham> ds = new ArrayList<>();
		Connection con = ConnectDB.getCon();
		if (con == null)
			return ds;

		StringBuilder sql = new StringBuilder("SELECT * FROM SanPham WHERE 1 = 1");
		ArrayList<Object> params = new ArrayList<>();

		boolean hasKw = keyword != null && !keyword.trim().isEmpty();
		if (hasKw) {
			sql.append(" AND (maSP LIKE ?").append(" OR tenSP LIKE ?").append(" OR moTaSP LIKE ?")
					.append(" OR loaiSP LIKE ?").append(" OR CAST(giaSP AS NVARCHAR(50)) LIKE ?")
					.append(" OR (CASE WHEN tinhTrangSP = 1 THEN N'Đang hoạt động' ELSE N'Ngừng' END) LIKE ?)");
			String kw = "%" + keyword.trim() + "%";
			for (int i = 0; i < 6; i++) {
				params.add(kw);
			}
		}

// Lọc theo loại
		if (loaiFilter != null && !loaiFilter.trim().isEmpty()) {
			sql.append(" AND loaiSP = ?");
			params.add(loaiFilter);
		}

// Lọc theo trạng thái
		if (trangThaiFilter != null) {
			sql.append(" AND tinhTrangSP = ?");
			params.add(trangThaiFilter ? 1 : 0);
		}

// Lọc theo giá
		if (giaMin != null) {
			sql.append(" AND giaSP >= ?");
			params.add(giaMin);
		}
		if (giaMax != null) {
			sql.append(" AND giaSP <= ?");
			params.add(giaMax);
		}

		sql.append(" ORDER BY maSP OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

		int offset = (page - 1) * pageSize;
		if (offset < 0)
			offset = 0;

		try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
			int i = 1;
			for (Object p : params) {
				if (p instanceof String) {
					ps.setString(i++, (String) p);
				} else if (p instanceof Integer) {
					ps.setInt(i++, (Integer) p);
				} else if (p instanceof Double) {
					ps.setDouble(i++, (Double) p);
				}
			}
			ps.setInt(i++, offset);
			ps.setInt(i, pageSize);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ds.add(new SanPham(rs.getString("maSP"), rs.getString("tenSP"), rs.getDouble("giaSP"),
							rs.getString("moTaSP"), rs.getString("hinhAnhSP"), rs.getBoolean("tinhTrangSP"),
							rs.getString("loaiSP")));
				}
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi lấy trang sản phẩm: " + e.getMessage());
		}

		return ds;
	}

	// =========================
	// THÊM SẢN PHẨM
	// =========================
	public boolean insertSanPham(SanPham sp) {
		String sql = "INSERT INTO SanPham (maSP, tenSP, giaSP, moTaSP, hinhAnhSP, tinhTrangSP, loaiSP) VALUES (?, ?, ?, ?, ?, ?, ?)";
		Connection con = ConnectDB.getCon();
		if (con == null)
			return false;

		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, sp.getMaSP());
			ps.setString(2, sp.getTenSP());
			ps.setDouble(3, sp.getGiaSP());
			ps.setString(4, sp.getMoTaSP());
			ps.setString(5, sp.getHinhAnhSP());
			ps.setBoolean(6, sp.isTinhTrangSP());
			ps.setString(7, sp.getLoaiSP());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("Lỗi khi thêm sản phẩm: " + e.getMessage());
		}
		return false;
	}

	// =========================
	// CẬP NHẬT SẢN PHẨM
	// =========================
	public boolean updateSanPham(SanPham sp) {
		String sql = "UPDATE SanPham SET tenSP=?, giaSP=?, moTaSP=?, hinhAnhSP=?, tinhTrangSP=?, loaiSP=? WHERE maSP=?";
		Connection con = ConnectDB.getCon();
		if (con == null)
			return false;

		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, sp.getTenSP());
			ps.setDouble(2, sp.getGiaSP());
			ps.setString(3, sp.getMoTaSP());
			ps.setString(4, sp.getHinhAnhSP());
			ps.setBoolean(5, sp.isTinhTrangSP());
			ps.setString(6, sp.getLoaiSP());
			ps.setString(7, sp.getMaSP());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("Lỗi khi cập nhật sản phẩm: " + e.getMessage());
		}
		return false;
	}

	// =========================
	// XÓA SẢN PHẨM
	// =========================
	public boolean deleteSanPham(String maSP) {
		String sql = "DELETE FROM SanPham WHERE maSP=?";
		Connection con = ConnectDB.getCon();
		if (con == null)
			return false;

		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, maSP);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			System.err.println("Lỗi khi xóa sản phẩm: " + e.getMessage());
		}
		return false;
	}

	// =========================
	// TÌM THEO MÃ
	// =========================
	public SanPham findById(String maSP) {
		String sql = "SELECT * FROM SanPham WHERE maSP=?";
		Connection con = ConnectDB.getCon();
		if (con == null)
			return null;

		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, maSP);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return new SanPham(rs.getString("maSP"), rs.getString("tenSP"), rs.getDouble("giaSP"),
							rs.getString("moTaSP"), rs.getString("hinhAnhSP"), rs.getBoolean("tinhTrangSP"),
							rs.getString("loaiSP"));
				}
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi tìm sản phẩm: " + e.getMessage());
		}
		return null;
	}

	// =========================
	// TÌM THEO TÊN (có chứa) HOẶC MÃ (Hàm Live Search cần)
	// =========================
	public List<SanPham> searchByNameOrMa(String keyword, int limit) {
		List<SanPham> ds = new ArrayList<>();
		String sql = "SELECT TOP (?) * FROM SanPham WHERE maSP LIKE ? OR tenSP LIKE ? and tinhTrangSP = 1";
		Connection con = ConnectDB.getCon();
		if (con == null)
			return ds;

		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setInt(1, limit);
			String query = "%" + keyword + "%";
			ps.setString(2, query);
			ps.setString(3, query);

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ds.add(new SanPham(rs.getString("maSP"), rs.getString("tenSP"), rs.getDouble("giaSP"),
							rs.getString("moTaSP"), rs.getString("hinhAnhSP"), rs.getBoolean("tinhTrangSP"),
							rs.getString("loaiSP")));
				}
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi tìm sản phẩm theo tên/mã: " + e.getMessage());
		}
		return ds;
	}

	public List<SanPham> findByTen(String keyword) {
		List<SanPham> ds = new ArrayList<>();
		String sql = "SELECT TOP 10 * FROM SanPham WHERE tenSP LIKE ?";
		Connection con = ConnectDB.getCon();
		if (con == null)
			return ds;

		try (PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, "%" + keyword + "%");
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ds.add(new SanPham(rs.getString("maSP"), rs.getString("tenSP"), rs.getDouble("giaSP"),
							rs.getString("moTaSP"), rs.getString("hinhAnhSP"), rs.getBoolean("tinhTrangSP"),
							rs.getString("loaiSP")));
				}
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi tìm sản phẩm theo tên: " + e.getMessage());
		}
		return ds;
	}

	// =========================
	// TÍNH VỊ TRÍ (INDEX 0-BASED) CỦA 1 MÃ TRONG DANH SÁCH ĐÃ LỌC
	// =========================
	public int getIndexById(String keyword, String maSP) {
		Connection con = ConnectDB.getCon();
		if (con == null || maSP == null || maSP.trim().isEmpty())
			return -1;

		boolean hasKw = keyword != null && !keyword.trim().isEmpty();
		StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM SanPham");

		if (hasKw) {
			sql.append(" WHERE (").append("maSP LIKE ?").append(" OR tenSP LIKE ?").append(" OR moTaSP LIKE ?")
					.append(" OR loaiSP LIKE ?").append(" OR CAST(giaSP AS NVARCHAR(50)) LIKE ?")
					.append(" OR (CASE WHEN tinhTrangSP = 1 THEN N'Đang hoạt động' ELSE N'Ngừng' END) LIKE ?")
					.append(") AND maSP <= ?");
		} else {
			sql.append(" WHERE maSP <= ?");
		}

		try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
			int idx = 1;
			if (hasKw) {
				String kw = "%" + keyword.trim() + "%";
				ps.setString(idx++, kw);
				ps.setString(idx++, kw);
				ps.setString(idx++, kw);
				ps.setString(idx++, kw);
				ps.setString(idx++, kw);
				ps.setString(idx++, kw);
			}
			ps.setString(idx, maSP);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					int cnt = rs.getInt(1);
					if (cnt <= 0)
						return -1; // không có trong danh sách đang lọc
					return cnt - 1; // index 0-based
				}
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi tính index sản phẩm: " + e.getMessage());
		}
		return -1;
	}

	// =========================
	// SINH MÃ SẢN PHẨM TIẾP THEO DẠNG SPxxx
	// =========================
	public String getNextMaSanPham() {
		Connection con = ConnectDB.getCon();
		if (con == null) {
			return "SP001"; // fallback khi chưa kết nối được DB
		}

		String sql = "SELECT MAX(maSP) AS maxMa FROM SanPham WHERE maSP LIKE 'SP%'";
		try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

			if (rs.next()) {
				String maxMa = rs.getString("maxMa");

				// Chưa có sản phẩm nào dạng SP... trong DB
				if (maxMa == null || maxMa.trim().isEmpty()) {
					return "SP001";
				}

				maxMa = maxMa.trim();
				String prefix = "SP";
				String numPart = maxMa.startsWith(prefix) ? maxMa.substring(prefix.length()) : maxMa; // lỡ ai nhập tay
																										// không có SP

				int nextNum;
				try {
					nextNum = Integer.parseInt(numPart) + 1;
				} catch (NumberFormatException e) {
					// Nếu phần số bị lỗi, quay về 1 cho an toàn
					nextNum = 1;
				}

				// Giữ nguyên số lượng chữ số như mã lớn nhất hiện tại (ví dụ SP001 → 3 số)
				int width = numPart.length() > 0 ? numPart.length() : 3;
				String fmt = "%0" + width + "d";

				return prefix + String.format(fmt, nextNum);
			}
		} catch (SQLException e) {
			System.err.println("Lỗi getNextMaSanPham: " + e.getMessage());
		}

		// Nếu có lỗi gì đó thì trả về SP001
		return "SP001";
	}

	// =========================
	// ĐẾM SẢN PHẨM ĐANG KHUYẾN MÃI
	// =========================
	public int countSanPhamKhuyenMai() {
		Connection con = ConnectDB.getCon();
		if (con == null)
			return 0;

		String sql = "SELECT COUNT(DISTINCT sp.maSP)" + " FROM SanPham sp"
				+ " JOIN CT_KhuyenMai ct ON sp.maSP = ct.maSP" + " JOIN KhuyenMai km ON ct.maKM = km.maKM"
				+ " WHERE km.ngayBatDau <= ? AND km.ngayKetThuc >= ?";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			java.sql.Date today = java.sql.Date.valueOf(LocalDate.now());
			ps.setDate(1, today);
			ps.setDate(2, today);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getInt(1);
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi đếm sản phẩm khuyến mãi: " + e.getMessage());
		}
		return 0;
	}

	// =========================
	// LẤY 1 TRANG SẢN PHẨM ĐANG KHUYẾN MÃI
	// =========================
	public List<SanPham> getSanPhamKhuyenMaiPage(int page, int pageSize) {
		List<SanPham> ds = new ArrayList<>();
		Connection con = ConnectDB.getCon();
		if (con == null)
			return ds;
		if (pageSize <= 0)
			pageSize = 5;
		if (page <= 0)
			page = 1;
		int offset = (page - 1) * pageSize;
		String sql = "SELECT DISTINCT sp.*" + " FROM SanPham sp" + " JOIN CT_KhuyenMai ct ON sp.maSP = ct.maSP"
				+ " JOIN KhuyenMai km ON ct.maKM = km.maKM" + " WHERE km.ngayBatDau <= ? AND km.ngayKetThuc >= ?"
				+ " ORDER BY sp.maSP" + " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			java.sql.Date today = java.sql.Date.valueOf(LocalDate.now());
			ps.setDate(1, today);
			ps.setDate(2, today);
			ps.setInt(3, offset);
			ps.setInt(4, pageSize);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					ds.add(new SanPham(rs.getString("maSP"), rs.getString("tenSP"), rs.getFloat("giaSP"),
							rs.getString("moTaSP"), rs.getString("hinhAnhSP"), rs.getBoolean("tinhTrangSP"),
							rs.getString("loaiSP")));
				}
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi lấy trang sản phẩm khuyến mãi: " + e.getMessage());
		}
		return ds;
	}
}
