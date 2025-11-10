package todolist;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Enumeration;
import java.util.Calendar;
import com.toedter.calendar.JCalendar;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JDayChooser;

public class ToDoAppMAIN extends JFrame {
    private Manager manager = new Manager();

    private DefaultTableModel model;
    private JTable table;
    private JLabel statusBar;
    private List<Task> currentViewTasks;

    private JCalendar permanentCalendar;

    private final String APP_FONT = "Verdana";
    private Color APP_GREY = new Color(34, 34, 34);
    private Color LIGHT_GREY_BGD = new Color(51, 51, 51);
    private Color APP_WHITE = Color.WHITE;
    private Color TASK_HIGHLIGHT_COLOR = new Color(180, 180, 180);
    private Color DARK_ACCENT = new Color(100, 100, 100);
    private final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    private final Color BUTTON_DEFAULT = LIGHT_GREY_BGD;

    public ToDoAppMAIN() {
        try {
            UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());

            UIManager.put("control", APP_GREY);
            UIManager.put("text", APP_WHITE);
            UIManager.put("Panel.background", APP_GREY);
            UIManager.put("Label.foreground", APP_WHITE);
            UIManager.put("OptionPane.background", LIGHT_GREY_BGD);
            UIManager.put("OptionPane.messageForeground", APP_WHITE);
            UIManager.put("ComboBox.background", LIGHT_GREY_BGD);
            UIManager.put("TextField.background", LIGHT_GREY_BGD);
            UIManager.put("TextArea.background", LIGHT_GREY_BGD);
            UIManager.put("TitledBorder.titleColor", APP_WHITE);

            UIManager.put("TextField.foreground", APP_WHITE);
            UIManager.put("TextArea.foreground", APP_WHITE);

            UIManager.put("Button.background", LIGHT_GREY_BGD);
            UIManager.put("Button.foreground", APP_WHITE);
            UIManager.put("Button.select", DARK_ACCENT);
            UIManager.put("Button.focus", APP_GREY);
            UIManager.put("Button.darkShadow", APP_GREY);
            UIManager.put("Button.light", APP_GREY);
            UIManager.put("Button.highlight", APP_GREY);

            UIManager.put("List.background", APP_GREY);
            UIManager.put("List.foreground", APP_WHITE);
            UIManager.put("List.selectionBackground", TASK_HIGHLIGHT_COLOR);
            UIManager.put("List.selectionForeground", APP_GREY);

            UIManager.put("ComboBox.selectionBackground", DARK_ACCENT);
            UIManager.put("ComboBox.buttonBackground", LIGHT_GREY_BGD);
            UIManager.put("ComboBox.foreground", APP_WHITE);

            UIManager.put("ScrollBar.background", APP_GREY);
            UIManager.put("ScrollBar.track", LIGHT_GREY_BGD);
            UIManager.put("ScrollBar.thumb", DARK_ACCENT);
            UIManager.put("ScrollBar.thumbHighlight", DARK_ACCENT);
            UIManager.put("ScrollBar.thumbShadow", APP_GREY);
            UIManager.put("ScrollBar.inactiveForeground", APP_GREY);
            UIManager.put("ScrollBar.inactiveBackground", APP_GREY);

            UIManager.put("Table.background", APP_GREY);
            UIManager.put("Table.foreground", APP_WHITE);
            UIManager.put("TableHeader.background", LIGHT_GREY_BGD);
            UIManager.put("TableHeader.foreground", APP_WHITE);
            UIManager.put("Table.selectionBackground", DARK_ACCENT);
            UIManager.put("Table.selectionForeground", APP_WHITE);

            for (Enumeration<Object> keys = UIManager.getDefaults().keys(); keys.hasMoreElements();) {
                Object key = keys.nextElement();
                Object value = UIManager.get(key);
                if (value instanceof Font) {
                    UIManager.put(key, new Font(APP_FONT, Font.PLAIN, 14));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to set Custom Look and Feel: " + e.getMessage());
        }

        setTitle("Scheduler To-Do List (MySQL DB Connected)");
        setSize(1200, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createSidePanel(), createMainContentPanel());
        splitPane.setDividerLocation(350);
        splitPane.setDividerSize(1);
        splitPane.setBorder(null);

        statusBar = new JLabel("Welcome to your Database-Connected Task Scheduler");
        statusBar.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        statusBar.setForeground(APP_WHITE);
        statusBar.setBackground(APP_GREY);
        statusBar.setOpaque(true);

        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        loadAllTasksAndDecorate();

        setVisible(true);
    }

    private JPanel createSidePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(APP_GREY);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0;

        JLabel header = new JLabel("TASKS");
        header.setHorizontalAlignment(SwingConstants.CENTER);
        header.setFont(new Font(APP_FONT, Font.BOLD, 14));
        header.setForeground(APP_WHITE);
        gbc.gridy = 0;
        gbc.weighty = 0.0;
        gbc.ipady = 10;
        panel.add(header, gbc);

        gbc.ipady = 0;

        String[] columns = {"ID", "Title", "Date & Time"};
        model = new DefaultTableModel(columns, 0);
        table = new JTable(model) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (currentViewTasks != null && row < currentViewTasks.size()) {
                    Task task = currentViewTasks.get(row);
                    boolean completed = task.isCompleted();

                    c.setBackground(APP_GREY);
                    c.setForeground(APP_WHITE);

                    if (completed) {
                        c.setForeground(Color.GRAY);
                        if (column == 1) {
                             c.setFont(c.getFont().deriveFont(Font.ITALIC));
                        }
                    } else {
                         c.setFont(c.getFont().deriveFont(Font.PLAIN));
                    }

                    if (isRowSelected(row)) {
                        c.setBackground(DARK_ACCENT);
                        c.setForeground(APP_WHITE);
                    }
                }
                return c;
            }
        };
        table.setFont(new Font(APP_FONT, Font.PLAIN, 14));
        table.setRowHeight(28);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow >= 0) {
                        showTaskDetails(selectedRow);
                    }
                }
            }
        });

        JTableHeader headerTable = table.getTableHeader();
        headerTable.setFont(new Font(APP_FONT, Font.BOLD, 13));
        headerTable.setBackground(LIGHT_GREY_BGD);
        headerTable.setForeground(APP_WHITE);
        headerTable.setReorderingAllowed(false);
        headerTable.setResizingAllowed(false);

        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(DARK_ACCENT);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(APP_GREY);
        scrollPane.setBorder(BorderFactory.createLineBorder(LIGHT_GREY_BGD.darker(), 1));

        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 15, 0);
        panel.add(scrollPane, gbc);

        gbc.gridy = 2; gbc.insets = new Insets(10, 0, 10, 0);
        JSeparator separator1 = new JSeparator();
        separator1.setForeground(LIGHT_GREY_BGD);
        gbc.weighty = 0.0;
        panel.add(separator1, gbc);
        gbc.ipady = 15;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(8, 0, 8, 0);

        JButton addBtn = createButton("Add New Task", BUTTON_DEFAULT);
        JButton editBtn = createButton("Edit Selected Task", BUTTON_DEFAULT);
        JButton completeBtn = createButton("Toggle Complete", BUTTON_DEFAULT);
        JButton deleteBtn = createButton("Delete Selected", BUTTON_DEFAULT);
        JButton undoBtn = createButton("Undo", BUTTON_DEFAULT);
        JButton viewAllBtn = createButton("View All Tasks", BUTTON_DEFAULT);

        gbc.gridy = 3; panel.add(addBtn, gbc);
        gbc.gridy = 4; panel.add(editBtn, gbc);
        gbc.gridy = 5; panel.add(completeBtn, gbc);
        gbc.gridy = 6; panel.add(deleteBtn, gbc);
        gbc.gridy = 7; panel.add(undoBtn, gbc);
        gbc.gridy = 8; panel.add(viewAllBtn, gbc);

        addBtn.addActionListener(e -> addTask());
        editBtn.addActionListener(e -> editTask());
        completeBtn.addActionListener(e -> markComplete());
        deleteBtn.addActionListener(e -> deleteTask());
        undoBtn.addActionListener(e -> undoDelete());
        viewAllBtn.addActionListener(e -> refreshTable());

        return panel;
    }

    private JPanel createMainContentPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(APP_GREY);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        permanentCalendar = new JCalendar();
        permanentCalendar.setFont(new Font(APP_FONT, Font.PLAIN, 18));
        permanentCalendar.setWeekOfYearVisible(false);
        permanentCalendar.setBackground(APP_GREY);
        permanentCalendar.setForeground(APP_WHITE);

        JDayChooser dayChooser = permanentCalendar.getDayChooser();
        dayChooser.setBackground(APP_GREY);
        dayChooser.setForeground(APP_WHITE);
        dayChooser.setDecorationBackgroundColor(APP_GREY.brighter());
        dayChooser.setSundayForeground(Color.RED.brighter());
        dayChooser.setWeekdayForeground(Color.LIGHT_GRAY);

        permanentCalendar.getDayChooser().addPropertyChangeListener("day", evt -> {
            filterTasksByDate(permanentCalendar.getDate());
        });

        permanentCalendar.getMonthChooser().setFont(new Font(APP_FONT, Font.BOLD, 16));
        permanentCalendar.getYearChooser().setFont(new Font(APP_FONT, Font.BOLD, 16));


        JPanel calendarPanel = new JPanel(new BorderLayout());
        calendarPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(LIGHT_GREY_BGD.darker(), 1),
            "CALENDAR",
            javax.swing.border.TitledBorder.CENTER,
            javax.swing.border.TitledBorder.TOP,
            new Font(APP_FONT, Font.BOLD, 16),
            APP_WHITE
        ));
        calendarPanel.setBackground(APP_GREY);
        calendarPanel.add(permanentCalendar, BorderLayout.CENTER);

        JPanel legendPanel = createPriorityLegend();
        calendarPanel.add(legendPanel, BorderLayout.SOUTH);

        panel.add(calendarPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createPriorityLegend() {
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        legendPanel.setBackground(APP_GREY);
        legendPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        legendPanel.add(createLegendItem("High Priority", Color.RED.darker()));
        legendPanel.add(createLegendItem("Medium Priority", Color.ORANGE.darker()));
        legendPanel.add(createLegendItem("Low Priority", Color.GREEN.darker()));

        return legendPanel;
    }

    private JLabel createLegendItem(String text, Color color) {
        JLabel label = new JLabel("● " + text);
        label.setForeground(color);
        label.setFont(new Font(APP_FONT, Font.BOLD, 12));
        return label;
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font(APP_FONT, Font.BOLD, 16));
        button.setBackground(color);
        button.setForeground(APP_WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(color.darker(), 1));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.brighter().darker());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        return button;
    }

    private void loadAllTasksAndDecorate() {
        try {
            manager.getAllTasks();
            currentViewTasks = manager.getTasksCache();
            manager.sortTasksInCache();
            updateTable(currentViewTasks);
            statusBar.setText("All tasks loaded successfully from the database. Total: " + currentViewTasks.size());
        } catch (Exception e) {
            statusBar.setText("ERROR: Failed to connect to database. Check console for details.");
            System.err.println("Load Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateTable(List<Task> tasks) {
        model.setRowCount(0);
        currentViewTasks = tasks;

        for (Task task : tasks) {
            model.addRow(new Object[]{
                task.getId(),
                task.getTitle(),
                task.getDateTimeDisplay()
            });
        }

        if (tasks.isEmpty()) {
             table.clearSelection();
        }
    }

    private void refreshTable() {
        permanentCalendar.setDate(new Date());
        loadAllTasksAndDecorate();
        statusBar.setText("Displaying all tasks.");
    }

    private void filterTasksByDate(Date date) {
        String dateString = SDF.format(date);

        try {
            List<Task> filtered = manager.filterTasksByDate(dateString);
            updateTable(filtered);
            statusBar.setText("Filtered tasks for date: " + dateString + ". Count: " + filtered.size());
        } catch (Exception e) {
            statusBar.setText("ERROR: Failed to filter tasks by date.");
            System.err.println("Filter Error: " + e.getMessage());
        }
    }

    private String[] createHourOptions() {
        String[] hours = new String[12];
        for (int i = 0; i < 12; i++) {
            hours[i] = String.format("%02d", i == 0 ? 12 : i);
        }
        return hours;
    }

    private String[] createMinuteOptions() {
        String[] minutes = new String[60];
        for (int i = 0; i < 60; i++) {
            minutes[i] = String.format("%02d", i);
        }
        return minutes;
    }

    private void addTask() {
        JTextField titleField = new JTextField();
        JTextArea descField = new JTextArea(3, 20);
        JDateChooser dateChooser = new JDateChooser(permanentCalendar.getDate());
        JComboBox<String> priorityComboBox = new JComboBox<>(new String[]{"1 - High", "2 - Medium", "3 - Low"});
        priorityComboBox.setSelectedIndex(1);

        JComboBox<String> hourComboBox = new JComboBox<>(createHourOptions());
        JComboBox<String> minuteComboBox = new JComboBox<>(createMinuteOptions());
        JComboBox<String> ampmComboBox = new JComboBox<>(new String[]{"AM", "PM"});

        Calendar now = Calendar.getInstance();
        hourComboBox.setSelectedIndex(now.get(Calendar.HOUR) == 0 ? 11 : now.get(Calendar.HOUR) - 1);
        minuteComboBox.setSelectedIndex(now.get(Calendar.MINUTE));
        ampmComboBox.setSelectedItem(now.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM");

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        timePanel.setBackground(LIGHT_GREY_BGD);
        timePanel.add(hourComboBox);
        timePanel.add(new JLabel(" : "));
        timePanel.add(minuteComboBox);
        timePanel.add(ampmComboBox);

        JScrollPane descScrollPane = new JScrollPane(descField);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 10));
        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Due Date:"));
        panel.add(dateChooser);
        panel.add(new JLabel("Time:"));
        panel.add(timePanel);
        panel.add(new JLabel("Priority:"));
        panel.add(priorityComboBox);
        panel.add(new JLabel("Description:"));
        panel.add(descScrollPane);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Task",
                                                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            String description = descField.getText().trim();
            String dueDate = SDF.format(dateChooser.getDate());
            int priority = priorityComboBox.getSelectedIndex() + 1;

            String hour = (String) hourComboBox.getSelectedItem();
            String minute = (String) minuteComboBox.getSelectedItem();
            String ampm = (String) ampmComboBox.getSelectedItem();
            String time = String.format("%s:%s %s", hour, minute, ampm);

            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            manager.addTask(title, description, dueDate, time, priority);

            filterTasksByDate(permanentCalendar.getDate());
            statusBar.setText("Task '" + title + "' added with time " + time + " and saved to database.");
        }
    }

    private void editTask() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            statusBar.setText("Please select a task to edit.");
            return;
        }

        Task selectedTask = currentViewTasks.get(selectedRow);

        JTextField titleField = new JTextField(selectedTask.getTitle());
        JTextArea descField = new JTextArea(selectedTask.getDescription(), 3, 20);

        Date initialDate = null;
        try {
            initialDate = SDF.parse(selectedTask.getDueDate());
        } catch (Exception ex) {
            System.err.println("Error parsing date: " + ex.getMessage());
            initialDate = new Date();
        }
        JDateChooser dateChooser = new JDateChooser(initialDate);

        JComboBox<String> priorityComboBox = new JComboBox<>(new String[]{"1 - High", "2 - Medium", "3 - Low"});
        priorityComboBox.setSelectedIndex(selectedTask.getPriority() - 1);

        JComboBox<String> hourComboBox = new JComboBox<>(createHourOptions());
        JComboBox<String> minuteComboBox = new JComboBox<>(createMinuteOptions());
        JComboBox<String> ampmComboBox = new JComboBox<>(new String[]{"AM", "PM"});

        String taskTime = selectedTask.getTime();
        String[] timeParts = taskTime.split(" ");
        String timeOnly = timeParts[0];
        String taskAmpm = timeParts.length > 1 ? timeParts[1] : "AM";
        String[] hmParts = timeOnly.split(":");
        String taskHour = hmParts[0];
        String taskMinute = hmParts[1];

        hourComboBox.setSelectedItem(taskHour);
        minuteComboBox.setSelectedItem(taskMinute);
        ampmComboBox.setSelectedItem(taskAmpm);

        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        timePanel.setBackground(LIGHT_GREY_BGD);
        timePanel.add(hourComboBox);
        timePanel.add(new JLabel(" : "));
        timePanel.add(minuteComboBox);
        timePanel.add(ampmComboBox);

        JScrollPane descScrollPane = new JScrollPane(descField);

        JCheckBox completedCheckbox = new JCheckBox("Task Completed", selectedTask.isCompleted());
        completedCheckbox.setBackground(LIGHT_GREY_BGD);
        completedCheckbox.setForeground(APP_WHITE);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 10));
        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Due Date:"));
        panel.add(dateChooser);
        panel.add(new JLabel("Time:"));
        panel.add(timePanel);
        panel.add(new JLabel("Priority:"));
        panel.add(priorityComboBox);
        panel.add(new JLabel("Description:"));
        panel.add(descScrollPane);
        panel.add(new JLabel("Status:"));
        panel.add(completedCheckbox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Task #" + selectedTask.getId(),
                                                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            String description = descField.getText().trim();
            String dueDate = SDF.format(dateChooser.getDate());
            int priority = priorityComboBox.getSelectedIndex() + 1;
            boolean completed = completedCheckbox.isSelected();

            String hour = (String) hourComboBox.getSelectedItem();
            String minute = (String) minuteComboBox.getSelectedItem();
            String ampm = (String) ampmComboBox.getSelectedItem();
            String newTime = String.format("%s:%s %s", hour, minute, ampm);

            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            manager.updateTask(selectedTask.getId(), title, description, dueDate, newTime, priority, completed);

            filterTasksByDate(permanentCalendar.getDate());
            statusBar.setText("Task ID " + selectedTask.getId() + " updated successfully in database.");
        }
    }

    private void markComplete() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            statusBar.setText("Please select a task to mark its status.");
            return;
        }

        Task selectedTask = currentViewTasks.get(selectedRow);
        boolean newStatus = !selectedTask.isCompleted();

        manager.markTask(selectedTask.getId(), newStatus);

        filterTasksByDate(permanentCalendar.getDate());
            statusBar.setText("Task ID " + selectedTask.getId() + " status updated in database.");
    }

    private void deleteTask() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            statusBar.setText("Please select a task to delete.");
            return;
        }

        Task selectedTask = currentViewTasks.get(selectedRow);

        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete task: " + selectedTask.getTitle() + "?\n(You can undo this immediately.)",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            manager.pushToUndoStack(selectedTask);

            manager.deleteTask(selectedTask.getId());

            filterTasksByDate(permanentCalendar.getDate());
            statusBar.setText("Task '" + selectedTask.getTitle() + "' deleted from DB and saved to Undo Stack.");
        }
    }

    private void undoDelete() {
        Task restoredTask = manager.undoDelete();

        if (restoredTask != null) {
            filterTasksByDate(permanentCalendar.getDate());
            statusBar.setText("Task '" + restoredTask.getTitle() + "' restored via Undo. It has a new database ID.");
        } else {
            statusBar.setText("Undo Stack is empty. Nothing to restore.");
        }
    }

    private void showTaskDetails(int selectedRow) {
        if (selectedRow < 0 || selectedRow >= currentViewTasks.size()) return;

        Task task = currentViewTasks.get(selectedRow);

        String priorityLabel = switch (task.getPriority()) {
            case 1 -> "HIGH";
            case 2 -> "MEDIUM";
            case 3 -> "LOW";
            default -> "N/A";
        };

        String message = String.format(
            "<html><body style='width: 300px; padding: 10px; font-family: %s;'>" +
            "<h3 style='color: #42a5f5;'>Task Details</h3>" +
            "<b>Title:</b> %s<br>" +
            "<b>ID:</b> %d<br>" +
            "<b>Due Date:</b> %s<br>" +
            "<b>Time:</b> %s<br>" +
            "<b>Priority:</b> <span style='color: %s;'>%s</span><br>" +
            "<b>Status:</b> <span style='color: %s;'>%s</span><br><br>" +
            "<b>Description:</b><br>%s" +
            "</body></html>",
            APP_FONT,
            task.getTitle(),
            task.getId(),
            task.getDueDate(),
            task.getTime(),
            (task.getPriority() == 1 ? "red" : task.getPriority() == 2 ? "orange" : "green"),
            priorityLabel,
            (task.isCompleted() ? "green" : "orange"),
            (task.isCompleted() ? "Completed" : "Pending"),
            task.getDescription().replace("\n", "<br>")
        );

        JOptionPane.showMessageDialog(this, message, "Task Details", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ToDoAppMAIN());
    }
}