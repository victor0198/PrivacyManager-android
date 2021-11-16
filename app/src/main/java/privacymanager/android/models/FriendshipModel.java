package privacymanager.android.models;

public class FriendshipModel {
    private long friendshipId;
    private long friendId;
    private String symmetricKey;

    public FriendshipModel(long friendshipId, long friendId, String symmetricKey) {
        this.friendshipId = friendshipId;
        this.friendId = friendId;
        this.symmetricKey = symmetricKey;
    }

    public long getFriendshipId() {
        return friendshipId;
    }

    public void setFriendshipId(long friendshipId) {
        this.friendshipId = friendshipId;
    }

    public long getFriendId() {
        return friendId;
    }

    public void setFriendId(long friendId) {
        this.friendId = friendId;
    }

    public String getSymmetricKey() {
        return symmetricKey;
    }

    public void setSymmetricKey(String symmetricKey) {
        this.symmetricKey = symmetricKey;
    }

    @Override
    public String toString() {
        return "FriendshipModel{" +
                "friendshipId=" + friendshipId +
                ", friendId=" + friendId +
                ", symmetricKey='" + symmetricKey + '\'' +
                '}';
    }
}
