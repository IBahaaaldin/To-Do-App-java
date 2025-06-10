import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class ToDoGUI extends JFrame {
    DefaultListModel<String> listModel = new DefaultListModel<>();
    java.util.List<Task> tasks = new ArrayList<>();
    JList<String> taskList;

    JTextField titleField, priorityField, dueDateField;

    static final String FILE_PATH = "data/tasks.txt";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ToDoGUI().setVisible(true));
    }

    public ToDoGUI() {
        setTitle("To-Do List App");
        setSize(500, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        loadTasks();
        initComponents();
    }

    void initComponents() {
        JPanel inputPanel = new JPanel(new GridLayout(4, 2));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add New Task"));

        inputPanel.add(new JLabel("Title:"));
        titleField = new JTextField();
        inputPanel.add(titleField);

        inputPanel.add(new JLabel("Priority (1–5):"));
        priorityField = new JTextField();
        inputPanel.add(priorityField);

        inputPanel.add(new JLabel("Due Date (YYYY-MM-DD):"));
        dueDateField = new JTextField();
        inputPanel.add(dueDateField);

        JButton addButton = new JButton("Add Task");
        addButton.addActionListener(e -> addTask());
        inputPanel.add(addButton);

        add(inputPanel, BorderLayout.NORTH);

        listModel.clear();
        for (Task t : tasks)
            listModel.addElement(t.toString());

        taskList = new JList<>(listModel);
        add(new JScrollPane(taskList), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton completeButton = new JButton("Mark Complete");
        completeButton.addActionListener(e -> markComplete());
        buttonPanel.add(completeButton);

        JButton deleteButton = new JButton("Delete Task");
        deleteButton.addActionListener(e -> deleteTask());
        buttonPanel.add(deleteButton);

        JButton sortPriority = new JButton("Sort by Priority");
        sortPriority.addActionListener(e -> sortTasks("priority"));
        buttonPanel.add(sortPriority);

        JButton sortDue = new JButton("Sort by Due Date");
        sortDue.addActionListener(e -> sortTasks("due"));
        buttonPanel.add(sortDue);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    void refreshList() {
        listModel.clear();
        for (Task t : tasks)
            listModel.addElement(t.toString());
    }

    void addTask() {
        String title = titleField.getText().trim();
        int priority;
        String dueDate = dueDateField.getText().trim();

        try {
            priority = Integer.parseInt(priorityField.getText().trim());
            if (priority < 1 || priority > 5)
                throw new Exception();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Priority must be between 1 and 5.");
            return;
        }

        if (!dueDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            JOptionPane.showMessageDialog(this, "Invalid due date format.");
            return;
        }

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title cannot be empty.");
            return;
        }

        tasks.add(new Task(title, priority, dueDate, false));
        saveTasks();
        refreshList();
        titleField.setText("");
        priorityField.setText("");
        dueDateField.setText("");
    }

    void markComplete() {
        int idx = taskList.getSelectedIndex();
        if (idx == -1) {
            JOptionPane.showMessageDialog(this, "Select a task to complete.");
            return;
        }
        tasks.get(idx).completed = true;
        saveTasks();
        refreshList();
    }

    void deleteTask() {
        int idx = taskList.getSelectedIndex();
        if (idx == -1) {
            JOptionPane.showMessageDialog(this, "Select a task to delete.");
            return;
        }
        tasks.remove(idx);
        saveTasks();
        refreshList();
    }

    void sortTasks(String by) {
        if (by.equals("priority")) {
            tasks.sort(Comparator.comparingInt(t -> t.priority));
        } else if (by.equals("due")) {
            tasks.sort(Comparator.comparing(t -> t.dueDate));
        }
        saveTasks();
        refreshList();
    }

    void saveTasks() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (Task task : tasks)
                writer.println(task.serialize());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save tasks.");
        }
    }

    void loadTasks() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                tasks.add(Task.deserialize(line));
            }
        } catch (IOException ignored) {
        }
    }

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

        public String toString() {
            return String.format("[%s] %s (Priority: %d, Due: %s)", completed ? "✓" : "✗", title, priority, dueDate);
        }

        public String serialize() {
            return title + ";" + priority + ";" + dueDate + ";" + completed;
        }

        static Task deserialize(String line) {
            String[] parts = line.split(";");
            return new Task(parts[0], Integer.parseInt(parts[1]), parts[2], Boolean.parseBoolean(parts[3]));
        }
    }
}
