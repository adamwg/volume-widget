package ca.xvx.volume;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.lang.reflect.Field;

import java.util.HashMap;
import java.util.Map;

public class VolumeWidgetProvider extends AppWidgetProvider {
    // log tag
    private static final String TAG = "VolumeWidgetProvider";

    private final Map<Integer, Integer> _streams = new HashMap<Integer, Integer>();
    private static final Map<Integer, Integer> _streamNames = new HashMap<Integer, Integer>();

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if(_streamNames.isEmpty()) {
            _streamNames.put(AudioManager.STREAM_RING, R.string.ring_volume_title);
            _streamNames.put(AudioManager.STREAM_MUSIC, R.string.media_volume_title);
            _streamNames.put(AudioManager.STREAM_ALARM, R.string.alarm_volume_title);
            _streamNames.put(AudioManager.STREAM_NOTIFICATION, R.string.notification_volume_title);
        }

        final AppWidgetManager awm = AppWidgetManager.getInstance(context);
        final ComponentName nm = new ComponentName(context, VolumeWidgetProvider.class);
        final AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        final String action = intent.getAction();
        final int sender = intent.getIntExtra(context.getString(R.string.AWI_EXTRA), -1);
        final int stream = intent.getIntExtra(context.getString(R.string.STREAM_EXTRA), -1);
        final int newVol = intent.getIntExtra(context.getString(R.string.VOL_EXTRA), -1);
        Log.d(TAG, "Received intent " + action + " from " + String.valueOf(sender));

        if(action.equals(context.getString(R.string.VOLUME_CHANGED))) {
            onUpdate(context, awm, awm.getAppWidgetIds(nm));
        } else if(action.equals(context.getString(R.string.VOLUME_DOWN))) {
            am.adjustStreamVolume(stream, AudioManager.ADJUST_LOWER, 0);
        } else if(action.equals(context.getString(R.string.VOLUME_UP))) {
            am.adjustStreamVolume(stream, AudioManager.ADJUST_RAISE, 0);
        } else if(action.equals(context.getString(R.string.VOLUME_SET))) {
            Log.d(TAG, "New volume for stream " + String.valueOf(stream) + " is " + String.valueOf(newVol));
            am.setStreamVolume(stream, newVol, 0);
        }
    }

    private int getStream(Context context, int awi) {
        int stream;
        if(!_streams.containsKey(awi)) {
            final String PREFS_NAME = context.getString(R.string.prefs_base_name) + String.valueOf(awi);
            final SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_READABLE);
            stream = prefs.getInt(context.getString(R.string.STREAM_PREF), -1);
            if(stream >= 0) {
                _streams.put(awi, stream);
            }
        } else {
            stream = _streams.get(awi);
        }

        Log.d(TAG, "App widget ID = " + String.valueOf(awi) + ", stream = " + String.valueOf(stream));
        return stream;
    }

    private int getBackground(Context context, int awi) {
        int background;

        final String PREFS_NAME = context.getString(R.string.prefs_base_name) + String.valueOf(awi);
        final SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_WORLD_READABLE);
        background = prefs.getInt(context.getString(R.string.BACKGROUND_PREF), 0xcc333333);

        Log.d(TAG, "App widget ID = " + String.valueOf(awi) + ", background = " + String.format("0x%x", background));
        return background;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "Volume widget updating");

        int stream;
        int background;
        for(int awi : appWidgetIds) {
            stream = getStream(context, awi);
            background = getBackground(context, awi);
            updateWidget(context, appWidgetManager, awi, stream, background);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);

        for(int awi : appWidgetIds) {
            _streams.remove(awi);
        }
    }

    static void updateWidget(Context context, AppWidgetManager appWidgetManager,
                             int appWidgetId, int stream, int background) {
        Log.d(TAG, "updateWidget appWidgetId=" + appWidgetId + " stream=" + String.valueOf(stream));

        if(stream < 0) {
            return;
        }

        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        String name;

        try {
            name = context.getString(_streamNames.get(stream));
        } catch(Exception e) {
            name = "";
        }

        int volume = am.getStreamVolume(stream);
        int max = am.getStreamMaxVolume(stream);
        Log.d(TAG, "Volume is " + String.valueOf(volume) + " / " + String.valueOf(max));

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setInt(R.id.widget_background, "setBackgroundColor", background);
        views.setTextViewText(R.id.name, name);
        views.setProgressBar(R.id.volume_bar, max, volume, false);

        Intent downIntent = new Intent(context.getString(R.string.VOLUME_DOWN));
        downIntent.putExtra(context.getString(R.string.STREAM_EXTRA), stream);
        downIntent.putExtra(context.getString(R.string.AWI_EXTRA), appWidgetId);
        views.setOnClickPendingIntent(R.id.down_button,
                                      PendingIntent.getBroadcast(context, appWidgetId, downIntent,
                                                                 PendingIntent.FLAG_UPDATE_CURRENT));

        Intent upIntent = new Intent(context.getString(R.string.VOLUME_UP));
        upIntent.putExtra(context.getString(R.string.STREAM_EXTRA), stream);
        upIntent.putExtra(context.getString(R.string.AWI_EXTRA), appWidgetId);
        views.setOnClickPendingIntent(R.id.up_button,
                                      PendingIntent.getBroadcast(context, appWidgetId, upIntent,
                                                                 PendingIntent.FLAG_UPDATE_CURRENT));

        for(int bn = 0; bn <= 15; bn += 1) {
            final String buttonName = "button_" + String.valueOf(bn);
            int id = -1;
            try {
                id = R.id.class.getField(buttonName).getInt(R.id.class);
            } catch(Exception e) {
                Log.e(TAG, "Exception getting ID");
                continue;
            }

            if(bn <= max) {
                final Intent setIntent = new Intent(context.getString(R.string.VOLUME_SET));
                setIntent.putExtra(context.getString(R.string.STREAM_EXTRA), stream);
                setIntent.putExtra(context.getString(R.string.AWI_EXTRA), appWidgetId);
                setIntent.putExtra(context.getString(R.string.VOL_EXTRA), bn);
                final int request = appWidgetId << 16 | bn;
                views.setOnClickPendingIntent(id,
                                              PendingIntent.getBroadcast(context,
                                                                         request, setIntent,
                                                                         PendingIntent.FLAG_UPDATE_CURRENT));
            } else {
                views.setInt(id, "setVisibility", View.GONE);
            }
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}


