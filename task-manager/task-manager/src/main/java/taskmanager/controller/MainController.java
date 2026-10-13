package taskmanager.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import taskmanager.model.Priority;
import taskmanager.model.Status;
import taskmanager.model.Task;
import taskmanager.storage.TaskStorage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Контроллер главного окна: таблица, поиск, фильтры, меню, добавление/редактирование/удаление. */
public class MainController {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String ALL = "Все";

    @FXML private TableView<Task> table;
    @FXML private TableColumn<Task, String> colTitle;
    @FXML private TableColumn<Task, Priority> colPriority;
    @FXML private TableColumn<Task, LocalDate> colDue;
    @FXML private TableColumn<Task, Status> colStatus;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> priorityFilter;
    @FXML private Label statusLabel;

    // extractor: список "замечает" изменение полей задачи (нужно для фильтра и обновления строк)
    private final ObservableList<Task> tasks = FXCollections.observableArrayList(Task::observables);
    private FilteredList<Task> filtered;

    @FXML
    private void initialize() {
        // --- колонки таблицы ---
        colTitle.setCellValueFactory(c -> c.getValue().titleProperty());
        colPriority.setCellValueFactory(c -> c.getValue().priorityProperty());
        colDue.setCellValueFactory(c -> c.getValue().dueDateProperty());
        colStatus.setCellValueFactory(c -> c.getValue().statusProperty());
        colDue.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setText(empty || d == null ? null : d.format(FMT));
            }
        });

        // --- строки: подсветка просроченных, двойной клик = редактирование ---
        table.setRowFactory(tv -> {
            TableRow<Task> row = new TableRow<>() {
                @Override
                protected void updateItem(Task t, boolean empty) {
                    super.updateItem(t, empty);
                    getStyleClass().remove("overdue");
                    if (!empty && t != null && t.isOverdue()) {
                        getStyleClass().add("overdue");
                    }
                }
            };
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    onEdit();
                }
            });
            return row;
        });

        // --- фильтры ---
        statusFilter.getItems().add(ALL);
        for (Status s : Status.values()) {
            statusFilter.getItems().add(s.toString());
        }
        priorityFilter.getItems().add(ALL);
        for (Priority p : Priority.values()) {
            priorityFilter.getItems().add(p.toString());
        }
        statusFilter.setValue(ALL);
        priorityFilter.setValue(ALL);

        // tasks -> FilteredList (фильтр) -> SortedList (сортировка по клику на заголовок) -> таблица
        filtered = new FilteredList<>(tasks, t -> true);
        SortedList<Task> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Задач нет. Нажмите «Добавить» (Ctrl+N)"));
        table.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DELETE) {
                onDelete();
            }
        });

        // --- события ---
        searchField.textProperty().addListener((o, a, b) -> applyFilter());
        statusFilter.valueProperty().addListener((o, a, b) -> applyFilter());
        priorityFilter.valueProperty().addListener((o, a, b) -> applyFilter());
        table.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> updateStatus());
        tasks.addListener((ListChangeListener<Task>) c -> updateStatus());

        loadTasks();
        applyFilter();
    }

    // ===================== фильтрация и строка состояния =====================

    private void applyFilter() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String st = statusFilter.getValue();
        String pr = priorityFilter.getValue();
        filtered.setPredicate(t ->
                (q.isEmpty() || t.getTitle().toLowerCase().contains(q)
                        || t.getDescription().toLowerCase().contains(q))
                        && (st == null || ALL.equals(st) || t.getStatus().toString().equals(st))
                        && (pr == null || ALL.equals(pr) || t.getPriority().toString().equals(pr)));
        updateStatus();
    }

    private void updateStatus() {
        long overdue = tasks.stream().filter(Task::isOverdue).count();
        Task sel = table.getSelectionModel().getSelectedItem();
        statusLabel.setText("Показано: " + filtered.size() + " из " + tasks.size()
                + "   |   Просрочено: " + overdue
                + (sel != null ? "   |   Выбрана: " + sel.getTitle() : ""));
    }

    @FXML
    private void onReset() {
        searchField.clear();
        statusFilter.setValue(ALL);
        priorityFilter.setValue(ALL);
    }

    // ===================== добавление / редактирование / удаление =====================

    @FXML
    private void onAdd() {
        Task t = showForm(null);
        if (t != null) {
            tasks.add(t);
            table.getSelectionModel().select(t);
        }
    }

    @FXML
    private void onEdit() {
        Task sel = selectedOrWarn();
        if (sel == null) {
            return;
        }
        Task edited = showForm(sel);
        if (edited != null) {
            sel.copyFrom(edited);
        }
    }

    @FXML
    private void onDelete() {
        Task sel = selectedOrWarn();
        if (sel == null) {
            return;
        }
        Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                "Удалить задачу «" + sel.getTitle() + "»?", ButtonType.YES, ButtonType.NO);
        a.setTitle("Подтверждение удаления");
        a.setHeaderText(null);
        a.initOwner(table.getScene().getWindow());
        if (a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            tasks.remove(sel);
        }
    }

    @FXML
    private void onDone() {
        Task sel = selectedOrWarn();
        if (sel != null) {
            sel.setStatus(Status.DONE);
        }
    }

    private Task selectedOrWarn() {
        Task sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.INFORMATION, "Сначала выберите задачу в таблице.");
        }
        return sel;
    }

    /** Открывает второе окно (форма). task == null — добавление, иначе — редактирование. */
    private Task showForm(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/taskmanager/task-form.fxml"));
            Parent root = loader.load();
            TaskFormController form = loader.getController();
            form.setTask(task);

            Stage stage = new Stage();
            stage.initOwner(table.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setTitle(task == null ? "Новая задача" : "Редактирование задачи");
            stage.setResizable(false);
            stage.setScene(new Scene(root));
            stage.showAndWait();
            return form.getResult();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Не удалось открыть форму: " + e.getMessage());
            return null;
        }
    }

    // ===================== файл, справка =====================

    @FXML
    private void onSave() {
        try {
            TaskStorage.save(tasks);
            statusLabel.setText("Сохранено: " + TaskStorage.FILE.toAbsolutePath());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Не удалось сохранить файл: " + e.getMessage());
        }
    }

    public void saveSilently() {
        try {
            TaskStorage.save(tasks);
        } catch (IOException ignored) {
            // при закрытии окна ошибку не показываем
        }
    }

    @FXML
    private void onExit() {
        saveSilently();
        Platform.exit();
    }

    @FXML
    private void onAbout() {
        showAlert(Alert.AlertType.INFORMATION,
                "Task Manager\nМини-проект по дисциплине «Визуальное программирование»\nJava, JavaFX, FXML, Scene Builder");
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert a = new Alert(type, message);
        a.setHeaderText(null);
        a.setTitle(type == Alert.AlertType.ERROR ? "Ошибка" : "Информация");
        if (table.getScene() != null) {
            a.initOwner(table.getScene().getWindow());
        }
        a.showAndWait();
    }

    // ===================== данные =====================

    private void loadTasks() {
        try {
            tasks.setAll(TaskStorage.load());
        } catch (IOException | RuntimeException e) {
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR,
                    "Не удалось прочитать tasks.csv, загружены демо-данные.\n" + e.getMessage()));
        }
        if (tasks.isEmpty()) {
            tasks.setAll(demoTasks());
        }
    }

    private List<Task> demoTasks() {
        LocalDate n = LocalDate.now();
        return List.of(
                new Task("Подготовить отчёт по СРО", "Архитектура, скриншоты, тестирование", Priority.HIGH, n.plusDays(3), Status.IN_PROGRESS),
                new Task("Сделать презентацию", "8–10 слайдов", Priority.HIGH, n.plusDays(5), Status.NEW),
                new Task("Выложить проект на GitHub", "README и ссылка в отчёте", Priority.MEDIUM, n.plusDays(2), Status.NEW),
                new Task("Купить продукты", "Молоко, хлеб, яйца", Priority.LOW, n.plusDays(1), Status.NEW),
                new Task("Сдать лабораторную по NoSQL", "Скрипты и отчёт", Priority.HIGH, n.minusDays(2), Status.IN_PROGRESS),
                new Task("Записаться к стоматологу", "", Priority.MEDIUM, n.plusDays(10), Status.NEW),
                new Task("Повторить лекции по JavaFX", "Layout, события, таблицы", Priority.MEDIUM, n.plusDays(4), Status.IN_PROGRESS),
                new Task("Оплатить интернет", "", Priority.HIGH, n.minusDays(1), Status.NEW),
                new Task("Прочитать главу по базам данных", "", Priority.LOW, n.plusDays(7), Status.NEW),
                new Task("Настроить Scene Builder", "Подключить к IntelliJ IDEA", Priority.LOW, n.minusDays(5), Status.DONE),
                new Task("Создать репозиторий", "Первый коммит", Priority.MEDIUM, n.minusDays(4), Status.DONE),
                new Task("Подготовиться к защите", "Повторить код обработчиков", Priority.HIGH, n.plusDays(6), Status.NEW));
    }
}
