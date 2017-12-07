package opengarden.com.tinychat.networking;

import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import opengarden.com.tinychat.activities.TinyChatMainActivity;
import opengarden.com.tinychat.models.ErrorMessage;

/**
 * Created by akshaymathur on 12/2/17.
 */

public class TCPClient {

    private static final String            TAG             = "TCPClient"     ;
    private final Handler mHandler                          ;
    private              String            ipNumber, incomingMessage, command;
    BufferedReader in                                ;
    PrintWriter out                               ;
    private              boolean           mRun            = false           ;


    /**
     * TCPClient class constructor, which is created in AsyncTasks after the button click.
     * @param mHandler Handler passed as an argument for updating the UI with sent messages
     * @param command  Command passed as an argument, e.g. "{"msg":"hello","client_time":1446754551485}\n"
     * @param ipNumber String hosts ip address.
     */
    public TCPClient(Handler mHandler, String command, String ipNumber) {
        this.ipNumber         = ipNumber;
        this.command          = command ;
        this.mHandler         = mHandler;
    }

    /**
     * Public method for sending the message via OutputStream object.
     * @param message Message passed as an argument and sent via OutputStream object.
     */
    public void sendMessage(String message){
        if (out != null && !out.checkError()) {
            out.print(message);
            out.flush();
            mHandler.sendEmptyMessageDelayed(TinyChatMainActivity.SENT,2000);
            Log.d(TAG, "Sent Message: " + message);

        }
    }

    /**
     * Public method for stopping the TCPClient object ( and finalizing it after that ) from AsyncTask
     */
    public void stopClient(){
        Log.d(TAG, "Client stopped!");
        mRun = false;
    }
    public boolean isRunning(){
        return mRun;
    }
    public void run() {

        mRun = true;

        try {

            Log.d(TAG, "Connecting...");

            /**
             * Sending empty message with static int value from MainActivity
             * to update UI ( 'Connecting...' ).
             *
             */
            mHandler.sendEmptyMessageDelayed(TinyChatMainActivity.CONNECTING, 2000);
            Socket socket = new Socket(ipNumber, 1234);


            try {

                // Create PrintWriter object for sending messages to server.
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                //Create BufferedReader object for receiving messages from server.
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                Log.d(TAG, "In/Out created");
                mHandler.sendEmptyMessageDelayed(TinyChatMainActivity.SENDING,2000);
                //Sending message with command specified by AsyncTask
                this.sendMessage(command);


                incomingMessage = in.readLine();
                Log.d(TAG, "Received Message after sending: " +incomingMessage);
                if (incomingMessage!=null){
                    Gson gson = new Gson();
                    ErrorMessage errorMessage = gson.fromJson(incomingMessage,ErrorMessage.class);
                    Log.d(TAG, "Error: " +errorMessage.getError());
                    if (errorMessage.getError()!=null){
                        mHandler.sendEmptyMessageDelayed(TinyChatMainActivity.ERROR, 2000);
                    }
                }
                /*//Listen for the incoming messages while mRun = true
                while (mRun) {
                    incomingMessage = in.readLine();
                    Log.d(TAG, "Received Message: " +incomingMessage);
                    if (incomingMessage != null && listener != null) {

                        *//**
                         * Incoming message is passed to MessageCallback object.
                         * Next it is retrieved by AsyncTask and passed to onPublishProgress method.
                         *//*

                        listener.callbackMessageReceiver(incomingMessage);

                    }
                    incomingMessage = null;

                }

                Log.d(TAG, "Received Message: " +incomingMessage);*/

            } catch (Exception e) {

                Log.d(TAG, "Error", e);
                mHandler.sendEmptyMessageDelayed(TinyChatMainActivity.ERROR, 2000);

            } finally {

                out.flush();
                out.close();
                in.close();
                socket.close();
                //mHandler.sendEmptyMessageDelayed(TinyChatMainActivity.SENT,2000);
                Log.d(TAG, "Socket Closed");
            }

        } catch (Exception e) {

            Log.d(TAG, "Error", e);
            mHandler.sendEmptyMessageDelayed(TinyChatMainActivity.ERROR,2000);

        }

    }
/*
    public interface MessageCallback {
        *//**
         * Method overriden in AsyncTask 'doInBackground' method while creating the TCPClient object.
         * @param message Received message from server app.
         *//*
        public void callbackMessageReceiver(String message);
    }*/
}
