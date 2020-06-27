package com.kishorekethineni.clipboardmanager;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.List;

public class ForegroundService extends Service implements ClipboardManager.OnPrimaryClipChangedListener {
    static final String  ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
    static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";
    ClipboardManager clipboardManager;

    @Override
    public void onCreate() {
        ClipboardManager clipBoard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipBoard != null) {
            clipBoard.addPrimaryClipChangedListener(this);
        }
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_START_FOREGROUND_SERVICE:
                        startForegroundService();
                        Log.i("action",action);
                        Toast.makeText(getApplicationContext(), "Foreground service is started.", Toast.LENGTH_LONG).show();
                        break;
                    case ACTION_STOP_FOREGROUND_SERVICE:
                        stopForegroundService();
                        Toast.makeText(getApplicationContext(), "Foreground service is stopped.", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
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

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onPrimaryClipChanged() {
        ClipData clipData = clipboardManager.getPrimaryClip();
        if (clipData != null && clipData.getItemCount() > 0) {
            ClipData.Item item = clipData.getItemAt(0);
            ClipDescription clipDescription = clipData.getDescription();
            String newStr = "";
            if (clipData.getItemCount() > 0 && clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                if (item.getText() != null) {
                    newStr = item.getText().toString();
                    if (URLUtil.isValidUrl(newStr))
                    {
                        sendNotification(newStr);
                        if (isForeground("com.kishorekethineni.clipboardmanager"))
                        {
                            Intent i = new Intent(this, WebView_ClipBoard.class);
                            i.putExtra("URL",newStr);
                            i.addCategory(Intent.CATEGORY_LAUNCHER);
                            startActivity(i);
                        }
                    }
                }
            } else if (clipData.getItemCount() > 0 && clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_HTML)) {
                newStr = item.getText().toString();
                if (URLUtil.isValidUrl(newStr))
                {
                    sendNotification(newStr);
                    if (isForeground("com.kishorekethineni.clipboardmanager"))
                    {
                        Intent i = new Intent(this, WebView_ClipBoard.class);
                        i.putExtra("URL",newStr);
                        i.addCategory(Intent.CATEGORY_LAUNCHER);
                        startActivity(i);
                    }
                }
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public boolean isForeground(String myPackage) {
        boolean componentinfo_b = false;
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        if (componentInfo != null) {
            componentinfo_b= componentInfo.getPackageName().equals(myPackage);
        }
        return componentinfo_b;
    }
    private void startForegroundService() {
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
            Intent activityIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingActivityIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);
            Intent serviceIntent =new Intent(this, ForegroundService.class);
            serviceIntent.setAction(ACTION_STOP_FOREGROUND_SERVICE);

            PendingIntent pStopSelf = PendingIntent.getService(this, 0, serviceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            NotificationCompat.Action action =new NotificationCompat.Action.Builder(0, "STOP", pStopSelf).build();
            Notification notification =new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Your ClipBoard is getting monitored..")
                    .setContentText("Click on stop to monitor")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .addAction(action)
                    .setContentIntent(pendingActivityIntent).build();
            startForeground(1, notification);
        }
    }

    private void stopForegroundService() {
        Log.d("TAG_FOREGROUND_SERVICE", "Stop foreground service.");
        stopForeground(true);
        stopSelf();
    }
    public void sendNotification(String URL) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(android.R.drawable.ic_dialog_alert);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        builder.setContentIntent(pendingIntent);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round));
        builder.setContentTitle("Found URL");
        builder.setContentText("Now you can open url from this app.");
        builder.setSubText("Tap to view the website.");
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

}
