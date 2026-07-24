import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;

public class TaskManagerGUI {

    JFrame frame;
    JTable table;
    DefaultTableModel model;
    TableRowSorter<DefaultTableModel> sorter; // handles sorting & filtering

    JTextField titleField;
    JTextField categoryField;
    JTextField deadlineField;
    JComboBox<String> priorityBox;

    JTextField searchField;  // search bar
    JLabel statusLabel;      // task counter

    ArrayList<Task> tasks = new ArrayList<>();

    public TaskManagerGUI() {

        frame = new JFrame("Task Manager");
        frame.setSize(800, 560);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(0, 0));

        // ── Table Setup ──────────────────────────────────────────────
        String[] columns = {"Title", "Category", "Deadline", "Priority", "Status"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        // ── TableRowSorter: Sorting + Filtering ──────────────────────
        sorter = new TableRowSorter<>(model);

        // Custom comparator for Priority column (index 3): High > Medium > Low
        sorter.setComparator(3, Comparator.comparingInt(o -> {
            switch (o.toString()) {
                case "High":   return 0;
                case "Medium": return 1;
                case "Low":    return 2;
                default:       return 3;
            }
        }));

        // Deadline column (index 2): YYYY-MM-DD sorts lexicographically = chronologically
        sorter.setComparator(2, Comparator.comparing(Object::toString));

        table.setRowSorter(sorter);

        // ── Row Color Renderer ────────────────────────────────────────
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    // Must convert view row to model row when table is sorted/filtered
                    int modelRow = table.convertRowIndexToModel(row);
                    String status = (String) model.getValueAt(modelRow, 4);
                    if ("Completed".equals(status)) {
                        c.setBackground(new Color(198, 239, 206));
                        c.setForeground(Color.DARK_GRAY);
                    } else {
                        c.setBackground(Color.WHITE);
                        c.setForeground(Color.BLACK);
                    }
                } else {
                    c.setBackground(new Color(184, 207, 229));
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        // Double-click to edit
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) editTask();
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane, BorderLayout.CENTER);

        // ── NORTH: Search bar + Input panel stacked ───────────────────
        JPanel northWrapper = new JPanel(new BorderLayout());

        // Search bar
        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Tasks"));
        searchField = new JTextField();
        searchField.setToolTipText("Type to filter tasks by title...");
        searchPanel.add(new JLabel("  Search: "), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        // Live filter on every keystroke
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e)  { applyFilter(); }
            public void removeUpdate(DocumentEvent e)  { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        // Input panel
        JPanel inputPanel = new JPanel(new GridLayout(2, 5, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add New Task"));

        titleField    = new JTextField();
        categoryField = new JTextField();
        deadlineField = new JTextField();
        String[] priorities = {"High", "Medium", "Low"};
        priorityBox = new JComboBox<>(priorities);

        JButton addButton = new JButton("Add Task");
        addButton.setBackground(new Color(70, 130, 180));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);

        inputPanel.add(new JLabel("Title"));
        inputPanel.add(new JLabel("Category"));
        inputPanel.add(new JLabel("Deadline (YYYY-MM-DD)"));
        inputPanel.add(new JLabel("Priority"));
        inputPanel.add(new JLabel(""));

        inputPanel.add(titleField);
        inputPanel.add(categoryField);
        inputPanel.add(deadlineField);
        inputPanel.add(priorityBox);
        inputPanel.add(addButton);

        northWrapper.add(searchPanel, BorderLayout.NORTH);
        northWrapper.add(inputPanel,  BorderLayout.SOUTH);
        frame.add(northWrapper, BorderLayout.NORTH);

        // ── SOUTH: Action buttons + Status bar ───────────────────────
        JPanel southWrapper = new JPanel(new BorderLayout());

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 8));
        actionPanel.setBorder(BorderFactory.createTitledBorder("Actions"));

        JButton markCompleteButton = new JButton("✔ Mark as Complete");
        markCompleteButton.setBackground(new Color(40, 167, 69));
        markCompleteButton.setForeground(Color.WHITE);
        markCompleteButton.setFocusPainted(false);

        JButton editButton = new JButton("✎ Edit Task");
        editButton.setBackground(new Color(255, 193, 7));
        editButton.setForeground(Color.BLACK);
        editButton.setFocusPainted(false);

        JButton deleteButton = new JButton("✖ Delete Task");
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);

        actionPanel.add(markCompleteButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);

        // Status bar
        statusLabel = new JLabel("  Total: 0  |  Pending: 0  |  Completed: 0");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        statusLabel.setForeground(new Color(50, 50, 50));
        statusLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        southWrapper.add(actionPanel, BorderLayout.NORTH);
        southWrapper.add(statusLabel, BorderLayout.SOUTH);
        frame.add(southWrapper, BorderLayout.SOUTH);

        // ── Button Listeners ──────────────────────────────────────────
        addButton.addActionListener(e -> addTask());
        deleteButton.addActionListener(e -> deleteTask());
        markCompleteButton.addActionListener(e -> markAsComplete());
        editButton.addActionListener(e -> editTask());

        frame.setVisible(true);
    }

    // ── Search Filter ─────────────────────────────────────────────────
    private void applyFilter() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null); // remove filter, show all rows
        } else {
            // (?i) = case-insensitive; column 0 = Title only
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 0));
        }
    }

    // ── Status Bar Updater ────────────────────────────────────────────
    private void updateStatusBar() {
        int total     = tasks.size();
        int completed = (int) tasks.stream().filter(t -> t.completed).count();
        int pending   = total - completed;
        statusLabel.setText("  Total: " + total + "  |  Pending: " + pending + "  |  Completed: " + completed);
    }

    // ── Add Task ──────────────────────────────────────────────────────
    public void addTask() {
        String title    = titleField.getText().trim();
        String category = categoryField.getText().trim();
        String deadline = deadlineField.getText().trim();
        String priority = priorityBox.getSelectedItem().toString();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Title cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Task task = new Task(title, category, deadline, priority);
        tasks.add(task);
        model.addRow(new Object[]{task.title, task.category, task.deadline, task.priority, task.getStatus()});

        titleField.setText("");
        categoryField.setText("");
        deadlineField.setText("");

        updateStatusBar();
    }

    // ── Delete Task ───────────────────────────────────────────────────
    public void deleteTask() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a task to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Convert view index to model index (critical when table is sorted/filtered)
        int modelRow = table.convertRowIndexToModel(viewRow);

        int confirm = JOptionPane.showConfirmDialog(frame,
                "Are you sure you want to delete \"" + model.getValueAt(modelRow, 0) + "\"?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            tasks.remove(modelRow);
            model.removeRow(modelRow);
            updateStatusBar();
        }
    }

    // ── Mark as Complete ──────────────────────────────────────────────
    public void markAsComplete() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a task to mark as complete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        Task task = tasks.get(modelRow);

        if (task.completed) {
            JOptionPane.showMessageDialog(frame, "This task is already completed.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        task.completed = true;
        model.setValueAt("Completed", modelRow, 4);
        table.repaint();
        updateStatusBar();
    }

    // ── Edit Task ─────────────────────────────────────────────────────
    public void editTask() {
        int viewRow = table.getSelectedRow();
        if (viewRow == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a task to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(viewRow);
        Task task = tasks.get(modelRow);

        JDialog dialog = new JDialog(frame, "Edit Task", true);
        dialog.setSize(350, 250);
        dialog.setLocationRelativeTo(frame);
        dialog.setLayout(new GridLayout(5, 2, 8, 8));
        dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField editTitle    = new JTextField(task.title);
        JTextField editCategory = new JTextField(task.category);
        JTextField editDeadline = new JTextField(task.deadline);
        JComboBox<String> editPriority = new JComboBox<>(new String[]{"High", "Medium", "Low"});
        editPriority.setSelectedItem(task.priority);

        dialog.add(new JLabel("Title:"));    dialog.add(editTitle);
        dialog.add(new JLabel("Category:")); dialog.add(editCategory);
        dialog.add(new JLabel("Deadline:")); dialog.add(editDeadline);
        dialog.add(new JLabel("Priority:")); dialog.add(editPriority);

        JButton saveButton = new JButton("Save Changes");
        saveButton.setBackground(new Color(70, 130, 180));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);

        JButton cancelButton = new JButton("Cancel");

        dialog.add(saveButton);
        dialog.add(cancelButton);

        saveButton.addActionListener(e -> {
            String newTitle = editTitle.getText().trim();
            if (newTitle.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Title cannot be empty.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            task.title    = newTitle;
            task.category = editCategory.getText().trim();
            task.deadline = editDeadline.getText().trim();
            task.priority = editPriority.getSelectedItem().toString();

            model.setValueAt(task.title,    modelRow, 0);
            model.setValueAt(task.category, modelRow, 1);
            model.setValueAt(task.deadline, modelRow, 2);
            model.setValueAt(task.priority, modelRow, 3);

            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TaskManagerGUI::new);
    }
}
