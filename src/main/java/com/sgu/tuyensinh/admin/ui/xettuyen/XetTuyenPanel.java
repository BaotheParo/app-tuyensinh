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
    private JLabel lblTrungTuyen;
    private JLabel lblTruot;
    private JLabel lblDangXet;
    private JLabel lblStatus;

    public XetTuyenPanel(AdmissionService admissionService, NguyenVongRepository nguyenVongRepository) {
        this.admissionService = admissionService;
        this.nguyenVongRepository = nguyenVongRepository;
        initComponents();
        loadSummary();
    }

    private void initComponents() {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JLabel lblTitle = new JLabel("XET TUYEN & LOC AO", SwingConstants.CENTER);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 24));
        add(lblTitle, BorderLayout.NORTH);

        // Center Panel (Button + Stats)
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;

        btnRun = new JButton("\u25B6 Chay Thuat Toan Xet Tuyen");
        btnRun.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnRun.setBackground(new Color(40, 167, 69));
        btnRun.setForeground(Color.WHITE);
        btnRun.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRun.addActionListener(this::runAdmission);
        centerPanel.add(btnRun, gbc);

        gbc.gridy = 1;
        lblStatus = new JLabel("Trang thai: San sang");
        lblStatus.setForeground(Color.GRAY);
        centerPanel.add(lblStatus, gbc);

        // Stats Panel
        gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel statsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        statsPanel.setBorder(BorderFactory.createTitledBorder(null, "Ket qua Xet Tuyen (Summary)", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("SansSerif", Font.BOLD, 14)));

        lblTrungTuyen = new JLabel("\u2705 TRUNG_TUYEN: 0");
        lblTrungTuyen.setFont(new Font("SansSerif", Font.PLAIN, 16));
        lblTrungTuyen.setForeground(new Color(0, 128, 0));

        lblTruot = new JLabel("\u274C TRUOT: 0");
        lblTruot.setFont(new Font("SansSerif", Font.PLAIN, 16));
        lblTruot.setForeground(Color.RED);

        lblDangXet = new JLabel("\u23F3 DANG_XET: 0");
        lblDangXet.setFont(new Font("SansSerif", Font.PLAIN, 16));
        lblDangXet.setForeground(Color.ORANGE);

        statsPanel.add(lblTrungTuyen);
        statsPanel.add(lblTruot);
        statsPanel.add(lblDangXet);

        centerPanel.add(statsPanel, gbc);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void loadSummary() {
        long trungTuyen = nguyenVongRepository.countByNvKetQua("TRUNG_TUYEN");
        long truot = nguyenVongRepository.countByNvKetQua("TRUOT");
        long dangXet = nguyenVongRepository.countByNvKetQua("DANG_XET");

        lblTrungTuyen.setText("\u2705 TRUNG_TUYEN: " + trungTuyen);
        lblTruot.setText("\u274C TRUOT: " + truot);
        lblDangXet.setText("\u23F3 DANG_XET / KHONG_HOP_LE: " + dangXet);
    }

    private void runAdmission(ActionEvent evt) {
        int confirm = JOptionPane.showConfirmDialog(this, "Ban co chac chan muon chay lai thuat toan loc ao?\nQua trinh nay co the mat vai giay den vai phut tuy theo du lieu.", "Xac nhan", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        btnRun.setEnabled(false);
        lblStatus.setText("Trang thai: Dang chay thuat toan, vui long doi...");
        lblStatus.setForeground(Color.BLUE);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                admissionService.runAdmissionProcess();
                return null;
            }

            @Override
            protected void done() {
                btnRun.setEnabled(true);
                setCursor(Cursor.getDefaultCursor());
                try {
                    get();
                    lblStatus.setText("Trang thai: Xet tuyen thanh cong!");
                    lblStatus.setForeground(new Color(0, 128, 0));
                    loadSummary();
                    JOptionPane.showMessageDialog(XetTuyenPanel.this, "Chay xet tuyen va loc ao thanh cong!", "Thong bao", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    lblStatus.setText("Trang thai: Co loi xay ra!");
                    lblStatus.setForeground(Color.RED);
                    JOptionPane.showMessageDialog(XetTuyenPanel.this, "Loi khi xet tuyen: " + e.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}