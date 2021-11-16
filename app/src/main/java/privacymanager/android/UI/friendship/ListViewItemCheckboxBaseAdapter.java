package privacymanager.android.UI.friendship;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import privacymanager.android.R;

public class ListViewItemCheckboxBaseAdapter extends BaseAdapter {
    private List<ListViewItemDTO> listViewItemDtoList = null;
    private Context ctx = null;
    public ListViewItemCheckboxBaseAdapter(Context ctx, List<ListViewItemDTO> listViewItemDtoList) {
        this.ctx = ctx;
        this.listViewItemDtoList = listViewItemDtoList;
    }

    public void changeItems(List<ListViewItemDTO> listViewItemDtoList) {
        this.listViewItemDtoList = listViewItemDtoList;
    }

    @Override
    public int getCount() {
        int ret = 0;
        if(listViewItemDtoList!=null)
        {
            ret = listViewItemDtoList.size();
        }
        return ret;
    }
    @Override
    public Object getItem(int itemIndex) {
        Object ret = null;
        if(listViewItemDtoList!=null) {
            ret = listViewItemDtoList.get(itemIndex);
        }
        return ret;
    }
    @Override
    public long getItemId(int itemIndex) {
        return itemIndex;
    }
    @Override
    public View getView(int itemIndex, View convertView, ViewGroup viewGroup) {
        ListViewItemViewHolder viewHolder = null;
        if(convertView!=null)
        {
            viewHolder = (ListViewItemViewHolder) convertView.getTag();
        }else
        {
            convertView = View.inflate(ctx, R.layout.row_friends, null);
            CheckBox listItemCheckbox = (CheckBox) convertView.findViewById(R.id.checkBox);
            listItemCheckbox.setOnClickListener(view -> {
                listViewItemDtoList.get(itemIndex).setChecked(!listViewItemDtoList.get(itemIndex).isChecked());
            });
            TextView listItemText = (TextView) convertView.findViewById(R.id.friendUsernameText);
            viewHolder = new ListViewItemViewHolder(convertView);
            viewHolder.setItemCheckbox(listItemCheckbox);
            viewHolder.setItemTextView(listItemText);
            viewHolder.setIcon(convertView.findViewById(R.id.FriendIcon));
            convertView.setTag(viewHolder);
        }
        ListViewItemDTO listViewItemDto = listViewItemDtoList.get(itemIndex);
        viewHolder.getItemCheckbox().setChecked(listViewItemDto.isChecked());
        viewHolder.getItemTextView().setText(listViewItemDto.getItemText());
        viewHolder.getItemIcon().setImageResource(R.drawable.friends_logo);
        return convertView;
    }
}
