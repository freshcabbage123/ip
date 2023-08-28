package duke.task;

import duke.ui.Ui;
import duke.exception.IllegalTaskIndexException;
import duke.exception.InvalidArgumentException;
import duke.storage.Storage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;


/**
 * Represents a list of tasks.
 */
public class TaskList {

    public enum TaskType {

        TODO {
            @Override
            public Task createTask(String details) {
                return new ToDo(details);
            }
        },
        DEADLINE {
            @Override
            public Task createTask(String details) throws InvalidArgumentException {
                String[] split = details.split(" /by ");
                if (split.length != 2) {
                    throw new InvalidArgumentException("☹ OOPS!!! The deadline format is incorrect. " +
                            "It should be: deadline <name> /by <date> <time>");
                }
                String taskName = split[0], dateTime = split[1];
                return new Deadline(taskName, parseDateTime(dateTime));
            }
        },
        EVENT {
            @Override
            public Task createTask(String details) throws InvalidArgumentException {
                String[] firstSplit = details.split(" /from ");
                String[] secondSplit = firstSplit[firstSplit.length - 1].split(" /to ");
                if (firstSplit.length != 2 || secondSplit.length != 2) {
                    throw new InvalidArgumentException("☹ OOPS!!! The event format is incorrect. " +
                            "It should be: event <name> /from <date> <time> /to <date> <time>");
                }
                String taskName = firstSplit[0], startDateTime = secondSplit[0], endDateTime = secondSplit[1];
                return new Event(taskName, parseDateTime(startDateTime), parseDateTime(endDateTime));
            }
        };

        /**
         * Creates a deadline duke.task.
         * @param details The details of the deadline duke.task.
         * @return The deadline duke.task.
         * @throws InvalidArgumentException If the deadline task's format is invalid.
         */
        public abstract Task createTask(String details) throws InvalidArgumentException;
        private static final DateTimeFormatter[] DATE_TIME_FORMATS = {
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
                DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm"),
        };
        private static LocalDateTime parseDateTime(String dateTime) throws InvalidArgumentException {
            for (DateTimeFormatter format: DATE_TIME_FORMATS) {
                try {
                    return LocalDateTime.parse(dateTime, format);
                } catch (DateTimeParseException e) {
                    // Do nothing
                }
            }
            throw new InvalidArgumentException("☹ OOPS!!! Your dateTime format is not supported!");
        }
    }

    private List<Task> tasks;
    private final Storage storage;

    /**
     * Constructor for TaskList.
     */
    public TaskList() {
//        this.tasks = new ArrayList<>();
        this.storage = new Storage();
        try {
            this.tasks = storage.load();
        } catch (IOException e) {
            Ui.showErrorLoadingFromFileMessage();
            this.tasks = new ArrayList<>();
        }
    }

    /**
     * Adds a task to the list of tasks.
     * @param command The command that the user inputted.
     * @throws InvalidArgumentException If the task's format is invalid.
     */
    public void addTask(String command) throws InvalidArgumentException {
        String[] splitCommand = command.split("\\s", 2);

        if (splitCommand.length < 2) {
            throw new InvalidArgumentException("☹ OOPS!!! The description cannot be empty.");
        }
        String type = splitCommand[0];
        String taskDetails = splitCommand[1];
        TaskType taskType;
        try {
            taskType = TaskType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidArgumentException("Bro your task type is unknown: " + type);
        }
        tasks.add(taskType.createTask(taskDetails));
        try {
            storage.save(tasks);
        } catch (IOException e) {
            Ui.showErrorSavingToFileMessage();
        }
        Ui.showAddTaskMessage(tasks);
    }

    /**
     * Lists all the tasks.
     */
    public void listTasks() {
        Ui.showListTasksMessage(tasks);
    }

    /**
     * Marks a duke.task as done.
     * @param index The index of the task to be marked as done.
     * @throws IllegalTaskIndexException If the index is invalid.
     */
    public void markAsDone(int index) throws IllegalTaskIndexException {
        if (index > tasks.size() || index < 1) {
            throw new IllegalTaskIndexException();
        }
        Ui.showMarkAsDoneMessage(tasks, index);
        try {
            storage.save(tasks);
        } catch (IOException e) {
            Ui.showErrorSavingToFileMessage();
        }
    }

    /**
     * Marks a duke.task as undone.
     * @param index The index of the task to be marked as undone.
     * @throws IllegalTaskIndexException If the index is invalid.
     */
    public void markAsUndone(int index) throws IllegalTaskIndexException {
        if (index > tasks.size() || index < 1) {
            throw new IllegalTaskIndexException();
        }
        Ui.showMarkAsUndoneMessage(tasks, index);
        try {
            storage.save(tasks);
        } catch (IOException e) {
            Ui.showErrorSavingToFileMessage();
        }
    }

    /**
     * Delete a task from the list of tasks.
     * @param index The index of the task to be deleted.
     * @throws IllegalTaskIndexException If the index is invalid.
     */
    public void deleteTask(int index) throws IllegalTaskIndexException {
        if (index > tasks.size() || index < 1) {
            throw new IllegalTaskIndexException();
        }
        // Calls delete message from duke.ui.Ui class
        Ui.showDeleteTaskMessage(tasks, index);
        try {
            storage.save(tasks);
        } catch (IOException e) {
            Ui.showErrorSavingToFileMessage();
        }
    }

}