package com.sgu.tuyensinh.admin.ui.common;
<<<<<<< HEAD
=======

>>>>>>> 96ab4ed6e412ff347741ba7f6f80acfd9a80f128
import javax.swing.*;

public class MessageDialog {
    public static void showInfo(String message) {
        JOptionPane.showMessageDialog(null, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    public static void showWarning(String message) {
        JOptionPane.showMessageDialog(null, message, "Cảnh báo", JOptionPane.WARNING_MESSAGE);
    }
}