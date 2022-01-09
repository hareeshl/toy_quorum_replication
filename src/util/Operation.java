package util;

public enum Operation {
    GET("GET"),
    PUT("PUT"),
    REGISTER("REGISTER"),
    PUBLISH("PUBLISH");

    private String operationName;

    Operation(String operationName) {
        this.operationName = operationName;
    }
}
