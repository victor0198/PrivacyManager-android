package privacymanager.android.models;

public class CredentialModel {
    private int credentialId;
    private String service;
    private String login;
    private String password;

    public CredentialModel(int credentialId, String service, String login, String password) {
        this.credentialId = credentialId;
        this.service = service;
        this.login = login;
        this.password = password;
    }

    @Override
    public String toString() {
        return "CustomerModel{" +
                "credentialId=" + credentialId +
                ", service='" + service + '\'' +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public int getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(int credentialId) {
        this.credentialId = credentialId;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}