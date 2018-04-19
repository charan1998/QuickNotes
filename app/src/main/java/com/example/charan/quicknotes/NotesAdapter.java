package com.example.charan.quicknotes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesAdapterViewHolder> {

    private NotesAdapterOnClickHandler mOnClickHandler;
    private ArrayList<String> descriptions;
    private ArrayList<String> notes;
    private ArrayList<String> dates;


    NotesAdapter(NotesAdapterOnClickHandler onClickHandler) {
        mOnClickHandler = onClickHandler;
    }

    @NonNull
    @Override
    public NotesAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.notes_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layoutIdForListItem, parent, false);
        return new NotesAdapterViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull NotesAdapterViewHolder holder, int position) {
        int noteLimit = Math.min(notes.get(position).length(), 25);
        int descLimit = Math.min(descriptions.get(position).length(), 15);
        holder.descriptionTextView.setText(descriptions.get(position).substring(0, descLimit).trim() + "...");
        holder.noteTextView.setText(notes.get(position).substring(0, noteLimit).trim() + "...");
        holder.dateTextView.setText(dates.get(position));
    }

    @Override
    public int getItemCount() {
        if (descriptions == null) {
            return 0;
        }
        return descriptions.size();
    }

    interface NotesAdapterOnClickHandler {
        void onClick(int position);
    }

    class NotesAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView descriptionTextView;
        TextView noteTextView;
        TextView dateTextView;

        NotesAdapterViewHolder(View itemView) {
            super(itemView);
            descriptionTextView = itemView.findViewById(R.id.desc_tv);
            noteTextView = itemView.findViewById(R.id.content_tv);
            dateTextView = itemView.findViewById(R.id.date_tv);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            mOnClickHandler.onClick(position);
        }
    }

    public void setNotesData(ArrayList<String> descriptionData, ArrayList<String> notesData, ArrayList<String> datesData) {
        descriptions = descriptionData;
        notes = notesData;
        dates = datesData;
        notifyDataSetChanged();
    }
}
