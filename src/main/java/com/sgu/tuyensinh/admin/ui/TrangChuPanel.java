package com.sgu.tuyensinh.admin.ui;

import org.springframework.stereotype.Component;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

@Component
public class TrangChuPanel extends JPanel {

    public TrangChuPanel() {
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(new Color(245, 246, 250)); // Màu nền xám nhạt hiện đại

        initComponents();
    }

    private void initComponents() {
        // 1. Header (Welcome banner)
        JPanel headerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("🎓 SGU Admission System 2026");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(new Color(47, 53, 66));

        JLabel lblSubtitle = new JLabel("Nền tảng quản trị tuyển sinh đại học (Phiên bản nâng cấp)");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblSubtitle.setForeground(new Color(116, 125, 140));

        headerPanel.add(lblTitle);
        headerPanel.add(lblSubtitle);

        // 2. Thống kê nhanh (Stats Grid - 4 Thẻ)
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(new EmptyBorder(20, 0, 20, 0));

        statsPanel.add(createStatCard("Tổng Thí Sinh", "4,231", new Color(102, 126, 234), new Color(118, 75, 162)));
        statsPanel.add(createStatCard("Đã Trúng Tuyển", "0", new Color(46, 213, 115), new Color(46, 213, 115)));
        statsPanel.add(createStatCard("Bị Loại", "10,898", new Color(255, 71, 87), new Color(255, 107, 129)));
        statsPanel.add(createStatCard("Ưu Tiên PT1", "18", new Color(255, 165, 2), new Color(255, 127, 80)));

        // 3. Khung thông tin hệ thống (Overview Card)
        JPanel infoCard = new JPanel();
        infoCard.setLayout(new BoxLayout(infoCard, BoxLayout.Y_AXIS));
        infoCard.setBackground(Color.WHITE);
        // Dùng FlatLaf để bo góc card
        infoCard.putClientProperty("FlatLaf.style", "arc: 15");
        infoCard.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblOverviewTitle = new JLabel("Tổng quan hệ thống");
        lblOverviewTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblOverviewTitle.setBorder(new EmptyBorder(0, 0, 15, 0));

        String infoText = "<html>" +
                "<p><b>Version:</b> 3.0 (Cập nhật cho Đồ án năm 3)</p><br>" +
                "<p><b>Các tính năng cốt lõi:</b></p>" +
                "<ul>" +
                "<li>Đọc file Excel bằng thư viện Apache POI.</li>" +
                "<li>Tối ưu hóa đa luồng (SwingWorker) để chống treo giao diện.</li>" +
                "<li>Kiến trúc 1 Transaction an toàn.</li>" +
                "<li>Thuật toán xử lý 6 bước xét tuyển tự động hóa.</li>" +
                "</ul>" +
                "</html>";
        JLabel lblInfo = new JLabel(infoText);
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        infoCard.add(lblOverviewTitle);
        infoCard.add(lblInfo);

        // Add tất cả vào TrangChuPanel
        JPanel topWrapper = new JPanel(new BorderLayout());
        topWrapper.setOpaque(false);
        topWrapper.add(headerPanel, BorderLayout.NORTH);
        topWrapper.add(statsPanel, BorderLayout.CENTER);

        add(topWrapper, BorderLayout.NORTH);
        add(infoCard, BorderLayout.CENTER);
    }

    // Hàm tiện ích tạo thẻ thống kê có Gradient giả lập (Màu phẳng hiện đại)
    private JPanel createStatCard(String title, String value, Color bgColor, Color borderColor) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(bgColor);
        card.putClientProperty("FlatLaf.style", "arc: 15");
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 4, 0, borderColor),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblValue.setForeground(Color.WHITE);

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblTitle.setForeground(new Color(255, 255, 255, 200));

        card.add(lblValue, BorderLayout.CENTER);
        card.add(lblTitle, BorderLayout.SOUTH);

        return card;
    }
}