package privacymanager.android.models;

public class FilesModel {
    private String  fileName;
    private String filePassword;
    private String fileMD5;
    public FilesModel(String fileName, String fileMD5, String filePath, String filePassword) {
        this.fileName = fileName;
        this.fileMD5 = fileMD5;
        this.filePassword = filePassword;
    }

    @Override
    public String toString() {
        return "CustomerModel{" +
                "fileName=" + fileName +
                ", fileMD5='" + fileMD5 + '\'' +
                ", filePassword='" + filePassword + '\'' +
                '}';
    }

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
