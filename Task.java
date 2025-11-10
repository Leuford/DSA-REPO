package todolist;

public class Task {
    private int id;
    private String title;
    private String description;
    private String dueDate;
    private String time;
    private int priority;
    private boolean completed;
    public Task(int id, String title, String description, String dueDate, String time, int priority, boolean completed) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.time = time;
        this.priority = priority;
        this.completed = completed;
    }

    public Task(String title, String description, String dueDate, String time, int priority, boolean completed) {
        this(0, title, description, dueDate, time, priority, completed);
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getTime() {
        return time;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getDateTimeDisplay() {
        return this.dueDate + " (" + this.time + ")";
    }
}