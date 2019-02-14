package com.example.blabber;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference userDatabase;
    private FirebaseUser curr_user;

    private CircleImageView profile_pic;
    private TextView display_name;
    private TextView profile_status;
    private Button status_button;
    private Button image_button;

    private static final int GALLERY_REQUEST = 1;

    private StorageReference profilePicStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        profile_pic = findViewById(R.id.profile_pic);
        display_name = findViewById(R.id.profile_name);
        profile_status = findViewById(R.id.desc);
        status_button = findViewById(R.id.change_desc);
        image_button = findViewById(R.id.profile_request);

        profilePicStorage = FirebaseStorage.getInstance().getReference();

        curr_user = FirebaseAuth.getInstance().getCurrentUser();
        String curr_uid = curr_user.getUid();

        userDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(curr_uid);
        userDatabase.keepSynced(true);

        userDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumbnail = dataSnapshot.child("thumbnail").getValue().toString();

                display_name.setText(name);
                profile_status.setText(status);


                Picasso.get()
                        .load(image)
                        .into(profile_pic);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        status_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String curr_status = profile_status.getText().toString();

                Intent statusIntent = new Intent(SettingsActivity.this, StatusActivity.class);
                statusIntent.putExtra("curr_status", curr_status);
                startActivity(statusIntent);
            }
        });

        image_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALLERY_REQUEST);

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            Uri imageURI = data.getData();
            // start cropping activity for pre-acquired image saved on the device
            CropImage.activity(imageURI)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                String curr_uid = curr_user.getUid();
                StorageReference filepath = profilePicStorage.child("profile_images").child(curr_uid + ".jpg");


                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        final String downloadURL = uri.toString();

                        userDatabase.child("image").setValue(downloadURL).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    Toast.makeText(SettingsActivity.this,"Profile picture successfully changed!", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
