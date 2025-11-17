package com.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class BankQRGenerator {

    public static void generateBIDVQR(String account, long amount, String addInfo, String filePath)
            throws WriterException, IOException {

        String encodedInfo = java.net.URLEncoder.encode(addInfo, "UTF-8");

        // Tạo URL VietQR
        String qrURL = String.format(
                "https://img.vietqr.io/image/bidv-%s-compact2.png?amount=%d&addInfo=%s",
                account, amount, encodedInfo
        );

        // Tạo mã QR từ URL
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(qrURL, BarcodeFormat.QR_CODE, 400, 400);

        Path path = FileSystems.getDefault().getPath(filePath);
        MatrixToImageWriter.writeToPath(matrix, "PNG", path);
    }
}
