package com.example.blabber;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private List<Messages> messageList;
    private FirebaseAuth auth;

    public MessageAdapter(List<Messages> messageList) {
        this.messageList = messageList;
    }

    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.msg_single_layout, parent, false);

        return new MessageViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder messageViewHolder, int i) {
        auth = FirebaseAuth.getInstance();
        String curr_user_id = auth.getCurrentUser().getUid();

        Messages msg = messageList.get(i);

        String from_user = msg.getFrom();

        if(from_user.equals(curr_user_id)) {

            messageViewHolder.messageText.setBackgroundColor(Color.WHITE);
            messageViewHolder.messageText.setTextColor(Color.BLACK);

        } else {

            messageViewHolder.messageText.setBackgroundResource(R.drawable.message_text_background);
            messageViewHolder.messageText.setTextColor(Color.WHITE);

        }

        messageViewHolder.messageText.setText(msg.getMessage());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public CircleImageView profilePic;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.msg_text);
            profilePic = itemView.findViewById(R.id.msg_user);
        }
    }

}
