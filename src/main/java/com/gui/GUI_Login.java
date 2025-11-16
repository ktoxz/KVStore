package com.gui;

import com.dao.DAO_NhanVien;
import com.entity.NhanVien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GUI_Login extends JPanel {

    private JFrame parentFrame;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblForgot;

    public GUI_Login() {

        setLayout(new BorderLayout());

        // ================= LEFT IMAGE =================
        JPanel left = new JPanel(new BorderLayout());
        left.setPreferredSize(new Dimension(520, 600));
        left.setBackground(new Color(245, 247, 255));

        ImageIcon icon = new ImageIcon("src/main/resources/login_img.png");
        Image img = icon.getImage().getScaledInstance(420, 420, Image.SCALE_SMOOTH);
        JLabel lbImg = new JLabel(new ImageIcon(img));
        lbImg.setHorizontalAlignment(SwingConstants.CENTER);
        lbImg.setBorder(new EmptyBorder(40, 0, 40, 0));

        left.add(lbImg, BorderLayout.CENTER);

        // ================= RIGHT FORM =================
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(Color.WHITE);
        right.setBorder(new EmptyBorder(40, 50, 40, 50));

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.insets = new Insets(10, 0, 10, 0);
        gc.anchor = GridBagConstraints.WEST;

        JLabel lblTitle = new JLabel("Đăng nhập");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));

        JLabel lblUser = new JLabel("Username");
        txtUsername = new JTextField(22);

        JLabel lblPass = new JLabel("Password");
        txtPassword = new JPasswordField(22);

        btnLogin = new JButton("Đăng nhập");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(0, 122, 255));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setPreferredSize(new Dimension(140, 38));
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        lblForgot = new JLabel("<html><u>Quên mật khẩu?</u></html>");
        lblForgot.setForeground(new Color(0, 122, 255));
        lblForgot.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        txtPassword.addActionListener(e -> btnLogin.doClick());

        gc.gridy = 0;    right.add(lblTitle, gc);
        gc.gridy++;      right.add(lblUser, gc);
        gc.gridy++;      right.add(txtUsername, gc);
        gc.gridy++;      right.add(lblPass, gc);
        gc.gridy++;      right.add(txtPassword, gc);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setBackground(Color.WHITE);
        row.add(btnLogin);
        row.add(Box.createHorizontalStrut(12));
        row.add(lblForgot);

        gc.gridy++;
        right.add(row, gc);

        add(left, BorderLayout.WEST);
        add(right, BorderLayout.EAST);

        btnLogin.addActionListener(e -> handleLogin());
    }

    public void setParentFrame(JFrame f) {
        this.parentFrame = f;
    }

    private void handleLogin() {

        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập đầy đủ thông tin!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DAO_NhanVien dao = new DAO_NhanVien();
        NhanVien nv = dao.checkLogin(user, pass);

        if (nv == null) {
            JOptionPane.showMessageDialog(this,
                    "Sai tên đăng nhập hoặc mật khẩu!",
                    "Thông báo", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // ===== LOGIN BY ACTIVATION =====
        if (dao.isLoginWithActivationPassword(user, pass)) {

            JPanel p = new JPanel(new GridLayout(0, 1, 5, 5));
            JPasswordField np = new JPasswordField();
            JPasswordField cf = new JPasswordField();
            p.add(new JLabel("Mật khẩu mới:")); p.add(np);
            p.add(new JLabel("Xác nhận mật khẩu:")); p.add(cf);

            int r = JOptionPane.showConfirmDialog(this, p,
                    "Đổi mật khẩu lần đầu",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (r != JOptionPane.OK_OPTION) return;

            String newPass = new String(np.getPassword());
            String confirm = new String(cf.getPassword());

            if (newPass.isBlank() || confirm.isBlank()) {
                JOptionPane.showMessageDialog(this,
                        "Không được để trống!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!newPass.equals(confirm)) {
                JOptionPane.showMessageDialog(this,
                        "Mật khẩu không khớp!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean ok = dao.changePasswordFromActivation(user, pass, newPass);
            if (!ok) {
                JOptionPane.showMessageDialog(this,
                        "Đổi mật khẩu thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(this,
                    "Đổi mật khẩu thành công! Đăng nhập lại.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);

            txtPassword.setText("");
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Đăng nhập thành công! Chào " + nv.getTenNV());

        openMainWindow(nv);
    }

    private void openMainWindow(NhanVien nv) {

        if (parentFrame == null) {
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof JFrame) parentFrame = (JFrame) w;
        }

        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("KVStore - Quản lý cửa hàng");
            f.setContentPane(new GUI_General(nv));
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setExtendedState(JFrame.MAXIMIZED_BOTH);
            f.setResizable(false);
            f.setVisible(true);

            if (parentFrame != null) parentFrame.dispose();
        });
    }
}
