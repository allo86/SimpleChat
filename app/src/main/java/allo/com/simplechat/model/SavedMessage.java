package allo.com.simplechat.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Table;

/**
 * Created by ALLO on 3/8/16.
 */
@Table(name = "messages")
public class SavedMessage extends Model {

    private String body;

    private String userId;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public SavedMessage(){
        super();
    }
}
