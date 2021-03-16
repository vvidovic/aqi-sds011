package hr.vvidovic.aqisds011;

public enum AqiRequest {
    CODE_PERMISSION(1),
    CREATE_FILE(2);

    int request;

    public int val() {
        return request;
    }
    AqiRequest(int request) {
        this.request = request;
    }
}
