package com.kishorekethineni.clipboardmanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class ForegroundService extends Service implements ClipboardManager.OnPrimaryClipChangedListener {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    ClipboardManager clipboardManager;
    String clipdata;

    @Override
    public void onCreate() {
        ClipboardManager clipBoard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipBoard.addPrimaryClipChangedListener(this);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        onPrimaryClipChanged();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onPrimaryClipChanged() {
        ClipboardManager clipBoard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        CharSequence pasteData = "";
        if (clipBoard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) ||
                clipBoard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML))
        {
            Log.d("Copied!", "Copied!");
        } else {
            ClipData.Item item = clipBoard.getPrimaryClip().getItemAt(0);
            pasteData = item.getText();
            Toast.makeText(getApplicationContext(), "copied val=" + pasteData,
                    Toast.LENGTH_SHORT).show();
            SharedPreferences sp = getSharedPreferences("Kish", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("key", pasteData.toString());
            editor.apply();
            createNotificationChannel();
        }
    }
}
