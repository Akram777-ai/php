package taskmanager.model;

public enum Status {
    NEW("Новая"), IN_PROGRESS("В работе"), DONE("Выполнена");

    private final String label;

    Status(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
