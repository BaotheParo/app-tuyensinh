package com.sgu.tuyensinh.admin.ui.common;

import com.sgu.tuyensinh.service.dto.ImportResultDTO;
import com.sgu.tuyensinh.service.interfaces.ProgressCallback;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.util.function.BiFunction;

/**
 * TUẦN 1 — ImportPanel  (Phiên bản đã sửa lỗi tương thích ProgressPanel)
 */
public class ImportPanel extends JPanel {

    // ── Palette ──────────────────────────────────────────────
    private static final Color BG = new Color(0xF9F9F8);
    private static final Color SURFACE = Color.WHITE;
    private static final Color BORDER = new Color(0xE0DFDA);
    private static final Color BLUE = new Color(0x1874D2);
    private static final Color BLUE_LIGHT = new Color(0xE6F1FB);
    private static final Color TEXT_MAIN = new Color(0x1A1A1A);
    private static final Color TEXT_MUTED = new Color(0x6B6B68);
    private static final Color SUCCESS_BG = new Color(0xEAF3DE);
    private static final Color SUCCESS_FG = new Color(0x3B6D11);
    private static final Color DANGER_BG = new Color(0xFCEBEB);
    private static final Color DANGER_FG = new Color(0xA32D2D);

    // ProgressCallback ở đây là
    // com.sgu.tuyensinh.service.interfaces.ProgressCallback
    private final BiFunction<InputStream, ProgressCallback, ImportResultDTO> importFunction;

    private JTextField filePathField;
    private ProgressPanel progressPanel;
    private JLabel lblSuccess, lblError;
    private JPanel resultPanel;

    public ImportPanel(BiFunction<InputStream, ProgressCallback, ImportResultDTO> importFunction) {
        this.importFunction = importFunction;
        setBackground(BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(0, 0, 0, 0));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
    }

    // ── Header ───────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 14));
        header.setBackground(SURFACE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        JLabel icon = new JLabel("↓");
        icon.setFont(new Font("SansSerif", Font.BOLD, 13));
        icon.setForeground(BLUE);
        icon.setOpaque(true);
        icon.setBackground(BLUE_LIGHT);
        icon.setPreferredSize(new Dimension(28, 28));
        icon.setHorizontalAlignment(SwingConstants.CENTER);
        icon.setBorder(new RoundedBorder(6, BLUE_LIGHT));

        JLabel title = new JLabel("Import ngành");
        title.setFont(new Font("SansSerif", Font.PLAIN, 15));
        title.setForeground(TEXT_MAIN);

        header.add(icon);
        header.add(title);
        return header;
    }

    // ── Body ─────────────────────────────────────────────────
    private JPanel buildBody() {
        JPanel body = new JPanel();
        body.setBackground(SURFACE);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(20, 20, 20, 20));

        body.add(buildLabel("Chọn file Excel (.xlsx)"));
        body.add(Box.createVerticalStrut(8));
        body.add(buildFilePicker());
        body.add(Box.createVerticalStrut(16));
        body.add(buildProgressSection());
        body.add(Box.createVerticalStrut(12));
        body.add(buildImportButton());
        body.add(Box.createVerticalStrut(12));

        resultPanel = buildResultPanel();
        resultPanel.setVisible(false);
        body.add(resultPanel);

        return body;
    }

    private JLabel buildLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(TEXT_MUTED);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel buildFilePicker() {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(SURFACE);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        row.setAlignmentX(LEFT_ALIGNMENT);

        filePathField = new JTextField();
        filePathField.setEditable(false);
        filePathField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        filePathField.setForeground(TEXT_MUTED);
        filePathField.setBackground(BG);
        filePathField.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(8, BORDER),
                new EmptyBorder(0, 12, 0, 12)));

        JButton browseBtn = makeButton("Chọn file", false);
        browseBtn.setPreferredSize(new Dimension(90, 36));
        browseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(
                    new javax.swing.filechooser.FileNameExtensionFilter("Excel files", "xlsx", "xls"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                filePathField.setText(chooser.getSelectedFile().getAbsolutePath());
                filePathField.setForeground(TEXT_MAIN);
            }
        });

        row.add(filePathField, BorderLayout.CENTER);
        row.add(browseBtn, BorderLayout.EAST);
        return row;
    }

    private JPanel buildProgressSection() {
        progressPanel = new ProgressPanel();
        progressPanel.setAlignmentX(LEFT_ALIGNMENT);
        return progressPanel;
    }

    private JPanel buildResultPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 8, 0));
        panel.setBackground(SURFACE);
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        lblSuccess = new JLabel("0", SwingConstants.LEFT);
        lblError = new JLabel("0", SwingConstants.LEFT);

        panel.add(buildResultCard("Thành công", lblSuccess, SUCCESS_BG, SUCCESS_FG));
        panel.add(buildResultCard("Lỗi", lblError, DANGER_BG, DANGER_FG));
        return panel;
    }

    private JPanel buildResultCard(String title, JLabel valueLabel, Color bg, Color fg) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(8, bg),
                new EmptyBorder(10, 14, 10, 14)));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setForeground(fg);

        valueLabel.setFont(new Font("SansSerif", Font.PLAIN, 22));
        valueLabel.setForeground(fg);

        card.add(lbl);
        card.add(Box.createVerticalStrut(4));
        card.add(valueLabel);
        return card;
    }

    private JButton buildImportButton() {
        JButton btn = makeButton("Import", true);
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.addActionListener(e -> {
            String path = filePathField.getText().trim();
            if (path.isEmpty()) {
                MessageDialog.showWarning("Vui lòng chọn file trước!");
                return;
            }

            resultPanel.setVisible(false);
            progressPanel.reset();

            ImportWorker worker = new ImportWorker(path, progressPanel, importFunction) {
                // Trong buildImportButton(), phần done()
                @Override
                protected void done() {
                    try {
                        ImportResultDTO result = get();
                        if (result != null) {
                            lblSuccess.setText(String.valueOf(result.getSuccessCount()));
                            lblError.setText(String.valueOf(result.getErrors().size()));
                            resultPanel.setVisible(true);
                            revalidate();
                            repaint();

                            Window w = SwingUtilities.getWindowAncestor(ImportPanel.this);
                            if (w != null)
                                w.pack();

                            // ← Thêm dòng này: hiện dialog lỗi nếu có
                            ErrorLogDialog.showIfNeeded(w, filePathField.getText(), result.getErrors());
                        }
                    } catch (Exception ex) {
                        MessageDialog.showError("Lỗi: " + ex.getMessage());
                    }
                }
            };
            worker.execute();
        });
        return btn;
    }

    // ── Helpers ──────────────────────────────────────────────
    private JButton makeButton(String text, boolean primary) {
        Color secBg = new Color(0xEEEDE8);
        Color secBgHover = new Color(0xE3E2DC);
        Color secBorder = new Color(0xC8C7C0);

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
                    g2.setColor(hovered ? BLUE.darker() : BLUE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } else {
                    g2.setColor(hovered ? secBgHover : secBg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                    g2.setColor(secBorder);
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
        btn.setBorder(new EmptyBorder(0, 14, 0, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // ── Rounded border helper ────────────────────────────────
    static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;

        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
        }
    }

}
