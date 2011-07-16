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
import android.widget.RemoteViews;

import java.util.HashSet;

public class VolumeWidgetProvider extends AppWidgetProvider {
	// log tag
	private static final String TAG = "VolumeWidgetProvider";

	private HashSet<Integer> _inited;

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		
		AppWidgetManager awm = AppWidgetManager.getInstance(context);
		ComponentName nm = new ComponentName(context, VolumeWidgetProvider.class);
		AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		
		String action = intent.getAction();
		Log.d(TAG, "Received intent " + action);
		if(action.equals("android.media.VOLUME_CHANGED_ACTION")) {
			onUpdate(context, awm, awm.getAppWidgetIds(nm));
		} else if(action.equals(context.getString(R.string.VOLUME_DOWN))) {
			am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
		} else if(action.equals(context.getString(R.string.VOLUME_UP))) {
			am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0);
		}
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.d(TAG, "Volume widget updating");

		AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		int volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
		int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		Log.d(TAG, "Volume is " + String.valueOf(volume) + " / " + String.valueOf(max));

		if(_inited == null) {
			_inited = new HashSet<Integer>();
		}
		
		final int N = appWidgetIds.length;
		for (int i=0; i<N; i++) {
			int appWidgetId = appWidgetIds[i];

			if(!_inited.contains(appWidgetId)) {
				_inited.add(appWidgetId);
			}
			
			updateWidget(context, appWidgetManager, appWidgetId, volume, max);
		}
	}

	static void updateWidget(Context context, AppWidgetManager appWidgetManager,
							 int appWidgetId, int volume, int max) {
		Log.d(TAG, "updateWidget appWidgetId=" + appWidgetId + " volume=" + String.valueOf(volume));

		final String PREFS_NAME = context.getString(R.string.prefs_base_name) + String.valueOf(appWidgetId);
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		int stream = prefs.getInt("stream", -1);
		String name;
		switch(stream) {
		case AudioManager.STREAM_RING:
			name = context.getString(R.string.ringtone_name);
			break;
		case AudioManager.STREAM_MUSIC:
			name = context.getString(R.string.media_name);
			break;
		case AudioManager.STREAM_ALARM:
			name = context.getString(R.string.alarm_name);
			break;
		case AudioManager.STREAM_NOTIFICATION:
			name = context.getString(R.string.notification_name);
			break;
		default:
			name = "";
			break;
		}
		
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		views.setTextViewText(R.id.name, name);
		views.setProgressBar(R.id.volume_bar, max, volume, false);
		views.setOnClickPendingIntent(R.id.down_button,
									  PendingIntent.getBroadcast(context, 0, new Intent(context.getString(R.string.VOLUME_DOWN)), 0));
		views.setOnClickPendingIntent(R.id.up_button,
									  PendingIntent.getBroadcast(context, 0, new Intent(context.getString(R.string.VOLUME_UP)), 0));
		
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}
}


