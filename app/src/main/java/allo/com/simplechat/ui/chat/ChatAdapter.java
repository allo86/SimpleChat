package allo.com.simplechat.ui.chat;

import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;

import allo.com.simplechat.R;
import allo.com.simplechat.model.Message;

/**
 * Created by ALLO on 3/8/16.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    List<Message> messages;
    String userId;

    public ChatAdapter(List<Message> messages, String userId) {
        this.messages = messages;
        this.userId = userId;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        holder.configureViewWithMessage(this.messages.get(position));
    }

    @Override
    public int getItemCount() {
        return this.messages != null ? this.messages.size() : 0;
    }

    public void notifyDataSetChanged(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    class ChatViewHolder extends RecyclerView.ViewHolder {

        ImageView ivProfileOther;
        TextView tvBody;
        ImageView ivProfileMe;
        TextView tvDate;

        public ChatViewHolder(View itemView) {
            super(itemView);

            ivProfileOther = (ImageView) itemView.findViewById(R.id.ivProfileOther);
            tvBody = (TextView) itemView.findViewById(R.id.tvBody);
            ivProfileMe = (ImageView) itemView.findViewById(R.id.ivProfileMe);
            tvDate = (TextView) itemView.findViewById(R.id.tvDate);
        }

        public void configureViewWithMessage(Message message) {
            tvBody.setText(message.getBody());
            tvDate.setText(message.getCreatedAt() != null ? message.getCreatedAt().toString() : "");

            boolean isMe = message.getUserId() != null && message.getUserId().equals(userId);
            // Show-hide image based on the logged-in user.
            // Display the profile image to the right for our user, left for other users.
            if (isMe) {
                ivProfileMe.setVisibility(View.VISIBLE);
                ivProfileOther.setVisibility(View.GONE);
                tvBody.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
            } else {
                ivProfileOther.setVisibility(View.VISIBLE);
                ivProfileMe.setVisibility(View.GONE);
                tvBody.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
            }

            final ImageView profileView = isMe ? ivProfileMe : ivProfileOther;
            Picasso.with(profileView.getContext()).load(getProfileUrl(message.getUserId())).into(profileView);
        }

    }

    private static String getProfileUrl(final String userId) {
        String hex = "";
        try {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            final byte[] hash = digest.digest(userId.getBytes());
            final BigInteger bigInt = new BigInteger(hash);
            hex = bigInt.abs().toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "http://www.gravatar.com/avatar/" + hex + "?d=identicon";
    }
}
