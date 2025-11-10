package todolist;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.Stack;

public class Manager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/todolist database";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";
    private ConcurrentMap<Integer, Task> tasksCache = new ConcurrentHashMap<>();
    private Stack<Task> undoStack = new Stack<>();
    public Manager() {
        createTableIfNotExists();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    private void createTableIfNotExists() {
        String createSql = "CREATE TABLE IF NOT EXISTS tasks ("
                         + "id INT AUTO_INCREMENT PRIMARY KEY,"
                         + "title VARCHAR(255) NOT NULL,"
                         + "description TEXT,"
                         + "due_date DATE NOT NULL,"
                         + "time VARCHAR(10) DEFAULT '12:00 AM',"
                         + "priority INT NOT NULL DEFAULT 2,"
                         + "completed BOOLEAN NOT NULL DEFAULT FALSE)";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createSql);
            System.out.println("Database table 'tasks' checked/created successfully.");
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addTask(String title, String description, String dueDate, String time, int priority) {
        String sql = "INSERT INTO tasks (title, description, due_date, time, priority, completed) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setString(3, dueDate);
            pstmt.setString(4, time);
            pstmt.setInt(5, priority);
            pstmt.setBoolean(6, false);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        Task newTask = new Task(id, title, description, dueDate, time, priority, false);
                        tasksCache.put(id, newTask);
                        System.out.println("Task added successfully with ID: " + id);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding task: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateTask(int id, String title, String description, String dueDate, String time, int priority, boolean completed) {
        String sql = "UPDATE tasks SET title = ?, description = ?, due_date = ?, time = ?, priority = ?, completed = ? WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setString(3, dueDate);
            pstmt.setString(4, time);
            pstmt.setInt(5, priority);
            pstmt.setBoolean(6, completed);
            pstmt.setInt(7, id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                Task updatedTask = new Task(id, title, description, dueDate, time, priority, completed);
                tasksCache.put(id, updatedTask);
                System.out.println("Task updated successfully. ID: " + id);
            }
        } catch (SQLException e) {
            System.err.println("Error updating task: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void deleteTask(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                tasksCache.remove(id);
                System.out.println("Task deleted successfully. ID: " + id);
            }
        } catch (SQLException e) {
            System.err.println("Error deleting task: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void markTask(int id, boolean completed) {
        String sql = "UPDATE tasks SET completed = ? WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, completed);
            pstmt.setInt(2, id);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                Task task = tasksCache.get(id);
                if (task != null) {
                    task.setCompleted(completed);
                }
                System.out.println("Task status updated successfully. ID: " + id);
            }
        } catch (SQLException e) {
            System.err.println("Error marking task complete: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void getAllTasks() {
        String sql = "SELECT id, title, description, due_date, time, priority, completed FROM tasks";
        tasksCache.clear();

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String description = rs.getString("description");
                String dueDate = rs.getString("due_date");
                String time = rs.getString("time");
                int priority = rs.getInt("priority");
                boolean completed = rs.getBoolean("completed");

                Task task = new Task(id, title, description, dueDate, time, priority, completed);
                tasksCache.put(id, task);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all tasks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Task> getTasksCache() {
        return new ArrayList<>(tasksCache.values());
    }

    public static class TaskComparator implements Comparator<Task> {
        @Override
        public int compare(Task t1, Task t2) {
            if (t1.isCompleted() != t2.isCompleted()) {
                return t1.isCompleted() ? 1 : -1;
            }

            if (t1.getPriority() != t2.getPriority()) {
                return Integer.compare(t1.getPriority(), t2.getPriority());
            }

            int dateComparison = t1.getDueDate().compareTo(t2.getDueDate());
            if (dateComparison != 0) {
                return dateComparison;
            }

            return t1.getTime().compareTo(t2.getTime());
        }
    }

    public void sortTasksInCache() {
        List<Task> sortedTasks = new ArrayList<>(tasksCache.values());
        Collections.sort(sortedTasks, new TaskComparator());
        tasksCache.clear();
        for (Task task : sortedTasks) {
            tasksCache.put(task.getId(), task);
        }
    }

    public List<Task> filterTasksByDate(String dateString) {
        List<Task> filtered = tasksCache.values().stream()
                .filter(task -> task.getDueDate().equals(dateString))
                .collect(Collectors.toList());

        Collections.sort(filtered, new TaskComparator());
        return filtered;
    }

    public void pushToUndoStack(Task task) {
        undoStack.push(task);
    }

    public Task undoDelete() {
        if (undoStack.isEmpty()) {
            return null;
        }

        Task restoredTask = undoStack.pop();
        addTask(restoredTask.getTitle(),
                restoredTask.getDescription(),
                restoredTask.getDueDate(),
                restoredTask.getTime(),
                restoredTask.getPriority());
         return restoredTask;
    }
}