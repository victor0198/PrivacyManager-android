package privacymanager.android.UI.friendship;

import privacymanager.android.R;

public class ListViewItemDTO {
    private boolean checked = false;
    private String itemText = "";
    private Integer iconId = R.drawable.friends_logo;
    private long friendshipId;
    private long friendId;
    private String friendName;
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

    public Integer getIconId() {
        return iconId;
    }

    public void setIconId(Integer iconId) {
        this.iconId = iconId;
    }

    public String getFriendName() {
        return friendName;
    }

    public void setFriendName(String friendName) {
        this.friendName = friendName;
    }

    public String getSymmetricKey() {
        return symmetricKey;
    }

    public void setSymmetricKey(String symmetricKey) {
        this.symmetricKey = symmetricKey;
    }

    @Override
    public String toString() {
        return "ListViewItemDTO{" +
                "checked=" + checked +
                ", itemText='" + itemText + '\'' +
                ", iconId=" + iconId +
                ", friendshipId=" + friendshipId +
                ", friendId=" + friendId +
                ", friendName='" + friendName + '\'' +
                ", symmetricKey='" + symmetricKey + '\'' +
                '}';
    }
}
