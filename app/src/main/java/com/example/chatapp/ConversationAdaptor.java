package com.example.chatapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class ConversationAdaptor extends RecyclerView.Adapter<ConversationAdaptor.MyViewHolder> implements Filterable {

    private final Context context;
    public ArrayList<Person> personsList;
    public ArrayList<Person> filteredPersonLists;
    private final SelectItemListener listener;
    private static final int[] AVATARS = new int[]{
            R.drawable.ic_avatar_cat_pastel,
            R.drawable.ic_avatar_bunny_pastel,
            R.drawable.ic_avatar_bear_pastel
    };

    public ConversationAdaptor(Context context, ArrayList<Person> personsList, SelectItemListener listener) {
        this.context = context;
        this.personsList = personsList != null ? personsList : new ArrayList<>();
        this.filteredPersonLists = this.personsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationAdaptor.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.conversation_field, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationAdaptor.MyViewHolder holder, int position) {
        Person person = filteredPersonLists.get(position);
        String name = person.getName();
        String lastMessage = person.getLastMessage();

        long timeStamp = person.getTimeStamp();
        Date dateTime = new Date(timeStamp);
        SimpleDateFormat formatted = new SimpleDateFormat("HH:mm a", Locale.getDefault());
        String formattedTime = formatted.format(dateTime);

        holder.textViewName.setText(name);
        int idx = Math.abs(name.hashCode()) % AVATARS.length;
        holder.conversationAvatar.setImageResource(AVATARS[idx]);
        
        if (Objects.equals(lastMessage, "")) {
            holder.textViewMessage.setText("");
            holder.textViewTime.setText("");
        } else {
            holder.textViewMessage.setText(lastMessage);
            holder.textViewTime.setText(formattedTime);
        }
    }

    @Override
    public int getItemCount() {
        return filteredPersonLists.size();
    }

    @Override
    public Filter getFilter() {
        return conversationFilter;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(ArrayList<Person> ds) {
        personsList = ds != null ? ds : new ArrayList<>();
        filteredPersonLists = personsList;
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView textViewName;
        TextView textViewMessage;
        TextView textViewTime;
        LinearLayout conversationField;
        ImageView conversationAvatar;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.conversationPersonNameField);
            textViewMessage = itemView.findViewById(R.id.conversationMessageField);
            textViewTime = itemView.findViewById(R.id.conversationTimeField);
            conversationAvatar = itemView.findViewById(R.id.conversationAvatar);
            
            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(filteredPersonLists.get(pos), pos);
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemLongClick(filteredPersonLists.get(pos), pos);
                    return true;
                }
                return false;
            });
        }
    }

    public interface SelectItemListener {
        void onItemClick(Person person, int pos);
        void onItemLongClick(Person person, int pos);
    }

    private final Filter conversationFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            String filterPattern = charSequence == null ? "" : charSequence.toString().toLowerCase().trim();
            ArrayList<Person> filteredResults = new ArrayList<>();

            if (filterPattern.isEmpty()) {
                filteredResults = personsList;
            } else {
                for (Person person : personsList) {
                    if (person.getName().toLowerCase().contains(filterPattern)) {
                        filteredResults.add(person);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredResults;
            return results;
        }

        @SuppressWarnings("unchecked")
        @SuppressLint("NotifyDataSetChanged")
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            filteredPersonLists = (ArrayList<Person>) filterResults.values;
            notifyDataSetChanged();
        }
    };
}
