package com.example.uog_media;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private RecyclerView postList;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;

    private ImageButton AddNewPostButton;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef,PostsRef,LikesRef;

    String currentUserID;
    boolean likeChecker=false;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        AddNewPostButton = (ImageButton) findViewById(R.id.add_new_post_button);

        drawerLayout=(DrawerLayout)findViewById(R.id.drawable_layout);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        //this line helps to add nav header to nav view
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        //casting navigation header components
        NavProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        NavProfileUserName = (TextView) navView.findViewById(R.id.nav_user_full_name);

        //just only to set the tool bar with home title at the corner
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        //just only to display the navigation menu drager icon
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //cast recycler adapter and set the post order
        postList = (RecyclerView) findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);


        //to retrieve profile picture and put it on the navigation header
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("fullname"))
                    {
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        NavProfileUserName.setText(fullname);
                    }
                    if(dataSnapshot.hasChild("profileimage"))
                    {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(NavProfileImage);                    }
                    else
                    {
                        Toast.makeText(MainActivity.this, "Profile name do not exists...", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


//get selected items from the navigation view
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                UserMenuSelector(menuItem);
                return false;
            }
        });


        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendUserToPostActivity();
            }
        });

        //here when ever the main activity starts thhe posts gonna display
        DisplayAllUsersPosts();

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

        UsersRef.child(currentUserID).child("usersState")
                .updateChildren(currentStateMap);

    }


    private void DisplayAllUsersPosts() {

Query displayPostsInDescendingOrder=PostsRef.orderByChild("counter");

        FirebaseRecyclerOptions<Posts> options=
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(displayPostsInDescendingOrder,Posts.class).build();

        FirebaseRecyclerAdapter<Posts,PostsViewHolder>adapter=
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull PostsViewHolder postsViewHolder, final int i, @NonNull Posts posts) {



                postsViewHolder.username.setText(posts.getFullname());
                postsViewHolder.PostTime.setText(posts.getTime());
                postsViewHolder.PostDate.setText(posts.getDate());
                postsViewHolder.PostDescription.setText(posts.getDescription());
                Picasso.get().load(posts.getPostimage()).placeholder(R.drawable.profile).into(postsViewHolder.PostImage);
                Picasso.get().load(posts.getProfileimage()).placeholder(R.drawable.profile).into(postsViewHolder.image);

                String PostKeyUsedForLike=getRef(i).getKey();
                postsViewHolder.setLikesButtonStatus(PostKeyUsedForLike);


                /* RRCYView gets the image 1st(w its key) then it checks whether that single image is
                 clicked or not then goes to the next image (that's how it knows the specific key for clicked post)
               */
                postsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String PostKey=getRef(i).getKey();


                        Intent clickPostIntent = new Intent(MainActivity.this, ClickPostActivity.class);
                      clickPostIntent.putExtra("PostKey",PostKey);
                        startActivity(clickPostIntent);

                    }
                });

                postsViewHolder.commentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String PostKey=getRef(i).getKey();


                        Intent commentsIntent = new Intent(MainActivity.this, CommentsActivity.class);
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

                         if(dataSnapshot.child(PostKey).hasChild(currentUserID)) {
                             LikesRef.child(PostKey).child(currentUserID).removeValue();
                             likeChecker = false;
                         }
                         else{
                             LikesRef.child(PostKey).child(currentUserID).setValue(true);
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
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout,parent,false);
                PostsViewHolder viewHolder=new PostsViewHolder(view);
                return viewHolder;

            }
        };
        postList.setAdapter(adapter);
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
                        displayNoOfLikes.setText((Integer.toString(countLikes)+(" Likes")));
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


    private void SendUserToPostActivity() {
        Intent addNewPostIntent = new Intent(MainActivity.this, PostActivity.class);
        startActivity(addNewPostIntent);

    }


    //when app launched these codes excuted
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null)
        {
            SendUserToLoginActivity();
        }
        //uses for 2nd step verfication means to check whether z user fill the setup activity profiles before he login in addition to authentication
        else
        {
            CheckUserExistence();


        }


    }




    //uses for 2nd step verfication means to check whether z user fill the setup activity profiles before he login in addition to authentication
    private void CheckUserExistence() {

        final String current_user_id = mAuth.getCurrentUser().getUid();
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(current_user_id))
                {
                    SendUserToSetupActivity();
                }else{

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToSetupActivity() {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    //to activate navigation menu drager icon
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//this method invoked when nav list pressed
    private void UserMenuSelector(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.nav_post:
                SendUserToPostActivity();
                break;

            case R.id.nav_profile:
                SendUserToProfileActivity();
                break;

            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_friends:
                SendUserToFriendsActivity();
                Toast.makeText(this, "Friend List", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_find_friends:
                SendUserToFindFriendsActivity();
                break;

            case R.id.nav_messages:
                SendUserToFriendsActivity();
                Toast.makeText(this, "Messages", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_settings:
                SendUserToSettingsActivity();
                break;

            case R.id.nav_Logout:
                UpdateUserStatus("offline");
                mAuth.signOut();
                SendUserToLoginActivity();
                break;
        }

    }

    private void SendUserToFriendsActivity() {
        Intent addNewPostIntent = new Intent(MainActivity.this, FriendsActivity.class);
        startActivity(addNewPostIntent);

    }
    private void SendUserToSettingsActivity() {
        Intent addNewPostIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(addNewPostIntent);

    }
    private void SendUserToProfileActivity() {
        Intent addNewPostIntent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(addNewPostIntent);

    }

    private void SendUserToFindFriendsActivity() {
        Intent addNewPostIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(addNewPostIntent);

    }

}
