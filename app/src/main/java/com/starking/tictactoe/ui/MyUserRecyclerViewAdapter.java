package com.starking.tictactoe.ui;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.starking.tictactoe.R;
import com.starking.tictactoe.model.User;

import java.util.List;

public class MyUserRecyclerViewAdapter extends RecyclerView.Adapter<MyUserRecyclerViewAdapter.ViewHolder> {

    private final List<User> mValues;

    public MyUserRecyclerViewAdapter(List<User> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.mItem = mValues.get(position);
        holder.textViewName.setText(String.valueOf(mValues.get(position).getName()));

        holder.textViewPoints.setText(String.valueOf(mValues.get(position).getPoints()));

        holder.textViewGamesPlayed.setText(String.valueOf(mValues.get(position).getGamesPlayed()));

    }

    @Override
    public int getItemCount() {

        if (mValues.size() == 0) return 0;

        return mValues.size();

    }

    public void putUsers(List<User> userList) {
        mValues.clear();
        mValues.addAll(userList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView textViewName;
        public final TextView textViewPoints;
        public final TextView textViewGamesPlayed;

        public User mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            textViewName = view.findViewById(R.id.textViewName);
            textViewPoints = view.findViewById(R.id.textViewPointsRanked);
            textViewGamesPlayed = view.findViewById(R.id.textViewGamesPlayed);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + textViewName.getText() + "'";
        }
    }
}
