package allo.com.simplechat;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.interceptors.ParseLogInterceptor;

import allo.com.simplechat.model.Message;

/**
 * Created by ALLO on 3/8/16.
 */
public class ChatApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Parse
        ParseObject.registerSubclass(Message.class);

        // Initialize Parse
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("93bd0b59a90d46b1999e484431b83f41") // should correspond to APP_ID env variable
                .addNetworkInterceptor(new ParseLogInterceptor())
                .server("https://simplechatclient.herokuapp.com/parse/").build());
    }
}
