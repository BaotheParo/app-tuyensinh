package com.sgu.tuyensinh.admin.ui.xettuyen;

import com.sgu.tuyensinh.service.AdmissionService;
import com.sgu.tuyensinh.repository.NguyenVongRepository;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

@Component
public class XetTuyenPanel extends JPanel {

    private final AdmissionService admissionService;
    private final NguyenVongRepository nguyenVongRepository;

    private JButton btnRun;
    private JButton btnReset;
    private JLabel lblTrungTuyen;
    private JLabel lblTruot;
    private JLabel lblDangXet;
    private JLabel lblStatus;
    private JProgressBar progressBar;

    public XetTuyenPanel(AdmissionService admissionService, NguyenVongRepository nguyenVongRepository) {
        this.admissionService = admissionService;
        this.nguyenVongRepository = nguyenVongRepository;
        initComponents();
        loadSummary();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(248, 249, 250));

        // --- HEADER SECTION ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(new Color(41, 128, 185));
        pnlHeader.setPreferredSize(new Dimension(0, 100));
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(0, 30, 0, 30));

        JLabel lblTitle = new JLabel("XÉT TUYỂN & LỌC ẢO TOÀN CỤC");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JLabel lblSubTitle = new JLabel("Hệ thống tự động tính điểm và phân bổ chỉ tiêu");
        lblSubTitle.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblSubTitle.setForeground(new Color(236, 240, 241));
        pnlHeader.add(lblSubTitle, BorderLayout.SOUTH);

        add(pnlHeader, BorderLayout.NORTH);

        // --- CONTENT SECTION ---
        JPanel pnlContent = new JPanel(new GridBagLayout());
        pnlContent.setOpaque(false);
        pnlContent.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;

        // 1. Action Card
        JPanel pnlActions = createStyledCard("BẢNG ĐIỀU KHIỂN");
        pnlActions.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 20));

        btnRun = new JButton("▶ Chạy Thuật Toán");
        styleButton(btnRun, new Color(46, 204, 113));
        btnRun.addActionListener(this::runAdmission);

        btnReset = new JButton("↺ Reset Kết Quả");
        styleButton(btnReset, new Color(231, 76, 60));
        btnReset.addActionListener(e -> resetResults());

        pnlActions.add(btnRun);
        pnlActions.add(btnReset);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 0.3;
        pnlContent.add(pnlActions, gbc);

        // 2. Status Card
        JPanel pnlStatus = createStyledCard("TRẠNG THÁI HỆ THỐNG");
        pnlStatus.setLayout(new BorderLayout(10, 10));
        pnlStatus.setBorder(BorderFactory.createCompoundBorder(pnlStatus.getBorder(), BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        lblStatus = new JLabel("Sẵn sàng thực hiện xét tuyển cho 64,000+ nguyện vọng.");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pnlStatus.add(lblStatus, BorderLayout.NORTH);

        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(0, 25));
        progressBar.setStringPainted(true);
        pnlStatus.add(progressBar, BorderLayout.CENTER);

        gbc.gridy = 1; gbc.weighty = 0.2;
        pnlContent.add(pnlStatus, gbc);

        // 3. Stats Card (3 small cards inside)
        JPanel pnlStatsGrid = new JPanel(new GridLayout(1, 3, 20, 0));
        pnlStatsGrid.setOpaque(false);

        lblTrungTuyen = createStatLabel("TRÚNG TUYỂN", new Color(39, 174, 96));
        lblTruot = createStatLabel("TRƯỢT", new Color(192, 57, 43));
        lblDangXet = createStatLabel("ĐANG XÉT", new Color(230, 126, 34));

        pnlStatsGrid.add(createStatCard("TRÚNG TUYỂN", lblTrungTuyen, new Color(235, 245, 238)));
        pnlStatsGrid.add(createStatCard("TRƯỢT", lblTruot, new Color(253, 237, 236)));
        pnlStatsGrid.add(createStatCard("ĐANG XÉT", lblDangXet, new Color(254, 245, 231)));

        gbc.gridy = 2; gbc.weighty = 0.5;
        pnlContent.add(pnlStatsGrid, gbc);

        add(new JScrollPane(pnlContent), BorderLayout.CENTER);
    }

    private JPanel createStyledCard(String title) {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
            title, TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12), new Color(127, 140, 141)
        ));
        return card;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color bg) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bg);
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true));
        
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(100, 100, 100));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JLabel createStatLabel(String text, Color color) {
        JLabel label = new JLabel("0", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 36));
        label.setForeground(color);
        return label;
    }

    private void styleButton(JButton btn, Color color) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(200, 45));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void loadSummary() {
        long trungTuyen = nguyenVongRepository.countByNvKetQua("TRUNG_TUYEN");
        long truot = nguyenVongRepository.countByNvKetQua("TRUOT");
        long dangXet = nguyenVongRepository.countByNvKetQua("DANG_XET");

        lblTrungTuyen.setText(String.valueOf(trungTuyen));
        lblTruot.setText(String.valueOf(truot));
        lblDangXet.setText(String.valueOf(dangXet));
    }

    private void runAdmission(ActionEvent evt) {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bắt đầu quy trình xét tuyển toàn cục?\n" +
            "- Tính điểm từng nguyện vọng.\n" +
            "- Phân loại trúng tuyển theo chỉ tiêu.\n" +
            "Quá trình có thể mất vài giây.", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        setUIEnabled(false);
        lblStatus.setText("Đang thực hiện xét tuyển... Vui lòng không đóng ứng dụng.");
        progressBar.setIndeterminate(true);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                admissionService.runAdmissionProcess();
                return null;
            }

            @Override
            protected void done() {
                setUIEnabled(true);
                progressBar.setIndeterminate(false);
                progressBar.setValue(100);
                try {
                    get();
                    lblStatus.setText("Kết thúc quy trình xét tuyển thành công!");
                    loadSummary();
                    JOptionPane.showMessageDialog(XetTuyenPanel.this, "Xét tuyển hoàn tất!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    lblStatus.setText("Lỗi: " + e.getMessage());
                    JOptionPane.showMessageDialog(XetTuyenPanel.this, "Lỗi: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void resetResults() {
        int confirm = JOptionPane.showConfirmDialog(this, "Xóa toàn bộ kết quả trúng tuyển hiện tại?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            admissionService.resetResults();
            loadSummary();
            lblStatus.setText("Đã reset trạng thái xét tuyển về ban đầu.");
            progressBar.setValue(0);
        }
    }

    private void setUIEnabled(boolean enabled) {
        btnRun.setEnabled(enabled);
        btnReset.setEnabled(enabled);
        setCursor(enabled ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }
}