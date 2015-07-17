package if1001.cin.ufpe.br.chat1001.ceboso.gui.message;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import if1001.cin.ufpe.br.chat1001.R;


public class MessageAdapter extends BaseAdapter {

    private Activity mContext;
    private List<Message> mMessageList;

    public MessageAdapter(Activity context, List<Message> messageList) {
        mContext = context;
        mMessageList = messageList;
    }

    @Override
    public int getCount() {
        return mMessageList.size();
    }

    @Override
    public Object getItem(int position) {
        return mMessageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        MessageViewHolder viewHolder;

        if(convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = layoutInflater.inflate(R.layout.row_message, null);
            viewHolder = new MessageViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (MessageViewHolder) view.getTag();
        }

        viewHolder.mTxtMessage.setText(mMessageList.get(position).getMessage());

        if(mMessageList.get(position).getTypeMessage().equals(Message.TypeMessage.SENT_MESSAGE)) {
        	viewHolder.mTxtMessage.setBackgroundResource(R.mipmap.bubble_green);
        	viewHolder.mTxtMessage.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
        	viewHolder.wrapper.setGravity(Gravity.RIGHT);
        	viewHolder.wrapper.setPadding(25, 0, 0, 0);
        } else {
        	viewHolder.mTxtMessage.setBackgroundResource(R.mipmap.bubble_yellow);
        	viewHolder.mTxtMessage.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        	viewHolder.wrapper.setGravity(Gravity.LEFT);
        	viewHolder.wrapper.setPadding(0, 0, 25, 0);
        }

        viewHolder.mTxtMessage.getBackground().setAlpha(50);

        return view;
    }

    class MessageViewHolder {

        public TextView mTxtMessage;
        private LinearLayout wrapper;

        public MessageViewHolder(View base) {
            mTxtMessage = (TextView) base.findViewById(R.id.txtMessage);
            wrapper = (LinearLayout) base.findViewById(R.id.wrapper);
        }
    }

}
