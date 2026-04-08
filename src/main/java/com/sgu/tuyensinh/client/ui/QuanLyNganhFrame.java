import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class QuanLyNganhFrame extends JFrame {

    // UI Components
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtMaNganh, txtTenNganh, txtChiTieu;
    private JButton btnAdd, btnUpdate, btnDelete, btnPrev, btnNext;
    private JLabel lblPageInfo;

    // Pagination state
    private int currentPage = 0;
    private final int pageSize = 10;

    public QuanLyNganhFrame() {
        setTitle("Quản Lý Ngành - SGU 2026");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        initComponents();
        layoutComponents();
        addEventHandlers();

        // Load initial data
        loadDataFromApi(currentPage);
    }

    private void initComponents() {
        // Table setup
        String[] columns = {"Mã Ngành", "Tên Ngành", "Chỉ Tiêu", "Tổ Hợp"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Chặn edit trực tiếp trên table
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Form inputs
        txtMaNganh = new JTextField(15);
        txtTenNganh = new JTextField(15);
        txtChiTieu = new JTextField(15);

        // Buttons
        btnAdd = new JButton("Thêm Mới");
        btnUpdate = new JButton("Cập Nhật");
        btnDelete = new JButton("Xóa");

        btnPrev = new JButton("<< Trước");
        btnNext = new JButton("Sau >>");
        lblPageInfo = new JLabel("Trang: 1");
    }

    private void layoutComponents() {
        // Top: Form Panel
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin Ngành"));
        formPanel.add(new JLabel("Mã Ngành:")); formPanel.add(txtMaNganh);
        formPanel.add(new JLabel("Tên Ngành:")); formPanel.add(txtTenNganh);
        formPanel.add(new JLabel("Chỉ Tiêu:")); formPanel.add(txtChiTieu);

        // Right: Action Panel
        JPanel actionPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        actionPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        actionPanel.add(btnAdd);
        actionPanel.add(btnUpdate);
        actionPanel.add(btnDelete);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(formPanel, BorderLayout.CENTER);
        topContainer.add(actionPanel, BorderLayout.EAST);

        // Center: Table
        JScrollPane scrollPane = new JScrollPane(table);

        // Bottom: Pagination Panel
        JPanel pagingPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pagingPanel.add(btnPrev);
        pagingPanel.add(lblPageInfo);
        pagingPanel.add(btnNext);

        // Add to Frame
        add(topContainer, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(pagingPanel, BorderLayout.SOUTH);
    }

    private void addEventHandlers() {
        // Xử lý click chuột vào JTable để đổ dữ liệu ngược lên Form
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                txtMaNganh.setText(tableModel.getValueAt(row, 0).toString());
                txtTenNganh.setText(tableModel.getValueAt(row, 1).toString());
                txtChiTieu.setText(tableModel.getValueAt(row, 2).toString());
            }
        });

        // Xử lý phân trang
        btnNext.addActionListener(e -> {
            currentPage++;
            loadDataFromApi(currentPage);
        });

        btnPrev.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                loadDataFromApi(currentPage);
            }
        });
    }

    private void loadDataFromApi(int page) {
        // Gọi Spring Boot API: GET http://localhost:8080/api/nganh?page={page}&size={pageSize}
        // Tạm thời mock data để bạn xem UI
        tableModel.setRowCount(0); // Xóa data cũ
        tableModel.addRow(new Object[]{"CS01", "Kỹ thuật phần mềm", "200", "A00, A01"});
        tableModel.addRow(new Object[]{"BA01", "Quản trị kinh doanh", "150", "A00, D01"});

        lblPageInfo.setText("Trang: " + (page + 1));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new QuanLyNganhFrame().setVisible(true));
    }
}