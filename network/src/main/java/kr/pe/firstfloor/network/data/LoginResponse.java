package kr.pe.firstfloor.network.data;

public class LoginResponse extends Data {
    private boolean failed;
    private User user;
    private String failedReason;


    protected LoginResponse() {}


    private static String getString(LoginResponse loginResponse) {
        String result = Converter.toString(loginResponse);

        Data.remove(loginResponse);

        return result;
    }
    public static String getString(User user) {
        try {
            LoginResponse loginResponse = (LoginResponse) Data.newInstance(LoginResponse.class);
            loginResponse.setFailed(false);
            loginResponse.setUser(user);

            return getString(loginResponse);
        } catch (Exception exception) {
            exception.printStackTrace();

            return null;
        }
    }
    public static String getString(String failedReason) {
        try {
            LoginResponse loginResponse = (LoginResponse) Data.newInstance(LoginResponse.class);
            loginResponse.setFailed(true);
            loginResponse.setFailedReason(failedReason);

            return getString(loginResponse);
        } catch (Exception exception) {
            exception.printStackTrace();

            return null;
        }
    }


    public boolean isFailed() { return failed; }
    public User getUser() { return user; }
    public String getFailedReason() { return failedReason; }

    public void setFailed(boolean failed) { this.failed = failed; }
    public void setUser(User user) { this.user = user; }
    public void setFailedReason(String failedReason) { this.failedReason = failedReason; }
}
