package opengarden.com.tinychat.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import opengarden.com.tinychat.database.TinyChatDatabase;

/**
 * Created by akshaymathur on 12/3/17.
 * Text message model class with GSON and DBFlow annotations
 */

@Table(database = TinyChatDatabase.class)
public class TextMessage extends BaseModel{

    @Column
    @SerializedName("msg")
    @Expose
    private String msg;
    @Column
    @PrimaryKey
    @SerializedName("client_time")
    @Expose
    private Long clientTime;
    @SerializedName("server_time")
    @Expose
    private Long serverTime;
    @Column
    private Boolean sent;

    public Boolean isSent() {
        return sent;
    }

    public void setSent(Boolean sent) {
        this.sent = sent;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Long getClientTime() {
        return clientTime;
    }

    public void setClientTime(Long clientTime) {
        this.clientTime = clientTime;
    }

    public Long getServerTime() {
        return serverTime;
    }

    public void setServerTime(Long serverTime) {
        this.serverTime = serverTime;
    }
}
