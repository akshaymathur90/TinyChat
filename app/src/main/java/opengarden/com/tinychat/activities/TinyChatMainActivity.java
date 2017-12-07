package opengarden.com.tinychat.activities;

import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Calendar;

import opengarden.com.tinychat.receivers.InternetCheckReceiver;
import opengarden.com.tinychat.R;
import opengarden.com.tinychat.adapters.MessagesRecyclerViewAdapter;
import opengarden.com.tinychat.models.TextMessage;
import opengarden.com.tinychat.networking.IncomingMessageThread;
import opengarden.com.tinychat.networking.SendMessageAsyncTask;
import opengarden.com.tinychat.utils.AppUtils;

/**
 * The activity which displays the main UI for the chat screen.
 */
public class TinyChatMainActivity extends AppCompatActivity {
    //static final integers for handler's msg.what
    public static final int ERROR = 1;
    public static final int SENDING = 2;
    public static final int CONNECTING = 3;
    public static final int SENT = 4;
    public static final int CLEAR = 5;
    //other final strings.
    public static final String IPADDRESS = "52.91.109.76";
    private static final String TAG = TinyChatMainActivity.class.getSimpleName();
    //UI widgets
    private Button b_send;
    private EditText et_message;
    private RecyclerView mRecyclerView;
    private ArrayList<TextMessage> mTextMessages;
    private RelativeLayout mRelativeLayout;
    private static TextView tv_msgStatus;
    private LinearLayoutManager mLinearLayoutManager;
    private MessagesRecyclerViewAdapter mAdapter;
    //other objects used in the activity
    private Gson gson;
    public InternetCheckReceiver mBroadcastReceiver;
    private Handler mHandler;
    private IncomingMessageThread mIncomingMessageThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tiny_chat_main);
        setupViews();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
        initBroadCastReciever();
    }

    private void initBroadCastReciever() {
        mBroadcastReceiver = new InternetCheckReceiver();
        mBroadcastReceiver.setListener(new InternetCheckReceiver.ConnectivityChangeListener() {
            @Override
            public void onNetworkChange(boolean isConnected) {
                Log.d(TAG, "is Connected: " + isConnected);
                if (isConnected) {
                    Log.d(TAG, "App is back Online...");
                    tv_msgStatus.setText(R.string.msg_online);
                    startMonitoring();
                    AppUtils.sendQueuedMessages(getmHandler(TinyChatMainActivity.this));
                    Snackbar.make(mRelativeLayout, getString(R.string.internet_connected), Snackbar.LENGTH_SHORT).show();

                } else {
                    tv_msgStatus.setText(R.string.msg_offline);
                    Snackbar.make(mRelativeLayout, getString(R.string.lost_internet), Snackbar.LENGTH_SHORT).show();
                    Log.d(TAG, "App is now offline");
                }
            }
        });
    }

    private void setupViews() {
        b_send = (Button) findViewById(R.id.b_send);
        et_message = (EditText) findViewById(R.id.et_message);
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_messageList);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.rl_containerView);
        tv_msgStatus = (TextView) findViewById(R.id.tv_msgStatus);
        tv_msgStatus.setText(R.string.msg_clear);

        mTextMessages = new ArrayList<>();
        mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mAdapter = new MessagesRecyclerViewAdapter(this, mTextMessages);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        b_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSendButtonClick();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(mBroadcastReceiver, intentFilter);
        if (AppUtils.isInternetAvailable(this)) {
            startMonitoring();
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "Stopping listening to incoming messages");
        unregisterReceiver(mBroadcastReceiver);
        stopMonitoring();
        super.onPause();
    }

    private void handleSendButtonClick() {
        Log.d("test", "<><> Button Clicked with message: " + et_message.getText().toString());
        if (!TextUtils.isEmpty(et_message.getText())) {
            if (AppUtils.isInternetAvailable(TinyChatMainActivity.this)) {
                SendMessageAsyncTask messageAsyncTask = new SendMessageAsyncTask(getmHandler(TinyChatMainActivity.this));
                messageAsyncTask.execute(et_message.getText().toString());
            } else {
                TextMessage saveMessage = new TextMessage();
                saveMessage.setMsg(et_message.getText().toString());
                saveMessage.setClientTime(Calendar.getInstance().getTimeInMillis());
                saveMessage.setSent(false);
                saveMessage.save();
                Snackbar.make(mRelativeLayout, getString(R.string.msg_queued),
                        Snackbar.LENGTH_LONG).show();
                Log.d(TAG, "Internet Unavailable Saving msg: " + saveMessage.getMsg());
            }
            et_message.setText("");
        }
    }

    /**
     * Method which starts IncomingMessageThread to listen for incoming messages.
     */
    private void startMonitoring() {
        stopMonitoring();

        mIncomingMessageThread = new IncomingMessageThread(IPADDRESS, 1234, AppUtils.getLastMessageTime(this), new IncomingMessageThread.IncomingMessageCallback() {
            @Override
            public void callbackMessageReceiver(String[] messages) {
                for (int i = 0; i < messages.length; i++) {
                    Log.d(TAG, "Received from server " + messages[i]);


                    final TextMessage textMessage = gson.fromJson(messages[i].trim(), TextMessage.class);
                    if (textMessage.getMsg() != null && textMessage.getServerTime() != null && textMessage.getClientTime() != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.addMessage(textMessage);
                                mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
                                Log.d(TAG, "Parsed msg: " + textMessage.getMsg());
                                Log.d(TAG, "Parsed client time: " + textMessage.getClientTime());
                                Log.d(TAG, "Parsed server time: " + textMessage.getServerTime());
                                AppUtils.setLastMessageTime(TinyChatMainActivity.this, textMessage.getServerTime());
                            }
                        });
                    }
                }
            }
        });
        mIncomingMessageThread.start();
    }

    /**
     * Method which stops IncomingMessageThread to stop listening for incoming messages.
     */

    private void stopMonitoring() {
        if (mIncomingMessageThread != null) {
            mIncomingMessageThread.stopThread();
            mIncomingMessageThread = null;
        }
    }


    //Method for passing Handler object in AsyncTask's constructor with final Context
    private Handler getmHandler(final Context context) {
        mHandler = new UIHandler(TinyChatMainActivity.this);
        return mHandler;
    }

    private static class UIHandler extends Handler {
        String mTag = "Handler";
        Context mContext;

        public UIHandler(Context context) {
            mContext = context;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ERROR:
                    Log.d(mTag, "In Handler's error");
                    tv_msgStatus.setText(R.string.msg_error);
                    break;
                case SENDING:
                    Log.d(mTag, "In Handler's sending");
                    tv_msgStatus.setText(R.string.msg_sending);
                    break;
                case CONNECTING:
                    Log.d(mTag, "In Handler's connecting");
                    tv_msgStatus.setText(R.string.msg_connecting);
                    break;
                case SENT:
                    Log.d(mTag, "In Handler's sent");
                    tv_msgStatus.setText(R.string.msg_sent);
                    break;
                case CLEAR:
                    Log.d(mTag, "In Handler's clear");
                    if (AppUtils.isInternetAvailable(mContext))
                        tv_msgStatus.setText(R.string.msg_online);
                    else
                        tv_msgStatus.setText(R.string.msg_offline);
                    break;
            }
        }
    }

}
