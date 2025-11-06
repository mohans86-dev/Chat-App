package com.example.chatapp.adapter;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.User;
import com.example.chatapp.activity.ChatWindowActivity;
import com.example.chatapp.activity.MainActivity;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.viewholder>  {
    MainActivity mainActivity;
    ArrayList<User> userArrayList;
    public UserAdapter(MainActivity mainActivity, ArrayList<User> userArrayList) {
        this.mainActivity=mainActivity;
        this.userArrayList=userArrayList;
    }

    @NonNull
    @Override
    public UserAdapter.viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mainActivity).inflate(R.layout.user_list_item, parent, false);
        return new viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.viewholder holder, int position) {
        User user=userArrayList.get(position);
        holder.userName.setText(user.getName());
//        holder.userStatus.setText(user.getStatus());
        holder.userLastMsg.setText(user.getLastMessage());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mainActivity, ChatWindowActivity.class);
                intent.putExtra("NAME", user.getName());
                intent.putExtra("PROFILE_IMG", user.getProfilePic());
                intent.putExtra("UID", user.getUserId());
                mainActivity.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }

    public class viewholder extends RecyclerView.ViewHolder {
        RelativeLayout userItem;
        ImageView profileImage;
        TextView userLastMsg, userStatus, userName;
        public viewholder(@NonNull View itemView) {
            super(itemView);

            userItem=itemView.findViewById(R.id.userItem);
            profileImage=itemView.findViewById(R.id.profileImage);
//            userStatus=itemView.findViewById(R.id.userStatus);
            userName=itemView.findViewById(R.id.userName);
            userLastMsg=itemView.findViewById(R.id.userLastMsg);
        }
    }
}
