package com.example.lab5;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.format.DateTimeFormatter;

public class HelloController {

    @FXML private TextField txtFullName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private ComboBox<String> cmbDepartment;
    @FXML private DatePicker dpBirthDate;

    @FXML private ToggleGroup positionGroup;
    @FXML private ToggleGroup degreeGroup;

    @FXML private CheckBox chkCurator;
    @FXML private CheckBox chkHeadOfDept;
    @FXML private CheckBox chkResearch;
    @FXML private CheckBox chkAgreement;

    @FXML private Button btnCreate;
    @FXML private Label lblResult;

    @FXML
    public void initialize() {
        cmbDepartment.setItems(FXCollections.observableArrayList(
                "ПОиВТ (Программное обеспечение)",
                "Информационные системы",
                "Высшая математика",
                "Физика и электроника",
                "Кибербезопасность"
        ));

        btnCreate.setDisable(true);

        txtFullName.textProperty().addListener((obs, oldVal, newVal) -> updateCreateButtonState());
        chkAgreement.selectedProperty().addListener((obs, oldVal, newVal) -> updateCreateButtonState());
    }

    private void updateCreateButtonState() {
        boolean isNameEmpty = txtFullName.getText().trim().isEmpty();
        boolean isAgreed = chkAgreement.isSelected();
        btnCreate.setDisable(isNameEmpty || !isAgreed);
    }

    @FXML
    private void onCreateClick() {
        String fullName = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();

        if (fullName.isBlank() || email.isBlank() || phone.isBlank()) {
            showError("Заполните все текстовые поля ввода.");
            return;
        }

        if (!isEmailValid(email)) {
            showError("Введите корректный email адрес.");
            txtEmail.requestFocus();
            return;
        }

        if (cmbDepartment.getValue() == null) {
            showError("Выберите кафедру из списка.");
            return;
        }

        if (dpBirthDate.getValue() == null) {
            showError("Укажите дату рождения.");
            return;
        }

        RadioButton selectedPosition = (RadioButton) positionGroup.getSelectedToggle();
        RadioButton selectedDegree = (RadioButton) degreeGroup.getSelectedToggle();

        if (selectedPosition == null || selectedDegree == null) {
            showError("Выберите должность и учёную степень.");
            return;
        }

        String birthDateStr = dpBirthDate.getValue().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        String activities = buildActivities();

        lblResult.setText(
                "КАРТОЧКА ПРЕПОДАВАТЕЛЯ:\n" +
                        "• ФИО: " + fullName + "\n" +
                        "• Email: " + email + " | Тел: " + phone + "\n" +
                        "• Кафедра: " + cmbDepartment.getValue() + "\n" +
                        "• Дата рождения: " + birthDateStr + "\n" +
                        "• Должность: " + selectedPosition.getText() + "\n" +
                        "• Учёная степень: " + selectedDegree.getText() + "\n" +
                        "• Обязанности: " + activities
        );
    }

    private String buildActivities() {
        StringBuilder result = new StringBuilder();
        if (chkCurator.isSelected()) result.append("Кураторство; ");
        if (chkHeadOfDept.isSelected()) result.append("Зав. кафедрой; ");
        if (chkResearch.isSelected()) result.append("НИР; ");

        if (result.length() == 0) return "Нет";
        return result.toString();
    }

    private boolean isEmailValid(String email) {
        int at = email.indexOf('@');
        int dot = email.lastIndexOf('.');
        return at > 0 && dot > at + 1 && dot < email.length() - 1;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка ввода");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onClearClick() {
        txtFullName.clear();
        txtEmail.clear();
        txtPhone.clear();
        cmbDepartment.setValue(null);
        dpBirthDate.setValue(null);

        positionGroup.selectToggle(null);
        degreeGroup.selectToggle(null);

        chkCurator.setSelected(false);
        chkHeadOfDept.setSelected(false);
        chkResearch.setSelected(false);
        chkAgreement.setSelected(false);

        lblResult.setText("Результат:");
        txtFullName.requestFocus();
    }

    @FXML
    private void onExitClick() {
        Platform.exit();
    }
}