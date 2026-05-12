package com.sgu.tuyensinh.admin.ui.common;

import com.sgu.tuyensinh.service.dto.RowErrorDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
// Dialog hiện lỗi sau khi import xong, có thể xuất lỗi ra file Excel
public class ErrorLogDialog extends JDialog {

    private static final Color BG = new Color(0xF9F9F8);
    private static final Color SURFACE = Color.WHITE;
    private static final Color BORDER = new Color(0xE0DFDA);
    private static final Color TEXT_MAIN = new Color(0x1A1A1A);
    private static final Color BLUE = new Color(0x1874D2);

    private final List<RowErrorDTO> errors;

    public ErrorLogDialog(Window owner, String fileName, List<RowErrorDTO> errors) {
        super(owner, "Kết quả import — " + fileName, ModalityType.APPLICATION_MODAL);
        this.errors = errors;

        setBackground(BG);
        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(560, 380));

        add(buildHeader(fileName), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    // ── Header ───────────────────────────────────────────────
    private JPanel buildHeader(String fileName) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 12));
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        JLabel lbl = new JLabel("KẾT QUẢ IMPORT — " + fileName);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 13));
        lbl.setForeground(TEXT_MAIN);
        panel.add(lbl);
        return panel;
    }

    // ── Table ────────────────────────────────────────────────
    // ── Table ────────────────────────────────────────────────
    private JScrollPane buildTable() {
        String[] cols = { "Dòng", "CCCD / Mã", "Mã lỗi", "Chi tiết" }; // ← 4 cột
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        for (RowErrorDTO e : errors) { // ← dùng RowErrorDTO
            model.addRow(new Object[] {
                    e.getRowNumber(),
                    e.getIdentifier(),
                    e.getErrorCode(),
                    e.getDetail()
            });
        }

        JTable table = new JTable(model);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setRowHeight(24);
        table.getTableHeader().setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setGridColor(BORDER);
        table.setShowGrid(true);

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(120);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(220);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        return scroll;
    }

    // ── Footer ───────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));

        JButton exportBtn = makeButton("Xuất file lỗi .xlsx", true);
        exportBtn.addActionListener(e -> exportErrors());

        JButton closeBtn = makeButton("Đóng", false);
        closeBtn.addActionListener(e -> dispose());

        panel.add(exportBtn);
        panel.add(closeBtn);
        return panel;
    }

    // ── Export lỗi ra Excel ──────────────────────────────────
    private void exportErrors() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File("errors.xlsx"));
        chooser.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter("Excel files", "xlsx"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

            org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("Lỗi");
            String[] headers = { "Dòng", "CCCD / Mã", "Mã lỗi", "Chi tiết" };

            // Header row
            org.apache.poi.ss.usermodel.Row hRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++)
                hRow.createCell(i).setCellValue(headers[i]);

            // Data rows
            int rowIdx = 1;
            for (RowErrorDTO e : errors) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(e.getRowNumber());
                row.createCell(1).setCellValue(e.getIdentifier());
                row.createCell(2).setCellValue(e.getErrorCode());
                row.createCell(3).setCellValue(e.getDetail());
            }

            // Auto-size
            for (int i = 0; i < headers.length; i++)
                sheet.autoSizeColumn(i);

            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(chooser.getSelectedFile())) {
                wb.write(fos);
            }

            MessageDialog.showInfo("Đã xuất file thành công!");

        } catch (Exception ex) {
            MessageDialog.showError("Xuất file thất bại: " + ex.getMessage());

        }
    }

    // ── Helpers ──────────────────────────────────────────────
    private JButton makeButton(String text, boolean primary) {
        Color BLUE_DARK = BLUE.darker();
        Color SEC_BG = new Color(0xEEEDE8);
        Color SEC_BDR = new Color(0xC8C7C0);

        JButton btn = new JButton(text) {
            private boolean hovered = false;
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        hovered = true;
                        repaint();
                    }

                    public void mouseExited(java.awt.event.MouseEvent e) {
                        hovered = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (primary) {
                    g2.setColor(hovered ? BLUE_DARK : BLUE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } else {
                    g2.setColor(SEC_BG);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(SEC_BDR);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("SansSerif", Font.PLAIN, 13));
        btn.setForeground(primary ? Color.WHITE : TEXT_MAIN);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 16, 6, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** Gọi từ ImportPanel.done() nếu có lỗi */
    public static void showIfNeeded(Window owner, String fileName, List<RowErrorDTO> errors) {
        if (errors == null || errors.isEmpty())
            return;
        new ErrorLogDialog(owner, fileName, errors).setVisible(true);
    }
}