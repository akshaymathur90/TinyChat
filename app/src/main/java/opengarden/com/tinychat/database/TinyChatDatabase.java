package opengarden.com.tinychat.database;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Created by akshaymathur on 12/5/17.
 * DB Flow database class which initializes the database.
 */
@Database(name = TinyChatDatabase.NAME, version = TinyChatDatabase.VERSION)
public class TinyChatDatabase {
    public static final String NAME = "TinyChatDatabase";

    public static final int VERSION = 2;
}
