package taskmanager.model;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;

/** Модель задачи. Поля — JavaFX Property, чтобы TableView обновлялась автоматически. */
public class Task {
    private final StringProperty title = new SimpleStringProperty("");
    private final StringProperty description = new SimpleStringProperty("");
    private final ObjectProperty<Priority> priority = new SimpleObjectProperty<>(Priority.MEDIUM);
    private final ObjectProperty<LocalDate> dueDate = new SimpleObjectProperty<>(LocalDate.now());
    private final ObjectProperty<Status> status = new SimpleObjectProperty<>(Status.NEW);

    public Task(String title, String description, Priority priority, LocalDate dueDate, Status status) {
        this.title.set(title);
        this.description.set(description);
        this.priority.set(priority);
        this.dueDate.set(dueDate);
        this.status.set(status);
    }

    public String getTitle() { return title.get(); }
    public void setTitle(String v) { title.set(v); }
    public StringProperty titleProperty() { return title; }

    public String getDescription() { return description.get(); }
    public void setDescription(String v) { description.set(v); }
    public StringProperty descriptionProperty() { return description; }

    public Priority getPriority() { return priority.get(); }
    public void setPriority(Priority v) { priority.set(v); }
    public ObjectProperty<Priority> priorityProperty() { return priority; }

    public LocalDate getDueDate() { return dueDate.get(); }
    public void setDueDate(LocalDate v) { dueDate.set(v); }
    public ObjectProperty<LocalDate> dueDateProperty() { return dueDate; }

    public Status getStatus() { return status.get(); }
    public void setStatus(Status v) { status.set(v); }
    public ObjectProperty<Status> statusProperty() { return status; }

    /** Копирует значения из другой задачи (используется при редактировании). */
    public void copyFrom(Task o) {
        setTitle(o.getTitle());
        setDescription(o.getDescription());
        setPriority(o.getPriority());
        setDueDate(o.getDueDate());
        setStatus(o.getStatus());
    }

    /** Просрочена: срок прошёл, а задача не выполнена. */
    public boolean isOverdue() {
        return getStatus() != Status.DONE && getDueDate() != null && getDueDate().isBefore(LocalDate.now());
    }

    /** Нужен для extractor в ObservableList: список реагирует на изменение полей. */
    public Observable[] observables() {
        return new Observable[]{title, description, priority, dueDate, status};
    }
}
