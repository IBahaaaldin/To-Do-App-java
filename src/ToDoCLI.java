import java.io.*;
import java.util.*;

public class ToDoCLI {
    static final String FILE_PATH = "data/tasks.txt";

    static class Task {
        String title;
        int priority;
        String dueDate;
        boolean completed;

        Task(String title, int priority, String dueDate, boolean completed) {
            this.title = title;
            this.priority = priority;
            this.dueDate = dueDate;
            this.completed = completed;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s (Priority: %d, Due: %s)", completed ? "✓" : "✗", title, priority, dueDate);
        }

        String serialize() {
            return title + ";" + priority + ";" + dueDate + ";" + completed;
        }

        static Task deserialize(String line) {
            String[] parts = line.split(";");
            return new Task(parts[0], Integer.parseInt(parts[1]), parts[2], Boolean.parseBoolean(parts[3]));
        }
    }

    static List<Task> tasks = new ArrayList<>();
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        loadTasks();

        while (true) {
            System.out.println("\n==== To-Do List CLI ====");
            System.out.println("1. View Tasks");
            System.out.println("2. Add Task");
            System.out.println("3. Mark as Complete");
            System.out.println("4. Delete Task");
            System.out.println("5. Sort Tasks");
            System.out.println("6. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    viewTasks();
                    break;
                case "2":
                    addTask();
                    break;
                case "3":
                    markComplete();
                    break;
                case "4":
                    deleteTask();
                    break;
                case "5":
                    sortTasks();
                    break;
                case "6":
                    saveTasks();
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    static void loadTasks() {
        tasks.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                tasks.add(Task.deserialize(line));
            }
        } catch (IOException e) {
            // Ignore if file not found
        }
    }

    static void saveTasks() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (Task task : tasks) {
                writer.println(task.serialize());
            }
        } catch (IOException e) {
            System.out.println("Failed to save tasks.");
        }
    }

    static void viewTasks() {
        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }
        for (int i = 0; i < tasks.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, tasks.get(i));
        }
    }

    static void addTask() {
        System.out.print("Enter title: ");
        String title = scanner.nextLine().trim();
        if (title.isEmpty()) {
            System.out.println("Title cannot be empty.");
            return;
        }

        System.out.print("Enter priority (1-5): ");
        int priority;
        try {
            priority = Integer.parseInt(scanner.nextLine().trim());
            if (priority < 1 || priority > 5)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            System.out.println("Invalid priority. Must be between 1 and 5.");
            return;
        }

        System.out.print("Enter due date (YYYY-MM-DD): ");
        String dueDate = scanner.nextLine().trim();
        if (!dueDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            System.out.println("Invalid date format.");
            return;
        }

        tasks.add(new Task(title, priority, dueDate, false));
        saveTasks();
        System.out.println("Task added.");
    }

    static void markComplete() {
        viewTasks();
        System.out.print("Enter task number to mark complete: ");
        try {
            int idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
            tasks.get(idx).completed = true;
            saveTasks();
            System.out.println("Task marked complete.");
        } catch (Exception e) {
            System.out.println("Invalid selection.");
        }
    }

    static void deleteTask() {
        viewTasks();
        System.out.print("Enter task number to delete: ");
        try {
            int idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
            tasks.remove(idx);
            saveTasks();
            System.out.println("Task deleted.");
        } catch (Exception e) {
            System.out.println("Invalid selection.");
        }
    }

    static void sortTasks() {
        System.out.print("Sort by (priority/due/title): ");
        String sortBy = scanner.nextLine().trim().toLowerCase();
        switch (sortBy) {
            case "priority":
                tasks.sort(Comparator.comparingInt(t -> t.priority));
                break;
            case "due":
                tasks.sort(Comparator.comparing(t -> t.dueDate));
                break;
            case "title":
                tasks.sort(Comparator.comparing(t -> t.title.toLowerCase()));
                break;
            default:
                System.out.println("Invalid sort key.");
                return;
        }
        System.out.println("Tasks sorted by " + sortBy + ".");
        saveTasks();
    }
}
