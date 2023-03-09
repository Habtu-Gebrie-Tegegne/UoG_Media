package com.example.uog_media;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostsActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView myPostList;
    private FirebaseAuth mAuth;
    private DatabaseReference PostsRef,UsersRef,LikesRef;
    private String currentUserId;

    boolean likeChecker=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        PostsRef= FirebaseDatabase.getInstance().getReference().child("Posts");
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");


        mToolbar = (Toolbar) findViewById(R.id.my_posts_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Posts");

        myPostList = (RecyclerView) findViewById(R.id.my_all_posts_list);
        myPostList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPostList.setLayoutManager(linearLayoutManager);

        DisplayMyAllPosts();

    }

    private void DisplayMyAllPosts() {

        Query myPostsQuery=PostsRef.orderByChild("uid").startAt(currentUserId)
                .endAt(currentUserId + "\uf8ff");

        FirebaseRecyclerOptions<Posts> options=
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(myPostsQuery,Posts.class).build();


        FirebaseRecyclerAdapter<Posts, MyPostsActivity.PostsViewHolder> adapter=
                new FirebaseRecyclerAdapter<Posts, MyPostsActivity.PostsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull MyPostsActivity.PostsViewHolder postsViewHolder, final int i, @NonNull Posts posts) {

                        final String PostKey=getRef(i).getKey();

                        postsViewHolder.username.setText(posts.getFullname());
                        postsViewHolder.PostTime.setText(posts.getTime());
                        postsViewHolder.PostDate.setText(posts.getDate());
                        postsViewHolder.PostDescription.setText(posts.getDescription());
                        Picasso.get().load(posts.getPostimage()).placeholder(R.drawable.profile).into(postsViewHolder.PostImage);
                        Picasso.get().load(posts.getProfileimage()).placeholder(R.drawable.profile).into(postsViewHolder.image);


                        postsViewHolder.setLikesButtonStatus(PostKey);


                /* RRCYView gets the image 1st(w its key) then it checks whether that single image is
                 clicked or not then goes to the next image (that's how it knows the specific key for clicked post)
               */
                        postsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent clickPostIntent = new Intent(MyPostsActivity.this, ClickPostActivity.class);
                                clickPostIntent.putExtra("PostKey",PostKey);
                                startActivity(clickPostIntent);

                            }
                        });

                        postsViewHolder.commentPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent commentsIntent = new Intent(MyPostsActivity.this, CommentsActivity.class);
                                commentsIntent.putExtra("PostKey",PostKey);
                                startActivity(commentsIntent);
                            }
                        });

                        postsViewHolder.likePostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                likeChecker=true;
                                LikesRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        final String PostKey=getRef(i).getKey();
                                        if(Objects.equals(likeChecker, true)){

                                            if(dataSnapshot.child(PostKey).hasChild(currentUserId)) {
                                                LikesRef.child(PostKey).child(currentUserId).removeValue();
                                                likeChecker = false;
                                            }
                                            else{
                                                LikesRef.child(PostKey).child(currentUserId).setValue(true);
                                                likeChecker = false;
                                            }

                                        }

                                    }
                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }
                        });


                    }

                    @NonNull
                    @Override
                    //to just communicate to the all post layout
                    public MyPostsActivity.PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout,parent,false);
                        MyPostsActivity.PostsViewHolder viewHolder=new MyPostsActivity.PostsViewHolder(view);
                        return viewHolder;

                    }
                };
        myPostList.setAdapter(adapter);
        adapter.startListening();

    }

    public static class PostsViewHolder extends  RecyclerView.ViewHolder{
        TextView   username,PostTime,PostDate,PostDescription;
        CircleImageView image;
        ImageView PostImage;

        ImageButton likePostButton,commentPostButton;
        TextView displayNoOfLikes;
        int countLikes;
        String currenttUserID;
        DatabaseReference LikesRef;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.post_user_name);
            image = itemView.findViewById(R.id.post_profile_image);
            PostTime = itemView.findViewById(R.id.post_time);
            PostDate = itemView.findViewById(R.id.post_date);
            PostDescription =itemView.findViewById(R.id.post_description);
            PostImage = itemView.findViewById(R.id.post_image);

            likePostButton=itemView.findViewById(R.id.like_button);
            commentPostButton=itemView.findViewById(R.id.comment_button);
            displayNoOfLikes=itemView.findViewById(R.id.display_no_of_likes);
            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currenttUserID=FirebaseAuth.getInstance().getCurrentUser().getUid();

        }

        public void setLikesButtonStatus(final String postKey){
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.child(postKey).hasChild(currenttUserID)){
                        countLikes=(int)dataSnapshot.child(postKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.like);
                        displayNoOfLikes.setText((Integer.toString(countLikes)+(" likes")));
                    }else{
                        countLikes=(int)dataSnapshot.child(postKey).getChildrenCount();
                        likePostButton.setImageResource(R.drawable.dislike);
                        displayNoOfLikes.setText(Integer.toString(countLikes) + (" Likes"));

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }


    }

}
