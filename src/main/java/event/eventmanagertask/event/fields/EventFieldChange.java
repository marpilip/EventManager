package event.eventmanagertask.event.fields;

public class EventFieldChange<T> {
    private T oldField;
    private T newField;

    public EventFieldChange() {
    }

    public EventFieldChange(T oldField, T newField) {
        this.oldField = oldField;
        this.newField = newField;
    }

    public T getOldField() {
        return oldField;
    }

    public void setOldField(T oldField) {
        this.oldField = oldField;
    }

    public T getNewField() {
        return newField;
    }

    public void setNewField(T newField) {
        this.newField = newField;
    }

    public boolean hasChanged() {
        return oldField != null && newField != null && !oldField.equals(newField);
    }
}