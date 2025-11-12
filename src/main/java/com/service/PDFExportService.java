// java
package com.service;

import com.entity.HoaDon;
import com.entity.CT_HoaDon;
import com.entity.KhachHang;
import org.xhtmlrenderer.pdf.ITextRenderer;
import com.lowagie.text.pdf.BaseFont;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

public class PDFExportService {

    private static final DecimalFormat df = new DecimalFormat("#,###");
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Xuất hóa đơn ra file PDF
     * @param hoaDon Đối tượng hóa đơn
     * @param khachHang Thông tin khách hàng
     * @param tenNhanVien Tên nhân viên
     * @param tienGiamTuDiem Số tiền giảm từ điểm tích lũy
     * @param diemDaSuDung Số điểm đã sử dụng
     * @return Đường dẫn file PDF đã tạo
     */
    public static String xuatHoaDonPDF(HoaDon hoaDon, KhachHang khachHang, String tenNhanVien,
                                       double tienGiamTuDiem, int diemDaSuDung) {
        try {
            // Tạo thư mục HoaDon nếu chưa có
            File hoaDonDir = new File("HoaDon");
            if (!hoaDonDir.exists()) {
                hoaDonDir.mkdirs();
            }

            // Tạo tên file PDF
            String fileName = "HoaDon/HD_" + hoaDon.getMaHoaDon() + ".pdf";

            // Tạo HTML content
            String htmlContent = generateHTML(hoaDon, khachHang, tenNhanVien, tienGiamTuDiem, diemDaSuDung);

            // Xuất PDF
            OutputStream os = new FileOutputStream(fileName);
            ITextRenderer renderer = new ITextRenderer();

            // 1) Load font from resources and register to renderer
            // Put a Unicode TTF (e.g. NotoSans-Regular.ttf) into src/main/resources/fonts/
            // 1) Load and register font
            InputStream fontIs = PDFExportService.class.getResourceAsStream("/fonts/DejaVuSans.ttf");
            if (fontIs != null) {
                File tmpFont = File.createTempFile("dejavu-", ".ttf");
                tmpFont.deleteOnExit();
                try (FileOutputStream fos = new FileOutputStream(tmpFont)) {
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = fontIs.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                } finally {
                    fontIs.close();
                }
                // Register Unicode font
                renderer.getFontResolver().addFont(tmpFont.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            }


            // 2) Provide a base URL so relative resources (if any) resolve; use classpath root
            String baseUrl = PDFExportService.class.getResource("/").toString();

            renderer.setDocumentFromString(htmlContent, baseUrl);
            renderer.layout();
            renderer.createPDF(os);
            os.close();

            return fileName;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Tạo HTML content cho hóa đơn
     */
    private static String generateHTML(HoaDon hoaDon, KhachHang khachHang, String tenNhanVien,
                                       double tienGiamTuDiem, int diemDaSuDung) {
        StringBuilder html = new StringBuilder();

        // Tính toán các giá trị
        double tongTien = 0;
        for (CT_HoaDon ct : hoaDon.getChiTietList()) {
            tongTien += ct.getThanhTien();
        }

        double vat = hoaDon.getThue();
        double tongCong = tongTien + vat;
        double thucThu = tongCong - tienGiamTuDiem;
        double tienThoi = hoaDon.getTienKhach() - thucThu;
        if (tienThoi < 0) tienThoi = 0;

        // Tính điểm tích lũy mới (chỉ từ tổng tiền gốc, không tính VAT)
        int diemTichLuyMoi = (int)(tongTien / 1000);

        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'/>");
        html.append("<style>");
        // Use the registered font family as primary
        html.append("body { font-family: 'DejaVu Sans', sans-serif; margin: 40px; }");
        html.append(".header { text-align: center; margin-bottom: 30px; }");
        html.append(".header h1 { color: #2c3e50; margin: 5px 0; font-size: 28px; }");
        html.append(".header h2 { color: #e74c3c; margin: 5px 0; font-size: 24px; }");
        html.append(".info { margin-bottom: 20px; }");
        html.append(".info-row { margin: 8px 0; font-size: 14px; }");
        html.append(".info-row strong { display: inline-block; width: 180px; }");
        html.append("table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
        html.append("th { background-color: #3498db; color: white; padding: 12px; text-align: left; font-size: 14px; }");
        html.append("td { padding: 10px; border-bottom: 1px solid #ddd; font-size: 13px; }");
        html.append("tr:hover { background-color: #f5f5f5; }");
        html.append(".text-right { text-align: right; }");
        html.append(".text-center { text-align: center; }");
        html.append(".summary { margin-top: 20px; float: right; width: 400px; }");
        html.append(".summary-row { display: flex; justify-content: space-between; padding: 8px 0; font-size: 14px; }");
        html.append(".summary-row.total { font-size: 18px; font-weight: bold; color: #e74c3c; border-top: 2px solid #333; margin-top: 10px; padding-top: 15px; }");
        html.append(".summary-row.discount { color: #27ae60; font-weight: bold; }");
        html.append(".footer { clear: both; margin-top: 50px; text-align: center; font-size: 12px; color: #7f8c8d; border-top: 1px solid #ddd; padding-top: 20px; }");
        html.append(".thank-you { text-align: center; margin-top: 30px; font-size: 16px; font-weight: bold; color: #3498db; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");

        // Header
        html.append("<div class='header'>");
        html.append("<h1>CỬA HÀNG TIỆN LỢI KV STORE</h1>");
        html.append("<h2>HÓA ĐƠN BÁN HÀNG</h2>");
        html.append("<p style='margin: 5px 0; font-size: 13px;'>Địa chỉ: 12 Nguyễn Văn Bảo, Phường 4, Quận Gò Vấp, TP.HCM</p>");
        html.append("<p style='margin: 5px 0; font-size: 13px;'>Hotline: 0123 456 789 | Email: kvstore@gmail.com</p>");
        html.append("</div>");

        // Thông tin hóa đơn
        html.append("<div class='info'>");
        html.append("<div class='info-row'><strong>Mã hóa đơn:</strong> " + hoaDon.getMaHoaDon() + "</div>");
        html.append("<div class='info-row'><strong>Ngày:</strong> " + hoaDon.getNgayGiaoDich().format(dateFormatter) + "</div>");
        html.append("<div class='info-row'><strong>Nhân viên:</strong> " + tenNhanVien + "</div>");
        html.append("<div class='info-row'><strong>Khách hàng:</strong> " +
                (khachHang != null ? khachHang.getTenKH() : "Khách vãng lai") + "</div>");

        if (khachHang != null) {
            html.append("<div class='info-row'><strong>Số điện thoại:</strong> " + khachHang.getSdt() + "</div>");
        }

        html.append("</div>");

        // Bảng sản phẩm
        html.append("<table>");
        html.append("<thead>");
        html.append("<tr>");
        html.append("<th class='text-center' style='width: 50px;'>STT</th>");
        html.append("<th>Tên sản phẩm</th>");
        html.append("<th class='text-center' style='width: 80px;'>Số lượng</th>");
        html.append("<th class='text-right' style='width: 120px;'>Đơn giá</th>");
        html.append("<th class='text-right' style='width: 120px;'>Thành tiền</th>");
        html.append("</tr>");
        html.append("</thead>");
        html.append("<tbody>");

        int stt = 1;
        for (CT_HoaDon ct : hoaDon.getChiTietList()) {
            double donGiaSau = ct.getGiaSP(); // giá sau khuyến mãi (đã lưu)
            double donGiaGoc = (ct.getSanPham() != null ? ct.getSanPham().getGiaSP() : donGiaSau);
            int sl = ct.getSoLuong();
            double thanhTienSau = sl * donGiaSau;
            double thanhTienGoc = sl * donGiaGoc;
            boolean coGiamGia = thanhTienSau < thanhTienGoc - 0.001;

            html.append("<tr>");
            html.append("<td class='text-center'>" + stt++ + "</td>");
            html.append("<td>" + ct.getTenSP() + "</td>");
            html.append("<td class='text-center'>" + sl + "</td>");

            // Đơn giá: chỉ hiển thị giá sau khuyến mãi (không gạch)
            html.append("<td class='text-right'>" + df.format(donGiaSau) + "đ</td>");

            // Thành tiền: gạch tổng gốc nếu có giảm
            if (coGiamGia) {
                html.append("<td class='text-right'>" +
                        "<div style='font-size:13px; color:#6c757d; text-decoration: line-through;'>" + df.format(thanhTienGoc) + "đ</div>" +
                        "<div style='font-size:13px; color:#e74c3c; font-weight:bold;'>" + df.format(thanhTienSau) + "đ</div>" +
                        "</td>");
            } else {
                html.append("<td class='text-right'>" + df.format(thanhTienSau) + "đ</td>");
            }
            html.append("</tr>");
        }

        html.append("</tbody>");
        html.append("</table>");

        // Tổng kết
        html.append("<div class='summary'>");
        html.append("<div class='summary-row'><span>Tổng tiền:</span><span>" + df.format(tongTien) + "đ</span></div>");
        html.append("<div class='summary-row'><span>VAT (8%):</span><span>" + df.format(vat) + "đ</span></div>");
        html.append("<div class='summary-row'><span>Tổng cộng:</span><span>" + df.format(tongCong) + "đ</span></div>");

        if (tienGiamTuDiem > 0) {
            html.append("<div class='summary-row discount'><span>Giảm từ điểm (-" + diemDaSuDung + " điểm):</span><span>-" + df.format(tienGiamTuDiem) + "đ</span></div>");
            html.append("<div class='summary-row total'><span>Thực thu:</span><span>" + df.format(thucThu) + "đ</span></div>");
        } else {
            html.append("<div class='summary-row total'><span>Tổng thanh toán:</span><span>" + df.format(thucThu) + "đ</span></div>");
        }

        html.append("<div class='summary-row'><span>Tiền khách đưa:</span><span>" + df.format(hoaDon.getTienKhach()) + "đ</span></div>");
        html.append("<div class='summary-row'><span>Tiền thối:</span><span>" + df.format(tienThoi) + "đ</span></div>");

        if (khachHang != null) {
            html.append("<div class='summary-row' style='color: #27ae60; margin-top: 15px; font-weight: bold;'>");
            html.append("<span>Điểm tích lũy được:</span><span>+" + diemTichLuyMoi + " điểm</span>");
            html.append("</div>");
        }

        html.append("</div>");

        // Cảm ơn
        html.append("<div class='thank-you'>");
        html.append("Cảm ơn quý khách! Hẹn gặp lại!");
        html.append("</div>");

        // Footer
        html.append("<div class='footer'>");
        html.append("<p>Hóa đơn được in tự động từ hệ thống quản lý cửa hàng tiện lợi</p>");
        html.append("<p>Vui lòng giữ hóa đơn để đổi/trả hàng trong vòng 7 ngày</p>");
        html.append("</div>");

        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }
}
