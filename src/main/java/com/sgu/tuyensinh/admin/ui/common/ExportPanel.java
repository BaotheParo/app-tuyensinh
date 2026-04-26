package com.sgu.tuyensinh.admin.ui.common;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.function.Consumer;
// Panel con để xuất file, được đặt trong JDialog. Chỉ lo phần giao diện, còn logic xử lí dữ liệu sẽ nằm ở bên ngoài (được truyền vào qua callback)
public class ExportPanel extends JPanel {
    private JTextField folderPathField;
    private JButton btnChooseFolder;
    private JButton btnExport;

    // Callback để bên ngoài truyền vào
    private Consumer<String> exportHandler;

    public ExportPanel(Consumer<String> exportHandler) {
        this.exportHandler = exportHandler;

        setLayout(new BorderLayout());
        folderPathField = new JTextField();
        btnChooseFolder = new JButton("Chọn thư mục...");
        btnExport = new JButton("Xuất báo cáo");

        JPanel top = new JPanel(new BorderLayout());
        top.add(folderPathField, BorderLayout.CENTER);
        top.add(btnChooseFolder, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
        add(btnExport, BorderLayout.SOUTH);

        // Chọn thư mục
        btnChooseFolder.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                folderPathField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        // Xuất file
        btnExport.addActionListener(e -> {
            String folder = folderPathField.getText();
            if (folder.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn thư mục lưu!");
                return;
            }
            // Gọi callback để bên ngoài xử lý dữ liệu
            exportHandler.accept(folder);
            // Sau khi xuất thành công thì đóng dialog cha
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) {
                window.dispose();
            }

        });
    }
}

// Cách dùng khi gọi xuất file// xử lí nằm ở ngoài exportPanel(chỉ giao diện)



//  List<Student> danhSachTrungTuyen = Arrays.asList(
//                                 new Student("Nguyễn Văn A", "CNTT", 8.5),
//                                 new Student("Trần Thị B", "Kinh tế", 7.2),
//                                 new Student("Lê Văn C", "Ngôn ngữ Anh", 7.8));
//                 btnPrint.addActionListener(e -> {
//                         JDialog dialog = new JDialog((Frame) null, "Xuất báo cáo", true);

//                         ExportPanel exportPanel = new ExportPanel(folder -> {
//                                 try {
//                                         File file = new File(folder, "danh_sach_trung_tuyen.csv");
//                                         PrintWriter writer = new PrintWriter(file);

//                                         writer.println("Họ tên, Ngành, Điểm");
//                                         for (Student s : danhSachTrungTuyen) {
//                                                 writer.println(s.getName() + "," + s.getMajor() + "," + s.getScore());
//                                         }

//                                         writer.close();
//                                         JOptionPane.showMessageDialog(dialog, "Xuất danh sách trúng tuyển thành công!");
//                                 } catch (Exception ex) {
//                                         JOptionPane.showMessageDialog(dialog, "Lỗi khi xuất: " + ex.getMessage());
//                                 }
//                         });

//                         dialog.add(exportPanel);
//                         dialog.pack();
//                         dialog.setLocationRelativeTo(this);
//                         dialog.setVisible(true);
//                 });


//          public class Student {
//                 private String name;
//                 private String major;
//                 private double score;

//                 public Student(String name, String major, double score) {
//                         this.name = name;
//                         this.major = major;
//                         this.score = score;
//                 }

//                 public String getName() {
//                         return name;
//                 }

//                 public String getMajor() {
//                         return major;
//                 }

//                 public double getScore() {
//                         return score;
//                 }
//         }        