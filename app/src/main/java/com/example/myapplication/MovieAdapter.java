package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private List<Movie> movieList;
    private OnBookClickListener onBookClickListener;

    public interface OnBookClickListener {
        void onBookClick(Movie movie);
    }

    public MovieAdapter(List<Movie> movieList, OnBookClickListener listener) {
        this.movieList = movieList;
        this.onBookClickListener = listener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movieList.get(position);
        holder.tvTitle.setText(movie.getTitle());
        holder.tvGenre.setText(movie.getGenre());
        holder.tvDesc.setText(movie.getDescription());

        // Dùng Glide load ảnh từ URL
        Glide.with(holder.itemView.getContext())
                .load(movie.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.ivPoster);

        holder.btnBook.setOnClickListener(v -> onBookClickListener.onBookClick(movie));
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    static class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvGenre, tvDesc;
        ImageView ivPoster;
        Button btnBook;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvMovieTitle);
            tvGenre = itemView.findViewById(R.id.tvMovieGenre);
            tvDesc = itemView.findViewById(R.id.tvMovieDesc);
            ivPoster = itemView.findViewById(R.id.ivMoviePoster);
            btnBook = itemView.findViewById(R.id.btnBook);
        }
    }
}
