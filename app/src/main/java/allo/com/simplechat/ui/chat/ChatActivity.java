package allo.com.simplechat.ui.chat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import allo.com.simplechat.R;
import allo.com.simplechat.model.Message;

public class ChatActivity extends AppCompatActivity {

    static final String TAG = ChatActivity.class.getSimpleName();

    static final int MAX_CHAT_MESSAGES_TO_SHOW = 50;

    static final String SHARED_PREFERENCES_NAME = "shared_prefs";

    RecyclerView mRecyclerView;
    ChatAdapter mAdapter;
    LinearLayoutManager mLayoutManager;

    EditText etMessage;
    Button btSend;

    ArrayList<Message> mMessages;

    // Create a handler which can run code periodically
    static final int POLL_INTERVAL = 2000; // milliseconds
    Handler mHandler = new Handler();  // android.os.Handler
    Runnable mRefreshMessagesRunnable = new Runnable() {
        @Override
        public void run() {
            refreshMessages();
            mHandler.postDelayed(this, POLL_INTERVAL);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_messages);
        mRecyclerView.hasFixedSize();

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        etMessage = (EditText) findViewById(R.id.etMessage);
        btSend = (Button) findViewById(R.id.btSend);

        // User login
        if (ParseUser.getCurrentUser() != null) { // start with existing user
            startWithCurrentUser();
        } else { // If not logged in, login as a new anonymous user
            login();
        }

        if (isOnline()) {
            mHandler.postDelayed(mRefreshMessagesRunnable, POLL_INTERVAL);
        } else {
            refreshMessages();
        }
    }

    // Get the userId from the cached currentUser object
    void startWithCurrentUser() {
        setupMessagePosting();
    }

    // Create an anonymous user using ParseAnonymousUtils and set sUserId
    void login() {
        ParseAnonymousUtils.logIn(new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Anonymous login failed: ", e);
                } else {
                    startWithCurrentUser();
                }
            }
        });
    }

    void setupMessagePosting() {
        final String userId = ParseUser.getCurrentUser().getObjectId();

        List<Message> messages = new ArrayList<>();
        mAdapter = new ChatAdapter(messages, userId);
        mRecyclerView.setAdapter(mAdapter);

        btSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String data = etMessage.getText().toString();
                ParseObject message = ParseObject.create("Message");
                message.put(Message.USER_ID_KEY, ParseUser.getCurrentUser().getObjectId());
                message.put(Message.BODY_KEY, data);
                message.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Toast.makeText(ChatActivity.this, "Successfully created message on Parse",
                                Toast.LENGTH_SHORT).show();
                        // Get messages
                        refreshMessages();
                    }
                });
                etMessage.setText(null);
            }
        });
    }

    void refreshMessages() {
        if (!isOnline()) {
            updateMessages(getLastSavedMessages());

        } else {
            // Construct query to execute
            ParseQuery<Message> query = ParseQuery.getQuery(Message.class);
            // Configure limit and sort order
            query.setLimit(MAX_CHAT_MESSAGES_TO_SHOW);
            query.orderByDescending("createdAt");

            // Execute query to fetch all messages from Parse asynchronously
            // This is equivalent to a SELECT query with SQL
            query.findInBackground(new FindCallback<Message>() {
                public void done(List<Message> messages, ParseException e) {
                    if (e == null) {
                        saveLastMessages(messages);
                        updateMessages(messages);
                    } else {
                        Log.e("message", "Error Loading Messages" + e);
                    }
                }
            });
        }
    }

    private void updateMessages(List<Message> messages) {
        Collections.sort(messages, new Comparator<Message>() {
            @Override
            public int compare(Message p1, Message p2) {
                if (p1.getCreatedAt() != null && p2.getCreatedAt() != null) {
                    return p1.getCreatedAt().compareTo(p2.getCreatedAt());
                }
                return -1;
            }
        });

        mAdapter.notifyDataSetChanged(messages); // update adapter
        mLayoutManager.scrollToPosition(messages.size() - 1);
    }

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void saveLastMessages(List<Message> messages) {
        SharedPreferences save = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences.Editor ed = save.edit();
        ed.clear().apply();
        for (int i = 0; i < 10; i++) {
            ed.putString("body_" + i, messages.get(i).getBody());
            ed.putString("userid_" + i, messages.get(i).getUserId());
            ed.putLong("createdAt_" + i, messages.get(i).getCreatedAt().getTime());
            ed.apply();
        }
    }

    public List<Message> getLastSavedMessages() {
        List<Message> messages = new ArrayList<>();
        SharedPreferences save = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        for (int i = 0; i < 10; i++) {
            Message message = new Message();
            message.setBody(save.getString("body_" + i, ""));
            message.setUserId(save.getString("userid_" + i, ""));
            message.setCreatedAt(new Date(save.getLong("createdAt_" + i, 0)));
            messages.add(message);
        }
        return messages;
    }
}
