package opengarden.com.tinychat.utils;

import java.util.HashSet;

/**
 * Created by akshaymathur on 12/5/17.
 * Singleton class with a HashSet to identify whether the message was sent by this client or not.
 */

public class SentMessagesSingleton {

    private HashSet<Long> clientTimes;
    private static SentMessagesSingleton sSingleton = null;

    private SentMessagesSingleton(){
        clientTimes = new HashSet<>();
    }

    public static SentMessagesSingleton getInstance(){
        if(sSingleton == null){
            sSingleton = new SentMessagesSingleton();
        }
        return sSingleton;
    }

    public HashSet<Long> getClientTimes() {
        return clientTimes;
    }


}
