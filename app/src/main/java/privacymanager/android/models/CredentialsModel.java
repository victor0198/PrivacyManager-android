package privacymanager.android.models;

public class CredentialsModel {
    private int credentialsId;
    private String service;
    private String login;
    private String password;
    private int uploaded;

    public CredentialsModel(int credentialId, String service, String login, String password, int uploaded) {
        this.credentialsId = credentialId;
        this.service = service;
        this.login = login;
        this.password = password;
        this.uploaded = uploaded;
    }

    @Override
    public String toString() {
        return "CustomerModel{" +
                "credentialId=" + credentialsId +
                ", service='" + service + '\'' +
                ", login='" + login + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public int getCredentialId() {
        return credentialsId;
    }

    public void setCredentialId(int credentialId) {
        this.credentialsId = credentialId;
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

    public int getUploaded() {
        return uploaded;
    }

    public void setUploaded(int uploaded) {
        this.uploaded = uploaded;
    }
}