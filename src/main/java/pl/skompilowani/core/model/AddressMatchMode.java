package pl.skompilowani.core.model;

public enum AddressMatchMode {
    FROM("Od"),
    TO("Do"),
    FROM_OR_TO("Od/Do");

    private final String label;

    AddressMatchMode(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
