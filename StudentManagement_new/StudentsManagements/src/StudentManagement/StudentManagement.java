package StudentManagement;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
// Code nhanh goc
// --- 1. MODEL: LỚP SINH VIÊN ---
class Student {
    public static final double DIEM_GIOI = 8.0;
    public static final double DIEM_KHA = 6.5;
    public static final double DIEM_TB = 5.0;

    private int id;
    private String name;
    private double score;

    public Student(int id, String name, double score) {
        this.id = id;
        this.name = name.trim();
        this.score = score;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public double getScore() { return score; }
    
    // Thêm Setters để phục vụ chức năng Sửa
    public void setName(String name) { this.name = name.trim(); }
    public void setScore(double score) { this.score = score; }

    public String getRank() {
        if (score >= DIEM_GIOI) return "Giỏi";
        if (score >= DIEM_KHA) return "Khá";
        if (score >= DIEM_TB) return "Trung Bình";
        return "Yếu";
    }
}

// --- 2. VIEW & CONTROLLER ---
public class StudentManagement extends JFrame {

    private JTable table;
    private DefaultTableModel tableModel;
    private List<Student> studentList;
    
    private JTextField txtName, txtScore, txtSearch;
    private JLabel lblStats;
    private int currentIdCounter = 6;
    private Collator viCollator = Collator.getInstance(new Locale("vi", "VN"));

    public StudentManagement() {
        initData();
        initUI();
        loadTableData(""); // Load dữ liệu ban đầu
    }

    private void initUI() {
        setTitle("Hệ Thống Quản Lý Sinh Viên - DTU Professional");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));

        // --- PANEL TOP: NHẬP LIỆU & TÌM KIẾM ---
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        
        // Khu vực nhập liệu
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Thông tin chi tiết"));
        
        inputPanel.add(new JLabel("Họ và Tên:"));
        txtName = new JTextField(18);
        inputPanel.add(txtName);
        
        inputPanel.add(new JLabel("Điểm số:"));
        txtScore = new JTextField(6);
        inputPanel.add(txtScore);

        JButton btnAdd = new JButton("Thêm Mới");
        btnAdd.setBackground(new Color(46, 204, 113)); btnAdd.setForeground(new Color(46, 204, 113));
        
        JButton btnUpdate = new JButton("Cập Nhật");
        btnUpdate.setBackground(new Color(52, 152, 219)); btnUpdate.setForeground(new Color(52, 152, 219));

        JButton btnDelete = new JButton("Xóa Chọn");
        btnDelete.setBackground(new Color(231, 76, 60)); btnDelete.setForeground(new Color(231, 76, 60));

        inputPanel.add(btnAdd);
        inputPanel.add(btnUpdate);
        inputPanel.add(btnDelete);

        // Khu vực Tìm kiếm
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 50, 10));
        searchPanel.add(new JLabel("🔍 Tìm kiếm ID:"));
        txtSearch = new JTextField(25);
        searchPanel.add(txtSearch);
        
        topPanel.add(inputPanel);
        topPanel.add(searchPanel);
        add(topPanel, BorderLayout.NORTH);

        // --- PANEL CENTER: BẢNG DỮ LIỆU ---
        String[] columnNames = {"ID", "Họ và Tên", "Điểm Số", "Học Lực"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        
        // Căn giữa số liệu
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // --- PANEL SOUTH: SẮP XẾP & THỐNG KÊ ---
        JPanel southPanel = new JPanel(new BorderLayout());
        
        JPanel sortPanel = new JPanel(new FlowLayout());
        sortPanel.setBorder(BorderFactory.createTitledBorder("Công cụ sắp xếp"));
        JButton btnSortAZ = new JButton("Tên A-Z");
        JButton btnSortZA = new JButton("Tên Z-A");
        JButton btnSortHigh = new JButton("Điểm Cao ↓");
        JButton btnSortLow = new JButton("Điểm Thấp ↑");
        sortPanel.add(btnSortAZ); sortPanel.add(btnSortZA);
        sortPanel.add(btnSortHigh); sortPanel.add(btnSortLow);

        lblStats = new JLabel("Thống kê: Đang tải...");
        lblStats.setFont(new Font("SansSerif", Font.ITALIC, 14));
        lblStats.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        southPanel.add(sortPanel, BorderLayout.NORTH);
        southPanel.add(lblStats, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

        // --- XỬ LÝ SỰ KIỆN ---

        // 1. Khi click vào bảng: Hiện thông tin lên ô nhập
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                txtName.setText(table.getValueAt(row, 1).toString());
                txtScore.setText(table.getValueAt(row, 2).toString());
            }
        });

        // 2. Thêm mới
        btnAdd.addActionListener(e -> {
            if(validateInput()) {
                studentList.add(new Student(currentIdCounter++, txtName.getText(), Double.parseDouble(txtScore.getText())));
                clearAndReload();
            }
        });

        // 3. Cập nhật
        btnUpdate.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 dòng để sửa!");
                return;
            }
            if(validateInput()) {
                int id = (int) table.getValueAt(row, 0);
                for (Student s : studentList) {
                    if (s.getId() == id) {
                        s.setName(txtName.getText());
                        s.setScore(Double.parseDouble(txtScore.getText()));
                        break;
                    }
                }
                clearAndReload();
            }
        });

        // 4. Xóa
        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1 && JOptionPane.showConfirmDialog(this, "Xóa sinh viên này?", "Xác nhận", 0) == 0) {
                int id = (int) table.getValueAt(row, 0);
                studentList.removeIf(s -> s.getId() == id);
                clearAndReload();
            }
        });

        // 5. Tìm kiếm Real-time
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { loadTableData(txtSearch.getText()); }
        });

        // 6. Sắp xếp
        btnSortAZ.addActionListener(e -> {
            studentList.sort((s1, s2) -> viCollator.compare(extractName(s1.getName()), extractName(s2.getName())));
            loadTableData("");
        });
        btnSortHigh.addActionListener(e -> {
            studentList.sort((s1, s2) -> Double.compare(s2.getScore(), s1.getScore()));
            loadTableData("");
        });
        btnSortZA.addActionListener(e -> {
            // Đảo ngược vị trí s2 và s1 để sắp xếp ngược lại
            studentList.sort((s1, s2) -> viCollator.compare(extractName(s2.getName()), extractName(s1.getName())));
            loadTableData("");
        });
        btnSortLow.addActionListener(e -> {
            // Đảo ngược vị trí s1 và s2 so với nút High để sắp tăng dần
            studentList.sort((s1, s2) -> Double.compare(s1.getScore(), s2.getScore()));
            loadTableData("");
        });

        getRootPane().setDefaultButton(btnAdd);
    }

    private boolean validateInput() {
        try {
            if (txtName.getText().trim().isEmpty()) throw new Exception("Tên trống!");
            double sc = Double.parseDouble(txtScore.getText());
            if (sc < 0 || sc > 10) throw new Exception("Điểm 0-10!");
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Dữ liệu không hợp lệ: " + e.getMessage());
            return false;
        }
    }

    private void clearAndReload() {
        txtName.setText(""); txtScore.setText("");
        loadTableData("");
        txtName.requestFocus();
    }

    private void loadTableData(String query) {
    tableModel.setRowCount(0);
    double sum = 0;
    int count = 0;

    for (Student s : studentList) {
        if (String.valueOf(s.getId()).contains(query.trim())) {
            tableModel.addRow(new Object[]{s.getId(), s.getName(), s.getScore(), s.getRank()});
            sum += s.getScore();
            count++;
        }
    }
    
    double avg = (count == 0) ? 0 : sum / count;
    lblStats.setText(String.format("📊 Thống kê: %d sinh viên | Điểm TB: %.2f", count, avg));
}

    private String extractName(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        return parts[parts.length - 1];
    }

    private void initData() {
        studentList = new ArrayList<>();
        studentList.add(new Student(1, "Nguyễn Khôi Nguyên", 8.5));
        studentList.add(new Student(2, "Trần Văn Nguyên", 6.0));
        studentList.add(new Student(3, "Lê Đại Lộc", 9.0));
        studentList.add(new Student(4, "Phạm Thanh Phúc", 7.5));
        studentList.add(new Student(5, "Đỗ Nhật Nam", 4.0));
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new StudentManagement().setVisible(true));
    }
}