package com.example.uog_media;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsActivity extends AppCompatActivity {

    private EditText CommentInputText;
    private ImageButton commentPostButton;
    private RecyclerView commentsList;
    private String Post_Key;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef,PostsRef;
    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Post_Key=getIntent().getExtras().get("PostKey").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Comments");



        //cast recycler adapter and set the post order
        commentsList=(RecyclerView)findViewById(R.id.comments_list);
        commentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        commentsList.setLayoutManager(linearLayoutManager);

        CommentInputText=(EditText)findViewById(R.id.comment_input);
        commentPostButton=(ImageButton)findViewById(R.id.post_comment_btn);

commentPostButton.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String userName=dataSnapshot.child("username").getValue().toString();
                    ValidateCommentUserName(userName);
                     CommentInputText.setText("");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
});

    }



    @Override
    protected void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Comments> options=
                new FirebaseRecyclerOptions.Builder<Comments>()
                        .setQuery(PostsRef,Comments.class).build();

        FirebaseRecyclerAdapter<Comments, CommentsActivity.PostsViewHolder> adapter=
                new FirebaseRecyclerAdapter<Comments, CommentsActivity.PostsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull CommentsActivity.PostsViewHolder postsViewHolder, final int i, @NonNull Comments posts) {

                        postsViewHolder.myUsername.setText(posts.getUsername());
                        postsViewHolder.myComment.setText(posts.getComment());
                        postsViewHolder.myDate.setText(posts.getDate());
                        postsViewHolder.myTime.setText(posts.getTime());

                    }

                    @NonNull
                    @Override
                    //to just communicate to the all post layout
                    public CommentsActivity.PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.all_comments_layout,parent,false);
                        CommentsActivity.PostsViewHolder viewHolder=new CommentsActivity.PostsViewHolder(view);
                        return viewHolder;

                    }
                };
        commentsList.setAdapter(adapter);
        adapter.startListening();


    }


    public static class PostsViewHolder extends  RecyclerView.ViewHolder{
        TextView myUsername,myComment,myDate,myTime;
        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);

            myUsername = itemView.findViewById(R.id.comment_username);
            myComment = itemView.findViewById(R.id.comment_text);
            myDate = itemView.findViewById(R.id.comment_date);
            myTime = itemView.findViewById(R.id.comment_time);
        }

    }



    private void ValidateCommentUserName(String userName) {
String commentText=CommentInputText.getText().toString();
if(TextUtils.isEmpty(commentText)){
    Toast.makeText(this, "please write a comment", Toast.LENGTH_SHORT).show();
}
else{
    Calendar calFordDate = Calendar.getInstance();
    SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
  final String  saveCurrentDate = currentDate.format(calFordDate.getTime());

    Calendar calFordTime = Calendar.getInstance();
    SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
   final String  saveCurrentTime = currentTime.format(calFordDate.getTime());

  final String RandomKey = currentUserID+saveCurrentDate + saveCurrentTime;

    HashMap commentsMap=new HashMap();
    commentsMap.put("uid",currentUserID);
    commentsMap.put("comment",commentText);
    commentsMap.put("date",saveCurrentDate);
    commentsMap.put("time",saveCurrentTime);
    commentsMap.put("username",userName);
   PostsRef.child(RandomKey).updateChildren(commentsMap)
           .addOnCompleteListener(new OnCompleteListener() {
       @Override
       public void onComplete(@NonNull Task task) {
          if(task.isSuccessful()){
              Toast.makeText(CommentsActivity.this, "you have commented successfully ", Toast.LENGTH_SHORT).show();
          }else{
              Toast.makeText(CommentsActivity.this, "error occurred try again ...", Toast.LENGTH_SHORT).show();
          }

       }
   });
}

    }



}
