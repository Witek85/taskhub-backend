package pl.witold.taskhub.task;

public enum TaskStatus {
    NEW("New"),
    IN_PROGRESS("In Progress"),
    READY("Ready");

    private final String label;

    TaskStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}