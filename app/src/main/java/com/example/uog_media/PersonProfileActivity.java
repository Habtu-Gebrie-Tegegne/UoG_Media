package com.example.uog_media;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity {

    private TextView userName,userProfName,userStatus,userCountry,userGender,userRelation,userDob;
    private CircleImageView userProfileImage;
    private Button SendFriendReqButton,DeclineFriendReqButton;

    private DatabaseReference profileUserRef,UsersRef,FriendRequestRef,FriendsRef;
    private FirebaseAuth mAuth;
    private String senderUserId,recieverUserId,CURRENT_STATE,saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        mAuth=FirebaseAuth.getInstance();
        senderUserId=mAuth.getCurrentUser().getUid();
        recieverUserId=getIntent().getExtras().get("visit_user_id").toString();
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        FriendRequestRef= FirebaseDatabase.getInstance().getReference().child("FriendsRequest");
        FriendsRef= FirebaseDatabase.getInstance().getReference().child("Friends");

        InitializeFields();

        UsersRef.child(recieverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    String myUserName = dataSnapshot.child("username").getValue().toString();
                    String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String myDob = dataSnapshot.child("dob").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String myRelationStatus = dataSnapshot.child("relationshipstatus").getValue().toString();

                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);
                    userName.setText("@ "+myUserName);
                    userProfName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userCountry.setText("user type : "+myCountry);
                    userGender.setText("department : "+myGender);
                    userRelation.setText("year of study : "+myRelationStatus);
                    userDob.setText("campus : "+myDob);

                    MaintenanceOfButtons();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

DeclineFriendReqButton.setVisibility(View.INVISIBLE);
DeclineFriendReqButton.setEnabled(false);

if(!senderUserId.equals(recieverUserId)){
SendFriendReqButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        SendFriendReqButton.setEnabled(false);

if(CURRENT_STATE.equals("not_friends")){
    SendFriendRequestToaPerson();
}
if(CURRENT_STATE.equals("request_sent")){
    CancelFriendRequest();
}
if(CURRENT_STATE.equals("request_received")) {
    AcceptFriendRequest();
}
if(CURRENT_STATE.equals("friends")){
    UnFriendAnExistedFriend();
}

    }
});

}
else{
    SendFriendReqButton.setVisibility(View.INVISIBLE);
    DeclineFriendReqButton.setVisibility(View.INVISIBLE);
}

    }


    private void UnFriendAnExistedFriend(){

        FriendsRef.child(senderUserId).child(recieverUserId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    FriendsRef.child(recieverUserId).child(senderUserId)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                SendFriendReqButton.setEnabled(true);
                                SendFriendReqButton.setText("send friend request");
                                CURRENT_STATE="not_friends";
                                DeclineFriendReqButton.setVisibility(View.INVISIBLE);
                                DeclineFriendReqButton.setEnabled(false);
                            }
                        }
                    });
                }

            }
        });

    }

    private void AcceptFriendRequest() {
        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        FriendsRef.child(senderUserId).child(recieverUserId).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                            FriendsRef.child(recieverUserId).child(senderUserId).child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){

                                                FriendRequestRef.child(senderUserId).child(recieverUserId)
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            FriendRequestRef.child(recieverUserId).child(senderUserId)
                                                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if(task.isSuccessful()){
                                                                        SendFriendReqButton.setEnabled(true);
                                                                        SendFriendReqButton.setText("un friend this person");
                                                                        CURRENT_STATE="friends";
                                                                        DeclineFriendReqButton.setVisibility(View.INVISIBLE);
                                                                        DeclineFriendReqButton.setEnabled(false);
                                                                    }
                                                                }
                                                            });
                                                        }

                                                    }
                                                });


                                            }
                                        }
                                    });

                        }
                    }
                });

    }


    private void CancelFriendRequest() {
        FriendRequestRef.child(senderUserId).child(recieverUserId)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    FriendRequestRef.child(recieverUserId).child(senderUserId)
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful()){
                                SendFriendReqButton.setEnabled(true);
                                SendFriendReqButton.setText("send friend request");
                                CURRENT_STATE="not_friends";
                                DeclineFriendReqButton.setVisibility(View.INVISIBLE);
                                DeclineFriendReqButton.setEnabled(false);
                            }
                        }
                    });
                }

            }
        });

    }

    private void MaintenanceOfButtons()
    {
FriendRequestRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        if(dataSnapshot.hasChild(recieverUserId)){
            String request_type=dataSnapshot.child(recieverUserId).child("request_type").getValue().toString();

            if(request_type.equals("sent")){
                CURRENT_STATE="request_sent";
                SendFriendReqButton.setText("cancel friend request");
                DeclineFriendReqButton.setVisibility(View.INVISIBLE);
                DeclineFriendReqButton.setEnabled(false);
            }
            else if(request_type.equals("received")){
                CURRENT_STATE="request_received";
                SendFriendReqButton.setText("accept friend request");
                DeclineFriendReqButton.setVisibility(View.VISIBLE);
                DeclineFriendReqButton.setEnabled(true);

                DeclineFriendReqButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CancelFriendRequest();
                    }
                });

            }

        }
        else{
            FriendsRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(recieverUserId)) {
                        CURRENT_STATE = "friends";
                        SendFriendReqButton.setText("un friend this person");
                        DeclineFriendReqButton.setVisibility(View.INVISIBLE);
                        DeclineFriendReqButton.setEnabled(false);
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


    private void SendFriendRequestToaPerson() {
FriendRequestRef.child(senderUserId).child(recieverUserId)
        .child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if(task.isSuccessful()){
            FriendRequestRef.child(recieverUserId).child(senderUserId)
                    .child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if(task.isSuccessful()){
                        SendFriendReqButton.setEnabled(true);
                        SendFriendReqButton.setText("cancel friend request");
                        CURRENT_STATE="request_sent";
                        DeclineFriendReqButton.setVisibility(View.INVISIBLE);
                        DeclineFriendReqButton.setEnabled(false);
                    }
                }
            });
        }

    }
});
    }

    private void InitializeFields() {
        userName=(TextView) findViewById(R.id.person_username);
        userProfName=(TextView)findViewById(R.id.person_profile_full_name);
        userStatus=(TextView)findViewById(R.id.person_profile_status);
        userCountry=(TextView)findViewById(R.id.person_country);
        userGender=(TextView)findViewById(R.id.person_gender);
        userRelation=(TextView)findViewById(R.id.person_relationship_status);
        userDob=(TextView)findViewById(R.id.person_dob);
        userProfileImage=(CircleImageView)findViewById(R.id.person_profile_pic);
        SendFriendReqButton=(Button)findViewById(R.id.person_send_friend_request_btn);
        DeclineFriendReqButton=(Button)findViewById(R.id.person_decline_friend_request_btn);
CURRENT_STATE="not_friends";
    }

}
