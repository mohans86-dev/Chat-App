package com.example.chatapp.activity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.adapter.MessageAdapter;
import com.example.chatapp.models.MsgModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

public class ChatWindowActivity extends AppCompatActivity {
    MediaPlayer sendSound, receiveSound;


    String receiverUid, receiverName, receiverImg, senderUid;
    ImageView profileImage;
    TextView userName;
    EditText messageInput;
    ImageButton sendBtn;
    RecyclerView chatRecyclerView;

    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference chatReference;

    String senderRoom, receiverRoom;

    ArrayList<MsgModel> msgModelArrayList;
    MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        setContentView(R.layout.activity_chat_window);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // sound effects
        sendSound = MediaPlayer.create(this, R.raw.send_sound);
        receiveSound = MediaPlayer.create(this, R.raw.receive_sound);


        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Get data from Intent
        receiverName = getIntent().getStringExtra("NAME");
        receiverImg = getIntent().getStringExtra("PROFILE_IMG");
        receiverUid = getIntent().getStringExtra("UID");

        // Bind Views
        profileImage = findViewById(R.id.profileImage);
        userName = findViewById(R.id.userName);
        messageInput = findViewById(R.id.messageInput);
        sendBtn = findViewById(R.id.sendBtn);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);

        // Set user info
        userName.setText(receiverName);
        // glide

        // Setup IDs and Rooms
        senderUid = auth.getUid();
        senderRoom = senderUid + "_" + receiverUid;
        receiverRoom = receiverUid + "_" + senderUid;

        // Initialize list & adapter
        msgModelArrayList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, msgModelArrayList);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(messageAdapter);


        // Fetch messages in real-time
        chatReference = database.getReference().child("chats").child(senderRoom).child("messages");
        chatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                msgModelArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    MsgModel msgModel = dataSnapshot.getValue(MsgModel.class);
                    msgModelArrayList.add(msgModel);
                }

                messageAdapter.notifyDataSetChanged();

                if (!msgModelArrayList.isEmpty()) {
                    chatRecyclerView.scrollToPosition(msgModelArrayList.size() - 1);

                    // 🔔 Play receive sound only for messages not sent by current user
                    MsgModel lastMsg = msgModelArrayList.get(msgModelArrayList.size() - 1);
                    if (lastMsg != null && !lastMsg.getSenderId().equals(senderUid)) {
                        if (receiveSound != null) receiveSound.start();
                    }
                }

                // Empty chat text
                TextView emptyChatText = findViewById(R.id.emptyChatText);

                if (msgModelArrayList.isEmpty()) {
                    emptyChatText.setVisibility(View.VISIBLE);
                } else {
                    emptyChatText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatWindowActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Send Button Logic
        sendBtn.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (message.isEmpty()) {
                Toast.makeText(ChatWindowActivity.this, "Type something before sending", Toast.LENGTH_SHORT).show();
                return;
            }

            messageInput.setText("");

            Date date = new Date();
            MsgModel msgModel = new MsgModel(message, senderUid, date.getTime());

            DatabaseReference chatRef = database.getReference().child("chats");

            // 🔊 Play send sound
            if (sendSound != null) sendSound.start();

            chatRef.child(senderRoom).child("messages").push().setValue(msgModel).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    chatRef.child(receiverRoom).child("messages").push().setValue(msgModel);
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sendSound != null) {
            sendSound.release();
            sendSound = null;
        }
        if (receiveSound != null) {
            receiveSound.release();
            receiveSound = null;
        }
    }

}
