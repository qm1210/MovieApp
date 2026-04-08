package com.example.myapplication;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String CHANNEL_ID = "movie_booking_channel";
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView rvMovies;
    private MovieAdapter adapter;
    private List<Movie> movieList = new ArrayList<>();
    private TextView tvUserEmail;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        tvUserEmail = findViewById(R.id.tvUserEmail);
        btnLogout = findViewById(R.id.btnLogout);
        rvMovies = findViewById(R.id.rvMovies);

        tvUserEmail.setText("Chào, " + (currentUser.getEmail() != null ? currentUser.getEmail() : "Người dùng"));

        rvMovies.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MovieAdapter(movieList, this::bookTicket);
        rvMovies.setAdapter(adapter);

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        });

        checkNotificationPermission();
        createNotificationChannel();
        fetchMovies();
        setupFCM();
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void fetchMovies() {
        db.collection("movies")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        movieList.clear();
                        boolean hasValidData = false;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Movie movie = document.toObject(Movie.class);
                            if (movie.getTitle() != null && !movie.getTitle().isEmpty()) {
                                movieList.add(movie);
                                hasValidData = true;
                            }
                        }
                        adapter.notifyDataSetChanged();
                        if (!hasValidData) addAllSampleData();
                    }
                });
    }

    private void addAllSampleData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            User user = new User(currentUser.getUid(), currentUser.getEmail(), currentUser.getEmail().split("@")[0]);
            db.collection("users").document(currentUser.getUid()).set(user);
        }

        List<Movie> movies = new ArrayList<>();
        movies.add(new Movie("1", "Avengers: Endgame", "Hành động", "Hồi kết hoành tráng.", "https://image.tmdb.org/t/p/w500/or06vSaeun0YTwBUDb7pBs9G7s5.jpg"));
        movies.add(new Movie("2", "Doraemon", "Hoạt hình", "Mèo máy tương lai.", "https://image.tmdb.org/t/p/w500/3Y6986C90v2t57iI742iB4uWvG3.jpg"));
        movies.add(new Movie("3", "Lật Mặt 7", "Tâm lý", "Phim gia đình Việt.", "https://rapchieuphim.com/photos/movies/lat-mat-7-mot-dieu-uoc/lat-mat-7-mot-dieu-uoc-poster.jpg"));

        for (Movie m : movies) db.collection("movies").document(m.getId()).set(m);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 35); // Đặt suất chiếu sau 35 phút để test nhắc trước 30p
        
        db.collection("showtimes").document("s1").set(new Showtime("s1", "1", "t1", cal.getTime(), 120000));
        db.collection("showtimes").document("s2").set(new Showtime("s2", "2", "t1", cal.getTime(), 90000));
        db.collection("showtimes").document("s3").set(new Showtime("s3", "3", "t1", cal.getTime(), 100000));

        rvMovies.postDelayed(this::fetchMovies, 1500);
    }

    private void bookTicket(Movie movie) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        db.collection("showtimes")
                .whereEqualTo("movieId", movie.getId())
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Showtime showtime = queryDocumentSnapshots.getDocuments().get(0).toObject(Showtime.class);
                        processBooking(user, movie, showtime);
                    } else {
                        Toast.makeText(this, "Không có suất chiếu", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void processBooking(FirebaseUser user, Movie movie, Showtime showtime) {
        String ticketId = db.collection("tickets").document().getId();
        Ticket ticket = new Ticket(ticketId, user.getUid(), showtime.getId(), "A1", new Date());

        db.collection("tickets").document(ticketId)
                .set(ticket)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đặt vé thành công!", Toast.LENGTH_SHORT).show();
                    scheduleReminder(movie, showtime);
                    sendBookingNotification(movie, showtime);
                });
    }

    private void scheduleReminder(Movie movie, Showtime showtime) {
        long reminderTime = showtime.getTime().getTime() - (30 * 60 * 1000); // Trước 30 phút
        
        if (reminderTime < System.currentTimeMillis()) {
            reminderTime = System.currentTimeMillis() + 10000; // Nếu đã sát giờ, nhắc sau 10 giây để test
        }

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("movieTitle", movie.getTitle());
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 
                (int) System.currentTimeMillis(), 
                intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        }
    }

    private void sendBookingNotification(Movie movie, Showtime showtime) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM", Locale.getDefault());
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Xác nhận đặt vé")
                .setContentText("Bạn đã đặt vé phim: " + movie.getTitle() + " lúc " + sdf.format(showtime.getTime()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Movie Booking", NotificationManager.IMPORTANCE_DEFAULT);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    private void setupFCM() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) Log.d(TAG, "FCM Token: " + task.getResult());
        });
    }
}
