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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton searchButton;
    private EditText searchInputText;
    private RecyclerView SearchResultList;

    private DatabaseReference allUsersDatabaseRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);


        allUsersDatabaseRef= FirebaseDatabase.getInstance().getReference().child("Users");

        //just only to cast and display back icon and update post title
        mToolbar = (Toolbar) findViewById(R.id.find_friends_appbar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        //cast recycler adapter and set the post order
        SearchResultList = (RecyclerView) findViewById(R.id.search_result_list);
        SearchResultList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        SearchResultList.setLayoutManager(linearLayoutManager);
        // linearLayoutManager.setReverseLayout(true);
        //linearLayoutManager.setStackFromEnd(true);

        searchInputText = (EditText) findViewById(R.id.search_box_input);
        searchButton=(ImageButton) findViewById(R.id.search_people_friends_button);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchBoxInput=searchInputText.getText().toString();
                SearchPeopleAndFriends(searchBoxInput);
            }
        });


    }


    private void SearchPeopleAndFriends(String searchBoxInput) {

        Toast.makeText(this, "searching ...", Toast.LENGTH_SHORT).show();
        Query searchPeopleAndFriendsQuery=allUsersDatabaseRef.orderByChild("fullname")
                .startAt(searchBoxInput).endAt(searchBoxInput+"\uf8ff");

        FirebaseRecyclerOptions<FindFriends> options=
                new FirebaseRecyclerOptions.Builder<FindFriends>()
                        .setQuery(searchPeopleAndFriendsQuery,FindFriends.class).build();

        FirebaseRecyclerAdapter<FindFriends, FindFriendsActivity.PostsViewHolder> adapter=
                new FirebaseRecyclerAdapter<FindFriends, FindFriendsActivity.PostsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendsActivity.PostsViewHolder postsViewHolder, final int i, @NonNull FindFriends posts) {


                        postsViewHolder.username.setText(posts.getFullname());
                        postsViewHolder.status.setText(posts.getStatus());
                        Picasso.get().load(posts.getProfileimage()).placeholder(R.drawable.profile).into(postsViewHolder.image);

                        postsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                 String visit_user_id=getRef(i).getKey();


                                Intent profileIntent = new Intent(FindFriendsActivity.this, PersonProfileActivity.class);
                                profileIntent.putExtra("visit_user_id",visit_user_id);
                                startActivity(profileIntent);
                            }
                        });

                    }

                    @NonNull
                    @Override
                    //to just communicate to the all post layout
                    public FindFriendsActivity.PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout,parent,false);
                        FindFriendsActivity.PostsViewHolder viewHolder=new FindFriendsActivity.PostsViewHolder(view);
                        return viewHolder;
                    }
                };

        SearchResultList.setAdapter(adapter);
        adapter.startListening();


    }


    public static class PostsViewHolder extends  RecyclerView.ViewHolder{
        TextView username,status;
        CircleImageView image;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.all_users_profile_full_name);
            image = itemView.findViewById(R.id.all_users_profile_image);
            status = itemView.findViewById(R.id.all_users_status);

        }
    }


}
