package com.example.uog_media;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private Toolbar chatToolBar;
    private ImageButton sendMessageButton,sendImageFileButton;
    private EditText userMessageInput ;

    private RecyclerView userMessagesList;
    private final List<Messages> messagesList=new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messageAdapter;

    private String messageRecieverId,messageRecieverName,messageSenderId,saveCurrentDate, saveCurrentTime;

    private TextView recieverName,userLastSeen;
    private CircleImageView recieverProfileImage;

    private DatabaseReference RootRef,UssersRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth=FirebaseAuth.getInstance();
        messageSenderId=mAuth.getCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();
        UssersRef=FirebaseDatabase.getInstance().getReference().child("Users");

        messageRecieverId =getIntent().getExtras().get("visit_user_id").toString();
        messageRecieverName =getIntent().getExtras().get("userName").toString();

InitializeFields();

DisplayReceiverInfo();

sendMessageButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        SendMessage();
    }
});

FetchMessages();

    }




    private void FetchMessages() {
       RootRef.child("Messages").child(messageSenderId).child(messageRecieverId)
               .addChildEventListener(new ChildEventListener() {
                   @Override
                   public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                       if(dataSnapshot.exists()){
                           Messages messages=dataSnapshot.getValue(Messages.class);
                           messagesList.add(messages);
                           messageAdapter.notifyDataSetChanged();

                       }

                   }

                   @Override
                   public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                   }

                   @Override
                   public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                   }

                   @Override
                   public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                   }

                   @Override
                   public void onCancelled(@NonNull DatabaseError databaseError) {

                   }
               });

    }

    private void SendMessage() {

        UpdateUserStatus("online");

         String messageText=userMessageInput.getText().toString();
         if(TextUtils.isEmpty(messageText)){
             Toast.makeText(this, "please type a message first . . .", Toast.LENGTH_SHORT).show();
         }
         else{
             String message_sender_ref="Messages/"+messageSenderId+"/"+messageRecieverId;
             String message_receiver_ref="Messages/"+messageRecieverId+"/"+messageSenderId;

             DatabaseReference user__message_key=RootRef.child("Messages").child(messageSenderId)
                     .child(messageRecieverId).push();
             String message_push_id=user__message_key.getKey();

             Calendar calFordDate = Calendar.getInstance();
             SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
             saveCurrentDate = currentDate.format(calFordDate.getTime());

             Calendar calFordTime = Calendar.getInstance();
             SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm aa");
             saveCurrentTime =  currentTime.format(calFordDate.getTime());

             Map messageTextBody=new HashMap();
             messageTextBody.put("message",messageText);
             messageTextBody.put("time",saveCurrentTime);
             messageTextBody.put("date",saveCurrentDate);
             messageTextBody.put("type","text");
             messageTextBody.put("from",messageSenderId);

             Map messageBodyDetails=new HashMap();
                  messageBodyDetails.put(message_sender_ref + "/" + message_push_id,messageTextBody);
                  messageBodyDetails.put(message_receiver_ref + "/" + message_push_id,messageTextBody);

               RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                   @Override
                   public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "message sent successfully.", Toast.LENGTH_SHORT).show();
                        userMessageInput.setText("");
                    }else{
                        String message=task.getException().getMessage();
                        Toast.makeText(ChatActivity.this, "error" + message, Toast.LENGTH_SHORT).show();
                        userMessageInput.setText("");
                    }


                   }
               });



         }

    }


    public void UpdateUserStatus(String state){
        String saveCurrentDate,saveCurrentTime;

        Calendar calFordDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calFordDate.getTime());

        Calendar calFordTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime =  currentTime.format(calFordTime.getTime());

        Map currentStateMap=new HashMap();
        currentStateMap.put("time",saveCurrentTime);
        currentStateMap.put("date",saveCurrentDate);
        currentStateMap.put("type",state);

        UssersRef.child(messageSenderId).child("usersState")
                .updateChildren(currentStateMap);
    }


    private void DisplayReceiverInfo() {
       recieverName.setText(messageRecieverName);

       RootRef.child("Users").child(messageRecieverId)
               .addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
    if(dataSnapshot.exists()){
        final String profileImage=dataSnapshot.child("profileimage").getValue().toString();
        Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(recieverProfileImage);

      if(dataSnapshot.hasChild("usersState")) {
          final String type = dataSnapshot.child("usersState").child("type").getValue().toString();
          final String lastDate = dataSnapshot.child("usersState").child("date").getValue().toString();
          final String lastTime = dataSnapshot.child("usersState").child("time").getValue().toString();


          if (type.equals("online")) {
              userLastSeen.setText("online");

          } else {
              userLastSeen.setText("Last Seen : " + lastTime + ("  ") + lastDate);

          }

      }

    }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError databaseError) {

           }
       });


    }



    private void InitializeFields() {
         chatToolBar=(Toolbar) findViewById(R.id.chat_bar_layout);
         setSupportActionBar(chatToolBar);

         ActionBar actionBar=getSupportActionBar();
         actionBar.setDisplayHomeAsUpEnabled(true);
         actionBar.setDisplayShowCustomEnabled(true);
         LayoutInflater layoutInflater=(LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         View action_bar_view=layoutInflater.inflate(R.layout.chat_custom_bar,null);
         actionBar.setCustomView(action_bar_view);

        sendImageFileButton=(ImageButton) findViewById(R.id.send_image_file_button);
        sendMessageButton=(ImageButton) findViewById(R.id.send_message_button);
        userMessageInput=(EditText) findViewById(R.id.input_message);

        recieverName=(TextView)findViewById(R.id.custom_profile_name);
        recieverProfileImage=(CircleImageView)findViewById(R.id.custom_profile_image);
        userLastSeen=(TextView)findViewById(R.id.custom_user_last_seen);

        messageAdapter=new MessagesAdapter(messagesList);
        userMessagesList=(RecyclerView) findViewById(R.id.messages_list_users);
        linearLayoutManager=new LinearLayoutManager(this);
        userMessagesList.setHasFixedSize(true);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

    }
}
