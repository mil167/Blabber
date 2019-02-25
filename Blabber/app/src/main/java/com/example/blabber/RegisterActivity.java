package com.example.blabber;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout displayName;
    private TextInputLayout email;
    private TextInputLayout password;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar navbar = findViewById(R.id.register_toolbar);
        TextView register_title = navbar.findViewById(R.id.navbar_title);
        register_title.setText("Register");

        setSupportActionBar(navbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mAuth = FirebaseAuth.getInstance();

        displayName = findViewById(R.id.reg_displayname);
        email = findViewById(R.id.reg_email);
        password = findViewById(R.id.reg_password);
        Button createUser = findViewById(R.id.reg_button);

        createUser.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                String displayName_val = Objects.requireNonNull(displayName.getEditText()).getText().toString();
                String email_val = Objects.requireNonNull(email.getEditText()).getText().toString();
                String password_val = Objects.requireNonNull(password.getEditText()).getText().toString();

                if(!TextUtils.isEmpty(displayName_val) || !TextUtils.isEmpty(email_val) ||
                    !TextUtils.isEmpty(password_val)) {

                    register_new_user(displayName_val, email_val, password_val);
                }

            }
        });
    }

    private void register_new_user(final String display_name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {

                            FirebaseUser curr_user = FirebaseAuth.getInstance().getCurrentUser();
                            String user_ID = curr_user.getUid();

                            // Write a message to the database
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            final DatabaseReference users = database.getReference().child("Users").child(user_ID);


                            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                @Override
                                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                    String token = task.getResult().getToken();

                                    HashMap<String, String> userMap = new HashMap<>();
                                    userMap.put("name", display_name);
                                    userMap.put("status", "Your status here...");
                                    userMap.put("image", "default");
                                    userMap.put("thumbnail", "default");

                                    userMap.put("device_token", token);

                                    users.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(mainIntent);
                                                finish();
                                            }
                                        }
                                    });
                                }
                            });

                            /*
                            users.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();
                                    }
                                }
                            });
                            */
                        } else {
                            Toast.makeText(RegisterActivity.this, "Please fill out all fields!", Toast.LENGTH_LONG).show();
                        }
                    }

                });
    }
}
