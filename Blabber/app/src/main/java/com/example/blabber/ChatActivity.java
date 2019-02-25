package com.example.blabber;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String chatUser;

    private DatabaseReference rootRef;
    private StorageReference imageStorage;

    private String chat_username;

    private TextView titleView;
    private TextView lastSeenView;
    private CircleImageView chat_img;

    private LinearLayout chatbox;
    private ImageButton chatAdd;
    private ImageButton chatSend;
    private EditText chatMsg;

    private FirebaseAuth auth;
    private String curr_user_id;

    private RecyclerView msgListView;
    private SwipeRefreshLayout refreshLayout;
    private final List<Messages> messagesList = new ArrayList<Messages>();
    private LinearLayoutManager llm;

    private MessageAdapter adapter;

    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    private static final int GALLERY_PICK = 1;
    private int currentPage = 1;

    private int itemPosition = 0;
    private String lastKey = "";
    private String prevKey = "";

    private boolean canChildScrollUp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatUser = getIntent().getStringExtra("user_id");
        chat_username = getIntent().getStringExtra("user_name");

        chatbox = findViewById(R.id.chat_bottom);

        titleView = findViewById(R.id.custombar_name);
        lastSeenView = findViewById(R.id.custombar_lastseen);
        chat_img = findViewById(R.id.custombar_img);

        chatMsg = findViewById(R.id.chat_chatbox);
        chatSend = findViewById(R.id.chat_sendmsg);

        adapter = new MessageAdapter(messagesList);

        msgListView = findViewById(R.id.chat_allmsg);
        refreshLayout = findViewById(R.id.swipeLayout);
        canChildScrollUp = true;

        llm = new LinearLayoutManager(this);

        msgListView.setHasFixedSize(true);
        msgListView.setLayoutManager(llm);

        msgListView.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        curr_user_id = auth.getCurrentUser().getUid();

        rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.child("Users").child(chatUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                chatMsg = chatbox.findViewById(R.id.chat_chatbox);
                chatSend = chatbox.findViewById(R.id.chat_sendmsg);

                String online = dataSnapshot.child("online").getValue().toString();
                String img = dataSnapshot.child("image").getValue().toString();
                String lastSeen = dataSnapshot.child("last_seen").getValue().toString();

                TimeAgo timeDiff = new TimeAgo();

                String formatLastSeen = timeDiff.getTimeAgo(Long.parseLong(lastSeen),
                        getApplicationContext());

                titleView.setText(chat_username);

                if(online.equals("true")) {
                    lastSeenView.setText("Online");
                } else {
                    lastSeenView.setText(formatLastSeen);
                }

                if(!img.equals("default")) {
                    Picasso.get()
                            .load(img)
                            .into(chat_img);
                } else {
                    Picasso.get()
                            .load(R.drawable.profile_trans)
                            .into(chat_img);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        rootRef.child("Chat").child(curr_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(chatUser)) {

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen", false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + curr_user_id + "/" + chatUser, chatAddMap);
                    chatUserMap.put("Chat/" + chatUser + "/" + curr_user_id, chatAddMap);

                    rootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError != null) {

                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        loadMessages();

        chatSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });


        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage++;

                itemPosition = 0;

                if(canChildScrollUp) {
                    loadMoreMessages();
                }
                refreshLayout.setRefreshing(false);
            }
        });

    }

    private void sendMessage() {
        String message = chatMsg.getText().toString();

        if(!TextUtils.isEmpty(message)) {

            DatabaseReference user_msg_push = rootRef.child("messages")
                    .child(curr_user_id)
                    .child(chatUser)
                    .push();

            String push_id = user_msg_push.getKey();

            String curr_user_ref = "messages/" + curr_user_id + "/" + chatUser;
            String chat_user_ref = "messages/" + chatUser + "/" + curr_user_id;

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", curr_user_id);

            Map messageUserMap = new HashMap();
            messageUserMap.put(curr_user_ref + "/" + push_id, messageMap);
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

            rootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {

                    if(databaseError != null) {
                        Log.d("Chat log", databaseError.getMessage());
                    }

                }
            });

            chatMsg.setText("");
        }
    }

    private void loadMoreMessages() {

        DatabaseReference messageRef = rootRef.child("messages").child(curr_user_id).child(chatUser);

        Query messageQuery = messageRef.orderByKey().endAt(lastKey).limitToLast(TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages msg = dataSnapshot.getValue(Messages.class);
                String msgKey = dataSnapshot.getKey();
                Log.d("prevKey checking", prevKey);
                Log.d("msgKey checking", msgKey);

                if(!prevKey.equals(msgKey)) {

                    messagesList.add(itemPosition++, msg);

                } else {

                    prevKey = lastKey;
                    canChildScrollUp = false;

                }

                if(itemPosition == 1) {

                    lastKey = msgKey;

                }

                adapter.notifyDataSetChanged();
                refreshLayout.setRefreshing(false);
                llm.scrollToPositionWithOffset(0, 0);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void loadMessages() {

        DatabaseReference messageRef = rootRef.child("messages").child(curr_user_id).child(chatUser);

        Query messageQuery = messageRef.limitToLast(currentPage * TOTAL_ITEMS_TO_LOAD);


        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages msg = dataSnapshot.getValue(Messages.class);

                itemPosition++;

                if(itemPosition == 1) {
                    String messageKey = dataSnapshot.getKey();

                    lastKey = messageKey;
                    prevKey = messageKey;
                }


                messagesList.add(msg);
                adapter.notifyDataSetChanged();

                msgListView.scrollToPosition(messagesList.size()-1);

                refreshLayout.setRefreshing(false);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
