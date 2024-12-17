package com.example.tracker.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.os.Looper;
import com.example.tracker.R;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class HomeFragment extends Fragment {
    private TextView timerTextView;
    private Button takeOutButton;
    private Button changeButton;
    private boolean isTimerRunning = false;
    private boolean over = false;
    private long startTime; // Start time in milliseconds
    private long elapsedTime = 0; // Total elapsed time
    //private static final long TOTAL_TIME = 150 * 1000;
    private static final long TOTAL_TIME = 8 * 60 * 60 * 1000; // 8 hours in milliseconds
    private Handler handler;
    private Runnable runnable;
    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);

        timerTextView = root.findViewById(R.id.timerTextView);
        takeOutButton = root.findViewById(R.id.takeOutButton);
        changeButton = root.findViewById(R.id.changeButton);

        handler = new Handler(Looper.getMainLooper());
        createNotificationChannel();

        // Setting Initial States
        takeOutButton.setText("Insert");
        timerTextView.setText("00:00:00");
        changeButton.setEnabled(false);

        // Takeout/Insert Button Functionality
        takeOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTimerRunning) {
                    resetTimer();
                } else {
                    startTimer();
                }
            }
        });

        // Change Button Functionality
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTimerRunning) {
                    resetTimer();
                    startTimer();
                }
            }
        });



        return root;
    }

    private void startTimer() {
        isTimerRunning = true;
        over = false;
        startTime = System.currentTimeMillis(); // Record start time
        changeButton.setEnabled(true);

        // Runnable to update the timer every second
        runnable = new Runnable() {
            @Override
            public void run() {
                elapsedTime = System.currentTimeMillis() - startTime; // Calculate elapsed time
                updateTimerText(elapsedTime);
                updateProgress(elapsedTime); // Update the donut chart's progress
                handler.postDelayed(this, 1000); // Update every second
            }
        };
        handler.post(runnable);
        takeOutButton.setText("Take Out"); // Change button text to "Take Out"
    }

    private void resetTimer() {
        isTimerRunning = false;
        handler.removeCallbacks(runnable); // Stop the handler
        elapsedTime = 0;
        updateTimerText(elapsedTime); // Update UI to show reset time
        takeOutButton.setText("Insert"); // Change button text to "Insert"
        changeButton.setEnabled(false);

        // Reset the progress bar
        ProgressBar progressBar = root.findViewById(R.id.progressBar);
        progressBar.setProgress(0);
    }

    private void updateTimerText(long elapsedTime) {
        int seconds = (int) (elapsedTime / 1000) % 60;
        int minutes = (int) ((elapsedTime / (1000 * 60)) % 60);
        int hours = (int) ((elapsedTime / (1000 * 60 * 60)) % 24);

        String timerText = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        timerTextView.setText(timerText);
    }
    private void updateProgress(long elapsedTime) {
        // Calculate progress as a percentage of TOTAL_TIME
        int progress = (int) ((elapsedTime * 100) / TOTAL_TIME);

        if (progress >= 80 && progress < 81) {
            sendNotification("Reminder", "It's almost time to change!");
        } else if (progress >= 100) {
            if (!over){
                sendNotification("Time's Up!", "Change your tampon ASAP!");
            }
        }

        // Ensure progress does not exceed 100%
        if (progress > 100) {
            progress = 100;
            over = true;
        }



        // Set progress on the progress bar
        ProgressBar progressBar = root.findViewById(R.id.progressBar);
        progressBar.setProgress(progress);
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "TimerChannel";
            String description = "Channel for Timer Notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("timerChannel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification(String title, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), "timerChannel")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp) // Use your app's notification icon
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(requireContext());
        notificationManager.notify(1, builder.build());
    }
}