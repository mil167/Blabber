package com.example.blabber;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView profile_image;
    private TextView profile_displayname;
    private TextView profile_status;
    private TextView profile_friends;
    private Button profile_request;
    private Button profile_decline;

    private DatabaseReference userDatabase;
    private DatabaseReference friendRequestDatabase;
    private DatabaseReference friendDatabase;
    private DatabaseReference notificationDatabase;
    private DatabaseReference rootRef;

    private FirebaseUser curr_user;

    private String current_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        profile_displayname = findViewById(R.id.profile_displayname);
        profile_status = findViewById(R.id.profile_status);
        profile_friends = findViewById(R.id.profile_friends);
        profile_image = findViewById(R.id.profile_image);
        profile_request = findViewById(R.id.profile_request);
        profile_decline = findViewById(R.id.profile_decline);

        profile_decline.setVisibility(View.INVISIBLE);
        profile_decline.setEnabled(false);

        current_state = "Not_Friends";

        curr_user = FirebaseAuth.getInstance().getCurrentUser();

        userDatabase = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .child(user_id);

        friendRequestDatabase = FirebaseDatabase.getInstance()
                .getReference()
                .child("Friend_Requests");

        friendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");

        notificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");

        rootRef = FirebaseDatabase.getInstance().getReference();

        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String displayName = dataSnapshot.child("name").getValue().toString();
                String displayStatus = dataSnapshot.child("status").getValue().toString();
                String displayImage = dataSnapshot.child("image").getValue().toString();

                profile_displayname.setText(displayName);
                profile_status.setText(displayStatus);
                if(!displayImage.equals("default")) {
                    Picasso.get()
                            .load(displayImage)
                            .into(profile_image);
                } else {
                    Picasso.get()
                            .load(R.drawable.profile_trans)
                            .into(profile_image);
                }

                if(curr_user.getUid().equals(user_id)){

                    profile_decline.setEnabled(false);
                    profile_decline.setVisibility(View.INVISIBLE);

                    profile_request.setEnabled(false);
                    profile_request.setVisibility(View.INVISIBLE);

                }

                friendRequestDatabase.child(curr_user.getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)) {
                            String request_type = dataSnapshot.child(user_id)
                                    .child("request_type")
                                    .getValue()
                                    .toString();
                            if(request_type.equals("received")) {

                                current_state = "request_received";
                                profile_request.setText("Accept Friend Request");

                                profile_decline.setVisibility(View.VISIBLE);
                                profile_decline.setEnabled(true);

                            } else if(request_type.equals("sent")) {

                                current_state = "request_sent";
                                profile_request.setText("Cancel Friend Request");

                                profile_decline.setVisibility(View.INVISIBLE);
                                profile_decline.setEnabled(false);

                            }

                        } else {

                            friendDatabase.child(curr_user.getUid())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)) {
                                        current_state = "Friends";
                                        profile_request.setText("Unfriend User");


                                        profile_decline.setVisibility(View.INVISIBLE);
                                        profile_decline.setEnabled(false);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        profile_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                profile_request.setEnabled(false);

                if(current_state.equals("Friends")) {

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + curr_user.getUid() + "/" + user_id, null);
                    unfriendMap.put("Friends/" + user_id + "/" + curr_user.getUid(), null);

                    rootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener(){

                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError == null){

                                current_state = "Not_Friends";
                                profile_request.setText("Send Friend Request");

                                profile_decline.setVisibility(View.INVISIBLE);
                                profile_decline.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();

                            }

                            profile_request.setEnabled(true);
                        }
                    });

                }

                if(current_state.equals("Not_Friends")) {

                    DatabaseReference newNotificationRef = rootRef.child("Notifications").child(user_id).push();
                    String newNotificationID = newNotificationRef.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", curr_user.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_Requests/" + curr_user.getUid()+ "/" + user_id + "/request_type", "sent");
                    requestMap.put("Friend_Requests/" + user_id + "/" + curr_user.getUid() + "/request_type", "received");
                    requestMap.put("Notifications/" + user_id + "/" + newNotificationID, notificationData);

                    rootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {

                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError != null) {
                                Toast.makeText(ProfileActivity.this, "Error sending friend request!", Toast.LENGTH_LONG).show();
                            } else {

                                profile_request.setText("Cancel Friend Request");
                                current_state = "request_sent";
                            }
                            profile_request.setEnabled(true);
                        }

                    });

                }

                if(current_state.equals("request_sent")) {

                    friendRequestDatabase.child(curr_user.getUid())
                            .child(user_id).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            friendRequestDatabase.child(user_id).child(curr_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    profile_request.setEnabled(true);
                                    current_state = "Not_Friends";
                                    profile_request.setText("Send Friend Request");

                                    profile_decline.setVisibility(View.INVISIBLE);
                                    profile_decline.setEnabled(false);

                                }
                            });

                        }
                    });

                }

                if(current_state.equals("request_received")) {

                    final String currDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + curr_user.getUid() + "/" + user_id + "/date", currDate);
                    friendsMap.put("Friends/" + user_id + "/" + curr_user.getUid() + "/date", currDate);

                    friendsMap.put("Friend_Requests/" + curr_user.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend_Requests/" + user_id + "/" + curr_user.getUid(), null);

                    rootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                            if(databaseError == null){

                                profile_request.setEnabled(true);
                                current_state = "friends";
                                profile_request.setText("Unfriend User");

                                profile_decline.setVisibility(View.INVISIBLE);
                                profile_decline.setEnabled(false);

                            } else {

                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_SHORT).show();


                            }

                        }
                    });


                }
            }
        });
    }
}
