package duke.storage;

import duke.task.Task;
import duke.task.ToDo;
import duke.task.Deadline;
import duke.task.Event;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;

/**
 * Storage class to load and save data.
 */
public class Storage {
    private final Path FILE_PATH;

    /**
     * Constructor for Storage.
     * @param filename The name of the file to load and save data.
     */
    public Storage(String filename) {
        this.FILE_PATH = Paths.get(".", "data", filename);
    }

    /**
     * Loads the data from the file.
     * @return The tasks loaded from the file.
     * @throws IOException If an I/O error occurs.
     */
    public ArrayList<Task> load() throws IOException {
        Path directoryPath = FILE_PATH.getParent();
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }
        // Check if file exists, if not create it
        if (!Files.exists(FILE_PATH)) {
            Files.createFile(FILE_PATH);
        }

        // Read file and load data
        List<String> data;
        try {
            data = Files.readAllLines(FILE_PATH);
        } catch (IOException e) {
            System.out.println("An error occurred while reading the file: " + e.getMessage());
            return new ArrayList<>();  // Return an empty duke.task list
        }
        ArrayList<Task> tasks = new ArrayList<>();

        for (String line : data) {
            String[] parts = line.split("\\]", 3);
            if (parts.length < 3) {
                System.out.println("Invalid duke.task format found in Hard Disk");
                continue;
            }
            char type = parts[0].charAt(parts[0].length()- 1);
            char doneStatus = parts[1].charAt(1);
            boolean isDone = doneStatus == 'X';
            String description = parts[2].trim();
            Task task;
            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            switch (type) {
            case 'T':
                task = new ToDo(description);
                if (isDone) {
                    task.markAsDone();
                }
                tasks.add(task);
                break;
            case 'D':
                String[] components = description.split(" \\(by: ");
                String deadlineDescription = components[0].trim();
                String deadlineDateTime = components[1].substring(0, components[1].length() - 1);
                task = new Deadline(deadlineDescription, LocalDateTime.parse(deadlineDateTime, format));
                if (isDone) {
                    task.markAsDone();
                }
                tasks.add(task);
                break;
            case 'E':
                String[] eventParts = description.split(" \\(from: ");
                String eventDescription = eventParts[0].trim();
                String[] eventDateTimes = eventParts[1].split(" to: ");
                String eventStartDateTime = eventDateTimes[0];
                String eventEndDateTime = eventDateTimes[1].substring(0, eventDateTimes[1].length() - 1);
                task = new Event(eventDescription,
                        LocalDateTime.parse(eventStartDateTime, format),
                        LocalDateTime.parse(eventEndDateTime, format));
                if (isDone) task.markAsDone();
                tasks.add(task);
                break;
            }
        }
        return tasks;
    }

    /**
     * Saves the data to the file.
     * @param tasks The tasks to save.
     * @throws IOException If an I/O error occurs.
     */
    public void save(List<Task> tasks) throws IOException {
        Path directoryPath = FILE_PATH.getParent();
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }
        // Check if file exists, if not create it
        if (!Files.exists(FILE_PATH)) {
            Files.createFile(FILE_PATH);
        }
        // Write data to file
        List<String> data = new ArrayList<>();
        for (Task task : tasks) {
            data.add(task.toString());
        }
        Files.write(FILE_PATH, data);
    }

    /**
     * Deletes the file.
     * @throws IOException If an I/O error occurs.
     */
    public void delete() throws IOException {
        Files.delete(FILE_PATH);
    }
}
