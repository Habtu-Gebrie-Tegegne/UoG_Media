package com.example.uog_media;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ClickPostActivity extends AppCompatActivity {

    private ImageView PostImage;
    private TextView PostDescription;
    private Button DeletePostButton,EditPostButton;

    private String PostKey,currentUserId,databaseUserId;
    private String description,image;

    private DatabaseReference ClickPostRef;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        PostKey=getIntent().getExtras().get("PostKey").toString();
        ClickPostRef= FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);
        mAuth=FirebaseAuth.getInstance();
       currentUserId=mAuth.getCurrentUser().getUid();

        PostImage=(ImageView)findViewById(R.id.click_post_image);
        PostDescription=(TextView)findViewById(R.id.click__post_description);
        DeletePostButton=(Button)findViewById(R.id.delete_post_button);
        EditPostButton=(Button)findViewById(R.id.edit_post_button);

        DeletePostButton.setVisibility(View.INVISIBLE);
        EditPostButton.setVisibility(View.INVISIBLE);

ClickPostRef.addValueEventListener(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

           if(dataSnapshot.exists()){

               description = dataSnapshot.child("description").getValue().toString();
               image = dataSnapshot.child("postimage").getValue().toString();
               PostDescription.setText(description);
               Picasso.get().load(image).placeholder(R.drawable.profile).into(PostImage);

               databaseUserId=dataSnapshot.child("uid").getValue().toString();
               if(currentUserId.equals(databaseUserId)){
                   DeletePostButton.setVisibility(View.VISIBLE);
                   EditPostButton.setVisibility(View.VISIBLE);
               }

           }

           EditPostButton.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   EditCurrentPost(description);
               }
           });
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
});

DeletePostButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        DeleteCurrentPost();
    }
});

    }

    private void EditCurrentPost(String description) {

        AlertDialog.Builder builder=new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("edit post : ");

        final EditText inputField=new EditText(ClickPostActivity.this);
        inputField.setText(description);
        builder.setView(inputField);

        builder.setPositiveButton("update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ClickPostRef.child("description").setValue(inputField.getText().toString());
                Toast.makeText(ClickPostActivity.this, "post updated succesfully", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        Dialog dia_log=builder.create();
        dia_log.show();
        dia_log.getWindow().setBackgroundDrawableResource(android.R.color.holo_green_dark);

    }


    private void DeleteCurrentPost() {
        ClickPostRef.removeValue();
        SendUserToMainActivity();
        Toast.makeText(this, "post  has been deleted successfully", Toast.LENGTH_SHORT).show();
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(ClickPostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}
