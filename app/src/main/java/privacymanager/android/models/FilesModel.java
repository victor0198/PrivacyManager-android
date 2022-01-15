package privacymanager.android.models;

public class FilesModel {
    private int keyId;
    private String fileName;
    private String filePassword;
    private String fileMD5;

    public FilesModel() {
    }
    public FilesModel(int keyId, String fileName, String fileMD5, String filePassword) {
        this.keyId = keyId;
        this.fileName = fileName;
        this.fileMD5 = fileMD5;
        this.filePassword = filePassword;
    }

    @Override
    public String toString() {
        return "FilesModel{" +
                "keyId=" + keyId +
                ", fileName='" + fileName + '\'' +
                ", filePassword='" + filePassword + '\'' +
                ", fileMD5='" + fileMD5 + '\'' +
                '}';
    }

    public int getKeyId() { return keyId; }

    public void setKeyId(int keyId) { this.keyId = keyId; }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileMD5() {
        return fileMD5;
    }

    public void setFileMD5(String fileMD5) {
        this.fileMD5 = fileMD5;
    }

    public String getFilePassword() {
        return filePassword;
    }

    public void setFilePassword(String filePassword) {
        this.filePassword = filePassword;
    }

}
