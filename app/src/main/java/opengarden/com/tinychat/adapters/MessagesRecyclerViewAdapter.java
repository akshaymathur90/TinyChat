package opengarden.com.tinychat.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import opengarden.com.tinychat.R;
import opengarden.com.tinychat.utils.SentMessagesSingleton;
import opengarden.com.tinychat.models.TextMessage;

/**
 * Created by akshaymathur on 12/3/17.
 * Recycler view adapter to display the messages in recycler view.
 */

public class MessagesRecyclerViewAdapter extends RecyclerView.Adapter<MessagesRecyclerViewAdapter.MessageViewHolder> {

    private static final String TAG = "MessageAdapter";
    private ArrayList<TextMessage> mMessages;
    private Context mContext;
    public MessagesRecyclerViewAdapter(Context context, ArrayList<TextMessage> messages){
        mMessages = messages;
        mContext = context;
    }

    public void addMessage(TextMessage message){
        mMessages.add(message);
        notifyItemInserted(mMessages.size());
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.message_row_layout,parent,false);

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {
        TextMessage message = mMessages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView tvMessageText;
        public MessageViewHolder(View view){
            super(view);
            tvMessageText = (TextView) view.findViewById(R.id.tv_messageText);
        }

        void bind(TextMessage message){
            Log.d(TAG,"The msg client time is: " +message.getClientTime());
            Log.d(TAG,"Singleton Size: " + SentMessagesSingleton.getInstance().getClientTimes().toString());
            Date serverDate = new Date(message.getServerTime());
            DateFormat formatDate =  SimpleDateFormat.getDateInstance();
            DateFormat formatTime =  SimpleDateFormat.getTimeInstance();

            Spanned chatBody = fromHtml( "<b>" +
                    message.getMsg() +"</b>  <br/> <br/> <i>"+
                    formatDate.format(serverDate) + " " + formatTime.format(serverDate) + "</i>") ;
            /*
               Checking if the messages originated from this client.
               If yes, the chat bubble and text appears on the right side.
             */
            if(SentMessagesSingleton.getInstance().getClientTimes().contains(message.getClientTime())){
                tvMessageText.setBackground(ContextCompat
                        .getDrawable(mContext,R.drawable.ic_chat_bubble_outline_black_48dp_right));
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)tvMessageText.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                tvMessageText.setLayoutParams(params);
            }
            tvMessageText.setText(chatBody);
        }

    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

}
