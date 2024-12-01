package de.computerstudienwerkstatt.tortuga.controller.base;

/**
 * @author Mischa Holz
 */
public class FilterCriteria {

    public enum Operation {
        EQUALS(":"),
        GREATER_THAN(">"),
        LESS_THAN("<");

        private String displayValue;

        Operation(String displayValue) {
            this.displayValue = displayValue;
        }
    }

    private String key;

    private Object value;

    private Operation operation;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }
}
