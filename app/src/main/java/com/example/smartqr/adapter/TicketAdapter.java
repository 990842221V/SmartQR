package com.example.smartqr.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.smartqr.R;
import com.example.smartqr.model.Ticket;

import java.util.List;

public class TicketAdapter extends RecyclerView.Adapter<TicketAdapter.ViewHolder> {
    private List<Ticket> ticketList;
    private OnTicketClickListener listener;

    public interface OnTicketClickListener {
        void onEditClick(Ticket ticket);
        void onItemClick(Ticket ticket);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtName, txtTicketId, txtStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_ticket_name);
            txtTicketId = itemView.findViewById(R.id.txt_ticket_id);
            txtStatus = itemView.findViewById(R.id.txt_ticket_status);
        }
    }

    public TicketAdapter(List<Ticket> ticketList, OnTicketClickListener listener) {
        this.ticketList = ticketList;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ticket, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Ticket ticket = ticketList.get(position);

        holder.txtName.setText(ticket.getName());
        holder.txtTicketId.setText("ID: " + ticket.getTicketId());
        holder.txtStatus.setText(ticket.getStatus());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(ticket);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) listener.onEditClick(ticket);
            return true;
        });
    }

    @Override
    public int getItemCount() { return ticketList.size(); }

    public void updateList(List<Ticket> newList) {
        ticketList = newList;
        notifyDataSetChanged();
    }
}
