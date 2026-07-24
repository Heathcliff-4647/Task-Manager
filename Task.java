public class Task {
    String title;
    String category;
    String deadline;
    String priority;
    boolean completed;

    public Task(String title, String category, String deadline, String priority) {
        this.title = title;
        this.category = category;
        this.deadline = deadline;
        this.priority = priority;
        this.completed = false;
    }

    public String getStatus() {
        return completed ? "Completed" : "Pending";
    }
}
