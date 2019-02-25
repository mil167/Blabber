package com.example.blabber;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class UsersActivity extends AppCompatActivity {

    private RecyclerView userlist;
    private DatabaseReference userDatabase;
    private FirebaseRecyclerOptions<Users> options;
    private FirebaseRecyclerAdapter<Users, UserViewHolder> fbra;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        Toolbar navbar = findViewById(R.id.users_toolbar);
        TextView user_title = navbar.findViewById(R.id.navbar_title);
        user_title.setText("All Users");

        setSupportActionBar(navbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        userlist = findViewById(R.id.userlist);
        //userlist.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        userlist.setLayoutManager(llm);

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users");

        options = new FirebaseRecyclerOptions.Builder<Users>()
                .setQuery(query, Users.class)
                .build();

        fbra = new FirebaseRecyclerAdapter<Users, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull Users model) {
                holder.setName(model.getName());
                holder.setStatus(model.getStatus());
                holder.setImage(model.getImage());

                final String selected_uid = getRef(position).getKey();

                holder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent profileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("user_id", selected_uid);
                        startActivity(profileIntent);
                    }
                });

            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.single_user_layout, viewGroup, false);
                return new UserViewHolder(view);
            }
        };
        userlist.setAdapter(fbra);


    }
    @Override
    protected void onStart() {
        super.onStart();
        fbra.startListening();

    }
    @Override
    protected void onStop() {

        super.onStop();
        fbra.stopListening();
    }

    @Override
    protected void onResume() {

        super.onResume();
        fbra.startListening();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        View view;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            view = itemView;
        }

        public void setName(String name) {
            TextView userName = view.findViewById(R.id.user_name);
            userName.setText(name);
        }

        public void setStatus(String status) {
            TextView userStatus = view.findViewById(R.id.user_status);
            userStatus.setText(status);
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
