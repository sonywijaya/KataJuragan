package com.sonywijaya.katajuragan;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Sony Surya on 28/05/2017.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Message> messageList;
    private Context context;
    private Message message;

    public MessageAdapter(Context context, List<Message> messageList){
        this.context = context;
        this.messageList = messageList;
    }

    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_all, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        message = messageList.get(position);
        holder.textSender.setText(message.getPartner_name());
        holder.textMessageDate.setText(message.getUpdated_at());
        holder.textMessage.setText(Html.fromHtml(Html.fromHtml(message.getLast_message()).toString()));
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textSender, textMessage, textMessageDate;
        public CardView cardMessage;
        public ViewHolder(View itemView) {
            super(itemView);
            textSender = (TextView) itemView.findViewById(R.id.textSender);
            textMessage = (TextView) itemView.findViewById(R.id.textMessage);
            textMessageDate = (TextView) itemView.findViewById(R.id.textMessageDate);
            cardMessage = (CardView) itemView.findViewById(R.id.cardMessage);
        }
    }
}
