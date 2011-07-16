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

import java.util.HashMap;
import java.util.Map;

public class VolumeWidgetProvider extends AppWidgetProvider {
	// log tag
	private static final String TAG = "VolumeWidgetProvider";

	private final Map<Integer, Integer> _streams = new HashMap<Integer, Integer>();
	private static final Map<Integer, String> _streamNames = new HashMap<Integer, String>();

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);

		if(_streamNames.isEmpty()) {
			_streamNames.put(AudioManager.STREAM_RING, context.getString(R.string.ringtone_name));
			_streamNames.put(AudioManager.STREAM_MUSIC, context.getString(R.string.media_name));
			_streamNames.put(AudioManager.STREAM_ALARM, context.getString(R.string.alarm_name));
			_streamNames.put(AudioManager.STREAM_NOTIFICATION, context.getString(R.string.notification_name));
		}

		final AppWidgetManager awm = AppWidgetManager.getInstance(context);
		final ComponentName nm = new ComponentName(context, VolumeWidgetProvider.class);
		final AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		final String action = intent.getAction();
		final int sender = intent.getIntExtra(context.getString(R.string.AWI_EXTRA), -1);
		Log.d(TAG, "Received intent " + action + " from " + String.valueOf(sender));

		int[] appWidgetIds = awm.getAppWidgetIds(nm);
		for(int awi : appWidgetIds) {
			if(action.equals(context.getString(R.string.VOLUME_CHANGED))) {
				onUpdate(context, awm, awm.getAppWidgetIds(nm));
			} else if(action.equals(context.getString(R.string.VOLUME_DOWN))) {
				if(sender == awi) {
					am.adjustStreamVolume(getStream(context, awi), AudioManager.ADJUST_LOWER, 0);
				}
			} else if(action.equals(context.getString(R.string.VOLUME_UP))) {
				if(sender == awi) {
					am.adjustStreamVolume(getStream(context, awi), AudioManager.ADJUST_RAISE, 0);
				}
			}
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

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.d(TAG, "Volume widget updating");

		int stream;
		for(int awi : appWidgetIds) {
			stream = getStream(context, awi);
			updateWidget(context, appWidgetManager, awi, stream);
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
							 int appWidgetId, int stream) {
		Log.d(TAG, "updateWidget appWidgetId=" + appWidgetId + " stream=" + String.valueOf(stream));

		if(stream < 0) {
			return;
		}

		AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		String name = _streamNames.get(stream);
		int volume = am.getStreamVolume(stream);
		int max = am.getStreamMaxVolume(stream);
		Log.d(TAG, "Volume is " + String.valueOf(volume) + " / " + String.valueOf(max));
		
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		views.setTextViewText(R.id.name, name);
		views.setProgressBar(R.id.volume_bar, max, volume, false);

		Intent upIntent = new Intent(context.getString(R.string.VOLUME_UP));
		upIntent.putExtra(context.getString(R.string.AWI_EXTRA), appWidgetId);
		Intent downIntent = new Intent(context.getString(R.string.VOLUME_DOWN));
		downIntent.putExtra(context.getString(R.string.AWI_EXTRA), appWidgetId);
		
		views.setOnClickPendingIntent(R.id.down_button,
									  PendingIntent.getBroadcast(context, appWidgetId, downIntent, 0));
		views.setOnClickPendingIntent(R.id.up_button,
									  PendingIntent.getBroadcast(context, appWidgetId, upIntent, 0));
		
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}
}


