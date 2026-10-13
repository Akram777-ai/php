package taskmanager.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import taskmanager.model.Priority;
import taskmanager.model.Status;
import taskmanager.model.Task;

import java.time.LocalDate;

/** Контроллер окна-формы добавления/редактирования задачи. */
public class TaskFormController {
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private ComboBox<Priority> priorityBox;
    @FXML private DatePicker dueDatePicker;
    @FXML private ComboBox<Status> statusBox;

    private Task result;      // null, если пользователь нажал «Отмена»
    private boolean editing;  // true — редактирование существующей задачи

    @FXML
    private void initialize() {
        priorityBox.getItems().setAll(Priority.values());
        priorityBox.setValue(Priority.MEDIUM);
        statusBox.getItems().setAll(Status.values());
        statusBox.setValue(Status.NEW);
        dueDatePicker.setValue(LocalDate.now().plusDays(7));
    }

    /** Заполняет форму данными редактируемой задачи. */
    public void setTask(Task t) {
        if (t == null) {
            return;
        }
        editing = true;
        titleField.setText(t.getTitle());
        descriptionArea.setText(t.getDescription());
        priorityBox.setValue(t.getPriority());
        dueDatePicker.setValue(t.getDueDate());
        statusBox.setValue(t.getStatus());
    }

    public Task getResult() {
        return result;
    }

    @FXML
    private void onSave() {
        String title = titleField.getText() == null ? "" : titleField.getText().trim();
        LocalDate due = dueDatePicker.getValue();
        Status status = statusBox.getValue();

        // --- валидация ввода ---
        String error = null;
        if (title.isEmpty()) {
            error = "Введите название задачи.";
        } else if (title.length() > 100) {
            error = "Название не должно быть длиннее 100 символов.";
        } else if (due == null) {
            error = "Укажите корректный срок выполнения.";
        } else if (!editing && status != Status.DONE && due.isBefore(LocalDate.now())) {
            error = "Срок новой задачи не может быть в прошлом.";
        }
        if (error != null) {
            Alert a = new Alert(Alert.AlertType.WARNING, error);
            a.setTitle("Ошибка ввода");
            a.setHeaderText("Проверьте введённые данные");
            a.initOwner(titleField.getScene().getWindow());
            a.showAndWait();
            return;
        }

        String desc = descriptionArea.getText() == null ? "" : descriptionArea.getText().trim();
        result = new Task(title, desc, priorityBox.getValue(), due, status);
        close();
    }

    @FXML
    private void onCancel() {
        result = null;
        close();
    }

    private void close() {
        ((Stage) titleField.getScene().getWindow()).close();
    }
}
