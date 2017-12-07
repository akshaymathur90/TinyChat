package opengarden.com.tinychat.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import opengarden.com.tinychat.utils.AppUtils;

/**
 * Created by akshaymathur
 * Broadcast Receiver to notify the activities if there has been a change in internet connectivity.
 */

public class InternetCheckReceiver extends BroadcastReceiver{

    private final String TAG = "InternetCheckReceiver";
    private boolean internet = true;
    public InternetCheckReceiver(){}
    private ConnectivityChangeListener mListener;
    public interface ConnectivityChangeListener{
        void onNetworkChange(boolean isConnected);
    }

    public void setListener(ConnectivityChangeListener listener) {
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"caught the change");

        boolean isConnected = AppUtils.isInternetAvailable(context);
        mListener.onNetworkChange(isConnected);

    }

}
