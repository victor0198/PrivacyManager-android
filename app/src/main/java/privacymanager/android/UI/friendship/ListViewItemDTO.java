package privacymanager.android.UI.friendship;

import privacymanager.android.R;

public class ListViewItemDTO {
    private boolean checked = false;
    private String itemText = "";
    private Integer iconId = R.drawable.friends_logo;
    private long friendshipId;
    private long friendId;
    private String symmetricKey;

    public boolean isChecked() {
        return checked;
    }
    public void setChecked(boolean checked) {
        this.checked = checked;
    }
    public String getItemText() {
        return itemText;
    }
    public void setItemText(String itemText) {
        this.itemText = itemText;
    }

    public long getFriendshipId() {
        return friendshipId;
    }

    public void setFriendshipId(long friendshipId) {
        this.friendshipId = friendshipId;
    }

    public long getFriendId() {
        return this.friendId;
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
}
