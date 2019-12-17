package kr.pe.firstfloor.network.data;

public class LoginRequest extends Data {
    private String userId;
    private String password;

    protected LoginRequest() {}
    protected LoginRequest(String id, String password) {
        this.userId = id;
        this.password = password;
    }

    public String getUserId() { return userId; }
    public String getPassword() { return password; }

    public void setUserId(String userId) { this.userId = userId; }
    public void setPassword(String password) { this.password = password; }
}
