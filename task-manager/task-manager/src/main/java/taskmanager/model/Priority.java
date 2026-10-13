package taskmanager.model;

public enum Priority {
    LOW("Низкий"), MEDIUM("Средний"), HIGH("Высокий");

    private final String label;

    Priority(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
