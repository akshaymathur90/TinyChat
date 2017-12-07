package opengarden.com.tinychat.networking;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.util.Calendar;
import java.util.Locale;

import opengarden.com.tinychat.utils.SentMessagesSingleton;
import opengarden.com.tinychat.activities.TinyChatMainActivity;

/**
 * Created by akshaymathur on 12/2/17.
 */

public class SendMessageAsyncTask extends AsyncTask<String, String, TCPClient> {
    private static final String COMMAND = "{\"msg\":\"%s\",\"client_time\":%d}\n";
    private TCPClient  tcpClient;
    private Handler mHandler;
    private static final String TAG = "SendMessageAsyncTask";

    /**
     * SendMessageAsyncTask constructor with handler passed as argument. The UI is updated via handler.
     * In doInBackground(...) method, the handler is passed to TCPClient object.
     * @param mHandler Handler object that is retrieved from MainActivity class and passed to TCPClient
     *                 class for sending messages and updating UI.
     */
    public SendMessageAsyncTask(Handler mHandler){
        this.mHandler = mHandler;
    }

    /**
     * Overriden method from AsyncTask class. There the TCPClient object is created.
     * @param params From MainActivity class empty string is passed.
     * @return TCPClient object for closing it in onPostExecute method.
     */
    @Override
    protected TCPClient doInBackground(String... params) {
        Log.d(TAG, "In do in background");
        long msgClientTime = Calendar.getInstance().getTimeInMillis();
        SentMessagesSingleton.getInstance().getClientTimes().add(msgClientTime);
        String messageJson = String.format(Locale.getDefault(),COMMAND,params[0], msgClientTime);
        try{
            tcpClient = new TCPClient(mHandler,
                    messageJson,
                    TinyChatMainActivity.IPADDRESS);

        }catch (NullPointerException e){
            Log.d(TAG, "Caught null pointer exception");
            e.printStackTrace();
        }
        tcpClient.run();
        return null;
    }
    /*@Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        tcpClient.stopClient();
        //mHandler.sendEmptyMessageDelayed(TinyChatMainActivity.SHUTDOWN, 2000);
    }*/

    @Override
    protected void onPostExecute(TCPClient result){
        super.onPostExecute(result);
        Log.d(TAG, "In on post execute");
        if(result != null && result.isRunning()){
            result.stopClient();
        }
        mHandler.sendEmptyMessageDelayed(TinyChatMainActivity.CLEAR, 7000);

    }
}

