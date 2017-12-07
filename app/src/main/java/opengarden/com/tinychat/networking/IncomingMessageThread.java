package opengarden.com.tinychat.networking;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by akshaymathur on 12/3/17.
 * This class is responsible for connecting to the server and listening for incoming messages
 */

public class IncomingMessageThread extends Thread {
    private static final String TAG = IncomingMessageThread.class.getSimpleName();
    private static final String COMMAND = "{\"command\":\"history\",\"client_time\":%d,\"since\":%d}\n";
    String address,incomingMessage;
    int port;
    BufferedReader in;
    PrintWriter out;
    private boolean mRun = false;
    IncomingMessageCallback listener;
    private long mSince;

    public IncomingMessageThread(String address, int port,long since, IncomingMessageCallback listener) {
        this.address = address;
        this.port = port;
        this.listener = listener;
        mSince = since;
    }

    public void stopThread(){
        mRun = false;
    }

    public boolean isRunning(){
        return mRun;
    }

    /**
     * This methods sends the command to request history from server
     * @param message the json string sent to server
     */
    private void retrieveHistory(String message){
        if (out != null && !out.checkError()) {
            out.print(message);
            out.flush();
            Log.d(TAG, "Sent Message: " + message);

        }
    }
    @Override
    public void run() {
        mRun = true;

        try {
            Log.d(TAG, "Connecting...");
            /**
             * Here the socket is created with hardcoded port.
             * Also the port is given in IpGetter class.
             *
             */
            Socket socket = new Socket(address, 1234);

            try {

                // Create PrintWriter object for sending messages to server.
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                //Create BufferedReader object for receiving messages from server.
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Log.d(TAG, "In/Out created");
                String messageJson = String.format(Locale.getDefault(),COMMAND,
                        Calendar.getInstance().getTimeInMillis(),mSince+1);
                if(mSince>0){
                    this.retrieveHistory(messageJson);
                }

                //Listen for the incoming messages while mRun = true
                while (mRun) {
                    incomingMessage = in.readLine();
                    Log.d(TAG, "Received Message: " +incomingMessage);
                    if (incomingMessage != null && listener != null && mRun) {

                        /**
                         * Incoming message is passed to MessageCallback object.
                         * Next it is retrieved by AsyncTask and passed to onPublishProgress method.
                         */
                        String[] messages = incomingMessage.split("\n");

                        listener.callbackMessageReceiver(messages);

                    }
                    incomingMessage = null;

                }

                Log.d(TAG, "Received Message: " +incomingMessage);

            } catch (Exception e) {

                Log.d(TAG, "Error", e);

            } finally {

                out.flush();
                out.close();
                in.close();
                socket.close();
                Log.d(TAG, "Socket Closed");
            }

        } catch (Exception e) {

            Log.d(TAG, "Error", e);

        }
    }
    public interface IncomingMessageCallback {
        /**
         * Method overriden in AsyncTask 'doInBackground' method while creating the TCPClient object.
         * @param messages Received message from server app.
         */
        public void callbackMessageReceiver(String[] messages);
    }
}
