package shamu.company.helpers.auth0;

public enum Auth0ConnectionEnum {
    SIMPLYHIRED("hrs-data-store"),
    INDEED("Indeed");

    private final String value;

    Auth0ConnectionEnum(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
