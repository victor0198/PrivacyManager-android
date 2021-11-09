package privacymanager.android.models;

public class NotificationsModel {
    private long createdRequestId;
    private long senderId;
    private long receiverId;
    private String publicKey;
    private String status;

    public void setCreatedRequestId(long createdRequestId) {
        this.createdRequestId = createdRequestId;
    }

    public void setSenderId(long senderId) {
        this.senderId = senderId;
    }

    public void setReceiverId(long receiverId) {
        this.receiverId = receiverId;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public long getCreatedRequestId() {
        return createdRequestId;
    }

    public long getSenderId() {
        return senderId;
    }

    public long getReceiverId() {
        return receiverId;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "NotificationsModel{" +
                "createdRequestId=" + createdRequestId +
                ", senderId=" + senderId +
                ", receiverId=" + receiverId +
                ", publicKey='" + publicKey + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
