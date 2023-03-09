package com.example.uog_media;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity {

    private RecyclerView myFriendList;
private DatabaseReference UssersRef,FriendsRef;
private FirebaseAuth mAuth;
private String online_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth=FirebaseAuth.getInstance();
        online_user_id=mAuth.getCurrentUser().getUid();
        UssersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        FriendsRef= FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);

        myFriendList=(RecyclerView)findViewById(R.id.friend_list);
        myFriendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);

        DisplayAllFriends();
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

        UssersRef.child(online_user_id).child("usersState")
                .updateChildren(currentStateMap);
    }



    @Override
    protected void onStart() {
        super.onStart();
        UpdateUserStatus("online");
    }
    @Override
    protected void onStop() {
        super.onStop();
        UpdateUserStatus("offline");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        UpdateUserStatus("offline");
    }



    private void DisplayAllFriends() {


        FirebaseRecyclerOptions<Friends> options=
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(FriendsRef,Friends.class).build();

        FirebaseRecyclerAdapter<Friends, FriendsActivity.PostsViewHolder> adapter=
                new FirebaseRecyclerAdapter<Friends, FriendsActivity.PostsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final FriendsActivity.PostsViewHolder postsViewHolder, final int i, @NonNull Friends posts) {
                        final String userIds=getRef(i).getKey();
                        postsViewHolder.date.setText("friends since : "+posts.getDate());

                        UssersRef.child(userIds).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                final String userName=dataSnapshot.child("fullname").getValue().toString();
                                final String profileImage=dataSnapshot.child("profileimage").getValue().toString();
                                final String type;

                                if(dataSnapshot.hasChild("usersState")){
                                    type=dataSnapshot.child("usersState").child("type").getValue().toString();
                                    if(type.equals("online")){
                                        postsViewHolder.onlineStatusView.setVisibility(View.VISIBLE);
                                    }else{
                                        postsViewHolder.onlineStatusView.setVisibility(View.INVISIBLE);
                                    }

                                }

                                postsViewHolder.fullName.setText(userName);
                                Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(postsViewHolder.profileImage);

                                postsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                     CharSequence options[]=new CharSequence[]
                                                {userName + "'s profile","send message"};
                                        AlertDialog.Builder builder=new AlertDialog.Builder(FriendsActivity.this);
                                        builder.setTitle("select option");

                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                if(i==0){
                                                    Intent clickPostIntent = new Intent(FriendsActivity.this, PersonProfileActivity.class);
                                                    clickPostIntent.putExtra("visit_user_id",userIds);
                                                    startActivity(clickPostIntent);
                                                }
                                                if(i==1){
                                                    Intent clickPostIntent = new Intent(FriendsActivity.this, ChatActivity.class);
                                                    clickPostIntent.putExtra("visit_user_id",userIds);
                                                    clickPostIntent.putExtra("userName",userName);
                                                    startActivity(clickPostIntent);
                                                }

                                            }
                                        });
                                        builder.show();

                                    }
                                });


                            }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    //to just communicate to the all post layout
                    public FriendsActivity.PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout,parent,false);
                        FriendsActivity.PostsViewHolder viewHolder=new FriendsActivity.PostsViewHolder(view);
                        return viewHolder;

                    }
                };
        myFriendList.setAdapter(adapter);
        adapter.startListening();

    }


    public static class PostsViewHolder extends  RecyclerView.ViewHolder{
        TextView date,fullName;
        CircleImageView profileImage;

        ImageView onlineStatusView;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.all_users_status);
            fullName = itemView.findViewById(R.id.all_users_profile_full_name);
            profileImage = itemView.findViewById(R.id.all_users_profile_image);

            onlineStatusView=(ImageView)itemView.findViewById(R.id.all_user_online_icon);

        }

    }

}
