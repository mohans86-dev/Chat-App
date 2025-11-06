package com.example.chatapp.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.User;
import com.example.chatapp.adapter.UserAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    RecyclerView userRecyclerView;
    UserAdapter userAdapter;
    ArrayList<User> userArrayList;

    ImageButton logoutButton, addButton;
    Dialog dialogLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Loading dialog box
        dialogLoading=new Dialog(this);
        dialogLoading.setContentView(R.layout.dialog_loading);

        addButton = findViewById(R.id.addButton);
        logoutButton = findViewById(R.id.logoutButton);

        logoutButton.setOnClickListener(v -> new AlertDialog.Builder(MainActivity.this)
                .setTitle("Logout Alert")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show());

        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users");

        userRecyclerView = findViewById(R.id.userRecyclerView);
        userRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        userArrayList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userArrayList);
        userRecyclerView.setAdapter(userAdapter);

        dialogLoading.show();
        loadUsers();  // ✅ Load only once
    }

    private void loadUsers() {
        String currentUid = auth.getUid();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userArrayList.clear();  // ✅ clear before reload

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);

                    if (user != null && !user.getUserId().equals(currentUid)) {

                        // Get the last message
                        String senderRoom = currentUid + "_" + user.getUserId();
                        DatabaseReference chatRef = firebaseDatabase.getReference("chats")
                                .child(senderRoom)
                                .child("messages");

                        chatRef.orderByKey().limitToLast(1)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot chatSnapshot) {
                                        String lastMessage = "Tap to chat";
                                        for (DataSnapshot msgSnap : chatSnapshot.getChildren()) {
                                            Object messageObj = msgSnap.child("message").getValue();
                                            if (messageObj != null) {
                                                lastMessage = messageObj.toString();
                                            }
                                        }
                                        user.setLastMessage(lastMessage);
                                        userArrayList.add(user);
                                        dialogLoading.dismiss();

                                        // ✅ Only update when all users are processed
                                        if (userArrayList.size() == snapshot.getChildrenCount() - 1) {
                                            userAdapter.notifyDataSetChanged();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}
