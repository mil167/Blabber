package com.example.blabber;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputLayout status;
    private Button saveButton;
    private TextView status_title;

    private DatabaseReference statusDatabase;
    private FirebaseUser curr_user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        curr_user = FirebaseAuth.getInstance().getCurrentUser();
        String userID = curr_user.getUid();
        statusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);


        toolbar = findViewById(R.id.status_navbar);
        status_title = toolbar.findViewById(R.id.navbar_title);
        status_title.setText("Edit Status");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        status = findViewById(R.id.status_text);
        saveButton = findViewById(R.id.status_save);

        String curr_status = getIntent().getStringExtra("curr_status");

        status.getEditText().setText(curr_status);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String newStatus = status.getEditText().getText().toString();

                statusDatabase.child("status").setValue(newStatus).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Successfully updated status!", Toast.LENGTH_LONG).show();
                            Intent settingsIntent = new Intent(StatusActivity.this, SettingsActivity.class);
                            startActivity(settingsIntent);
                            finish();
                        } else {
                            Toast.makeText(getApplicationContext(), "Error updating status!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

    }
}
