package com.example.uog_media;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText userName,userProfName,userStatus,userCountry,userGender,userRelation,userDob;
    Button updateAccountSettingButton;
    private CircleImageView userProfImage;
    private ProgressDialog loadingBar;

    private DatabaseReference SettingsUserRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private StorageReference UserProfileImageRef;


    final static int Gallery_Pick=1;
    Uri ImageUri;
    private String downloadImageUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        SettingsUserRef= FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("profile Images");

        //just only to cast and display back icon and update post title
        mToolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("account settings");

        userName=(EditText)findViewById(R.id.settings_username);
        userProfName=(EditText)findViewById(R.id.settings_profile_full_name);
        userStatus=(EditText)findViewById(R.id.settings_status);
        userCountry=(EditText)findViewById(R.id.settings_country);
        userGender=(EditText)findViewById(R.id.settings_gender);
        userRelation=(EditText)findViewById(R.id.settings_relationship_status);
        userDob=(EditText)findViewById(R.id.settings_dob);
        updateAccountSettingButton=(Button)findViewById(R.id.settings_update_account_settings_buttons);
        userProfImage=(CircleImageView)findViewById(R.id.settings_profile_image);
        loadingBar = new ProgressDialog(this);


        SettingsUserRef.addValueEventListener(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists()) {

            String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
            String myUserName = dataSnapshot.child("username").getValue().toString();
            String myProfileName = dataSnapshot.child("fullname").getValue().toString();
            String myProfileStatus = dataSnapshot.child("status").getValue().toString();
            String mydob = dataSnapshot.child("dob").getValue().toString();
            String myCountry = dataSnapshot.child("country").getValue().toString();
            String myGender = dataSnapshot.child("gender").getValue().toString();
            String myRelationStatus = dataSnapshot.child("relationshipstatus").getValue().toString();

            Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfImage);
            userName.setText(myUserName);
            userProfName.setText(myProfileName);
            userStatus.setText(myProfileStatus);
            userCountry.setText(myCountry);
            userGender.setText(myGender);
            userRelation.setText(myRelationStatus);
            userDob.setText(mydob);
        }
    }



    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
});

updateAccountSettingButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        ValidateAccountInfo();
    }
});

userProfImage.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }
});


    }


    //for selecting and cropping profile image and for storing on storage,download and sore on database
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null)
        {
            ImageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, while we updating your profile image...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();


                Uri resultUri = result.getUri();

                final StorageReference filePath = UserProfileImageRef.child(ImageUri.getLastPathSegment()+currentUserId + ".jpg");

                //     StorageReference filePath=getlast,here image stred on storage
                final UploadTask uploadTask= filePath.putFile(resultUri);
                //to download the url and store in the database
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        //to download the url
                        Task<Uri>uriTask=uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if(!task.isSuccessful()){
                                    throw task.getException();
                                }

                                downloadImageUrl=filePath.getDownloadUrl().toString();
                                return filePath.getDownloadUrl();
                            }

                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if(task.isSuccessful()){
                                    //here is the main place where we download the url
                                    Uri downloadUri = task.getResult();
                                    SettingsUserRef.child("profileimage").setValue(downloadUri.toString())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task)
                                                {
                                                    if(task.isSuccessful())
                                                    {
                                                        Intent selfIntent = new Intent(SettingsActivity.this, SettingsActivity.class);
                                                        startActivity(selfIntent);

                                                        Toast.makeText(SettingsActivity.this, "Profile Image stored to Firebase Database Successfully...", Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                    }
                                                    else
                                                    {
                                                        String message = task.getException().getMessage();
                                                        Toast.makeText(SettingsActivity.this, "Error Occured: " + message, Toast.LENGTH_SHORT).show();
                                                        loadingBar.dismiss();
                                                    }
                                                }
                                            });
                                }
                            }
                        });
                    }
                });




            }

            else
            {
                Toast.makeText(this, "Error Occured: Image can not be cropped. Try Again.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }




    private void ValidateAccountInfo() {
        String username = userName.getText().toString();
        String prof = userProfName.getText().toString();
        String status = userStatus.getText().toString();
        String country = userCountry.getText().toString();
        String gender = userGender.getText().toString();
        String relation = userRelation.getText().toString();
        String dob = userDob.getText().toString();

        if (TextUtils.isEmpty(username)){
            Toast.makeText(this, "please write ur name", Toast.LENGTH_SHORT).show();
    }
       else if (TextUtils.isEmpty(prof)){
            Toast.makeText(this, "please write ur profile name", Toast.LENGTH_SHORT).show();
        }
        else   if (TextUtils.isEmpty(status)){
            Toast.makeText(this, "please write ur status", Toast.LENGTH_SHORT).show();
        }
        else  if (TextUtils.isEmpty(country)){
            Toast.makeText(this, "please write ur country", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(gender)){
            Toast.makeText(this, "please write ur gender", Toast.LENGTH_SHORT).show();
        }
        else  if (TextUtils.isEmpty(relation)){
            Toast.makeText(this, "please write ur relation", Toast.LENGTH_SHORT).show();
        }
        else   if (TextUtils.isEmpty(dob)){
            Toast.makeText(this, "please write ur dob", Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Profile Image");
            loadingBar.setMessage("Please wait, while we updating your profile image...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            UpdateAccountInfo(username, prof, status, country, gender, relation, dob);
        }

    }

    private void UpdateAccountInfo(String username, String prof, String status, String country, String gender, String relation, String dob) {
  HashMap userMap=new HashMap();
        userMap.put("username",username);
        userMap.put("fullname",prof);
        userMap.put("status",status);
        userMap.put("dob",dob);
        userMap.put("country",country);
        userMap.put("gender",gender);
        userMap.put("relationshipstatus",relation);
SettingsUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
    @Override
    public void onComplete(@NonNull Task task) {
        if(task.isSuccessful()){
            SendUserToMainActivity();
            Toast.makeText(SettingsActivity.this, "account settings updated successfully...", Toast.LENGTH_SHORT).show();
       loadingBar.dismiss();
        }
        else{
            Toast.makeText(SettingsActivity.this, "error occured while updating ur account setting", Toast.LENGTH_SHORT).show();
loadingBar.dismiss();
        }
    }
});

    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}
