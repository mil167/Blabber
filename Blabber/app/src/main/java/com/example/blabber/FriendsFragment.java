package com.example.blabber;


import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment {

    private RecyclerView friendsList;

    private DatabaseReference friendsDatabase;
    private DatabaseReference usersDatabase;
    private FirebaseAuth auth;

    private FirebaseRecyclerOptions<Friends> options;
    private FirebaseRecyclerAdapter<Friends, FriendsFragment.FriendViewHolder> fbra;

    private String curr_user_id;

    private View mainView;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mainView = inflater.inflate(R.layout.fragment_friends, container, false);

        friendsList = mainView.findViewById(R.id.friends_list);

        auth = FirebaseAuth.getInstance();

        curr_user_id = auth.getCurrentUser().getUid();

        friendsDatabase = FirebaseDatabase.getInstance()
                .getReference()
                .child("Friends")
                .child(curr_user_id);

        friendsDatabase.keepSynced(true);

        usersDatabase = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users");

        usersDatabase.keepSynced(true);

        friendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        Query query = friendsDatabase;

        options = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(query, Friends.class)
                .build();

        fbra = new FirebaseRecyclerAdapter<Friends, FriendViewHolder>(options) {

            @NonNull
            @Override
            public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.single_user_layout, viewGroup, false);
                return new FriendViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final FriendViewHolder holder, int position, @NonNull final Friends model) {
                holder.setDate(model.getDate());

                final String list_user_id = getRef(position).getKey();

                usersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        final String user_name = dataSnapshot.child("name").getValue().toString();
                        String user_img = dataSnapshot.child("image").getValue().toString();

                        if(dataSnapshot.hasChild("online")) {
                            String user_online = dataSnapshot.child("online").getValue().toString();
                            holder.setOnline(user_online);
                        }
                        holder.setName(user_name);
                        holder.setImage(user_img);

                        holder.view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                CharSequence modaloptions [] = new CharSequence[]
                                        {"Open Profile", "Send Message"};

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle("Select Option:");
                                builder.setItems(modaloptions, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if(i == 0) {
                                            Intent profileIntent = new Intent(getContext(), ProfileActivity.class);
                                            profileIntent.putExtra("user_id", list_user_id);
                                            startActivity(profileIntent);
                                        }

                                        if(i == 1) {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("user_id", list_user_id);
                                            chatIntent.putExtra("user_name", user_name);
                                            startActivity(chatIntent);
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });

                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        };

        friendsList.setAdapter(fbra);

        // Inflate the layout for this fragment
        return mainView;
    }

    @Override
    public void onStart() {
        super.onStart();
        fbra.startListening();

    }
    @Override
    public void onStop() {

        super.onStop();
        fbra.stopListening();
    }

    @Override
    public void onResume() {

        super.onResume();
        fbra.startListening();
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {

        View view;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);

            view = itemView;
        }

        public void setName(String name) {
            TextView userName = view.findViewById(R.id.user_name);
            userName.setText(name);
        }

        public void setDate(String date) {
            TextView userStatus = view.findViewById(R.id.user_status);
            userStatus.setText(date);
        }

        public void setOnline(String online) {
            ImageView userOnline = view.findViewById(R.id.user_online);

            Log.d("online check", online);

            if(online.equals("true")) {
                Log.d("online check", "User is online");
                userOnline.setVisibility(View.VISIBLE);
            } else {
                Log.d("online check", "User is offline");
                userOnline.setVisibility(View.INVISIBLE);
            }

        }

        public void setImage(String image) {

            CircleImageView userImage = view.findViewById(R.id.user_pic);
            if(!image.equals("default")) {
                Picasso.get()
                        .load(image)
                        .into(userImage);
            } else {
                Picasso.get()
                        .load(R.drawable.profile_trans)
                        .into(userImage);
            }
        }
    }

}
