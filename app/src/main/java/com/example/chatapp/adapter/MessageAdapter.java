package com.example.chatapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.R;
import com.example.chatapp.models.MsgModel;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final ArrayList<MsgModel> msgModelArrayList;

    private static final int ITEM_SEND = 1;
    private static final int ITEM_RECEIVE = 2;

    public MessageAdapter(Context context, ArrayList<MsgModel> msgModelArrayList) {
        this.context = context;
        this.msgModelArrayList = msgModelArrayList;
    }

    @Override
    public int getItemCount() {
        return msgModelArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        MsgModel msgModel = msgModelArrayList.get(position);
        if (FirebaseAuth.getInstance().getUid().equals(msgModel.getSenderId())) {
            return ITEM_SEND;
        } else {
            return ITEM_RECEIVE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == ITEM_SEND) {
            View view = LayoutInflater.from(context).inflate(R.layout.sender_layout, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.receiver_layout, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MsgModel msgModel = msgModelArrayList.get(position);

        // Format timestamp into readable time (e.g. 10:45 AM)
        String formattedTime = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(new Date(msgModel.getTimeStamp()));

        if (holder.getClass() == SenderViewHolder.class) {
            SenderViewHolder viewHolder = (SenderViewHolder) holder;
            viewHolder.senderMessage.setText(msgModel.getMessage());
            viewHolder.senderTime.setText(formattedTime);

        } else {
            ReceiverViewHolder viewHolder = (ReceiverViewHolder) holder;
            viewHolder.receiverMessage.setText(msgModel.getMessage());
            viewHolder.receiverTime.setText(formattedTime);
        }
    }

    // Sender ViewHolder
    static class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView senderMessage, senderTime;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessage = itemView.findViewById(R.id.senderMessage);
            senderTime = itemView.findViewById(R.id.senderTime);
        }
    }

    // Receiver ViewHolder
    static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        TextView receiverMessage, receiverTime;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiverMessage = itemView.findViewById(R.id.receiverMessage);
            receiverTime = itemView.findViewById(R.id.receiverTime);
        }
    }
}
