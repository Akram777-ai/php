package taskmanager.storage;

import taskmanager.model.Priority;
import taskmanager.model.Status;
import taskmanager.model.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Локальное хранение задач в CSV (разделитель ";", кодировка UTF-8). */
public final class TaskStorage {
    public static final Path FILE = Paths.get("tasks.csv");

    private TaskStorage() {
    }

    public static void save(Collection<Task> tasks) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("title;description;priority;dueDate;status");
        for (Task t : tasks) {
            lines.add(String.join(";",
                    quote(t.getTitle()),
                    quote(t.getDescription().replace("\n", "\\n")),
                    t.getPriority().name(),
                    String.valueOf(t.getDueDate()),
                    t.getStatus().name()));
        }
        Files.write(FILE, lines, StandardCharsets.UTF_8);
    }

    public static List<Task> load() throws IOException {
        List<Task> result = new ArrayList<>();
        if (!Files.exists(FILE)) {
            return result;
        }
        List<String> lines = Files.readAllLines(FILE, StandardCharsets.UTF_8);
        for (int i = 1; i < lines.size(); i++) { // строка 0 — заголовок
            if (lines.get(i).isBlank()) {
                continue;
            }
            List<String> f = parseLine(lines.get(i));
            if (f.size() < 5) {
                continue;
            }
            try {
                result.add(new Task(f.get(0), f.get(1).replace("\\n", "\n"),
                        Priority.valueOf(f.get(2)), LocalDate.parse(f.get(3)), Status.valueOf(f.get(4))));
            } catch (RuntimeException e) {
                // повреждённая строка пропускается, программа не падает
            }
        }
        return result;
    }

    private static String quote(String s) {
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    private static List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        sb.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    sb.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ';') {
                fields.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields;
    }
}
