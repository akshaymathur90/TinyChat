package opengarden.com.tinychat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

import opengarden.com.tinychat.R;
import opengarden.com.tinychat.models.TextMessage_Table;
import opengarden.com.tinychat.networking.SendMessageAsyncTask;
import opengarden.com.tinychat.models.TextMessage;


/**
 * Created by akshaymathur on 12/4/17.
 * Class for misc. helper functions required in the app.
 */

public class AppUtils {

    private static final String TAG = AppUtils.class.getSimpleName();

    /**
     * Method to get time in mills from shared preferences
     * @param context activity context
     * @return time in mills
     */
    public static long getLastMessageTime(Context context){
        SharedPreferences mSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return mSharedPref.getLong(context.getString(R.string.server_timestamp),0);
    }

    /**
     * Method to save time in mills to shared preferences
     * @param context activity context
     * @param timestamp time in mills
     */
    public static void setLastMessageTime(Context context, long timestamp){

        SharedPreferences mSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putLong(context.getString(R.string.server_timestamp), timestamp);
        editor.apply();
        Log.d(TAG, "Server time saved as: "+ timestamp);
    }

    /**
     * Method to check if internet is available
     * @param context activity context
     * @return true if internet is available, false otherwise.
     */

    public static boolean isInternetAvailable(Context context){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnected();
    }

    /**
     * Method to retrieve messages which could not be sent because the app was offline.
     * The method fetches the messages from database which have not been sent to the server by
     * checking if sent column value is false.
     * Once the message has been sent to the server the sent column value is set to true.
     * @param handler
     */

    public static void sendQueuedMessages(Handler handler){
        Log.d(TAG,"Checking for queued messages...");
        List<TextMessage> messages = SQLite.select()
                .from(TextMessage.class)
                .where(TextMessage_Table.sent.eq(false))
                .queryList();
        Log.d(TAG,"Sending " +messages.size() + " messages");
        if(messages.size()>0) {
            for(TextMessage msg : messages){
                Log.d(TAG,"Sending " +msg.getMsg() + " Client Time: "+ msg.getClientTime());
                SendMessageAsyncTask sendMessageAsyncTask = new SendMessageAsyncTask(handler);
                sendMessageAsyncTask.execute(msg.getMsg());
                msg.setSent(true);
                msg.save();
            }

        }
    }
}
