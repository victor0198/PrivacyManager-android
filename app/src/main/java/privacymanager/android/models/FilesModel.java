package privacymanager.android.models;

public class FilesModel {
    private int filesID;
    private String filePath;
    private String filePassword;

    public FilesModel(int filesID, String filePath, String filePassword) {
        this.filesID = filesID;
        this.filePath = filePath;
        this.filePassword = filePassword;
    }

    @Override
    public String toString() {
        return "CustomerModel{" +
                "filesID=" + filesID +
                ", filePath='" + filePath + '\'' +
                ", filePassword='" + filePassword + '\'' +
                '}';
    }

    public int getFilesID() {
        return filesID;
    }

    public void setFilesID(int credentialId) {
        this.filesID = credentialId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePassword() {
        return filePassword;
    }

    public void setFilePassword(String filePassword) {
        this.filePassword = filePassword;
    }

}
