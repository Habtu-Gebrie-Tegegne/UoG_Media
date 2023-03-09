package com.example.uog_media;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private Button ResetPasswordSenddEmailButton;
    private EditText ResetEmailInput;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mAuth=FirebaseAuth.getInstance();

        ResetPasswordSenddEmailButton=(Button)findViewById(R.id.reset_password_email_button);
        ResetEmailInput=(EditText)findViewById(R.id.reset_password_EMAIL);

        //just only to cast and display back icon and update post title
        mToolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.forget_password_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("reset password ");

        ResetPasswordSenddEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userEmail=ResetEmailInput.getText().toString();
                if(TextUtils.isEmpty(userEmail)){
                    Toast.makeText(ResetPasswordActivity.this, "please enter a valid email address", Toast.LENGTH_SHORT).show();
                }
                else{
                    mAuth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(ResetPasswordActivity.this, "check or visit your email account to reset your password", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(ResetPasswordActivity.this,LoginActivity.class));
                            }else{
                                String message=task.getException().getMessage();
                                Toast.makeText(ResetPasswordActivity.this, "error occurred : " + message, Toast.LENGTH_SHORT).show();

                            }

                        }
                    });
                }
            }
        });


    }
}
