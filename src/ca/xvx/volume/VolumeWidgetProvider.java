package ca.xvx.volume;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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

	private static final String VOLUME_DOWN = "ca.xvx.VOLUME_DOWN";
	private static final String VOLUME_UP = "ca.xvx.VOLUME_UP";

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
		} else if(action.equals(VOLUME_DOWN)) {
			am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, 0);
		} else if(action.equals(VOLUME_UP)) {
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

		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
		views.setProgressBar(R.id.volume_bar, max, volume, false);
		views.setOnClickPendingIntent(R.id.down_button, PendingIntent.getBroadcast(context, 0, new Intent(VOLUME_DOWN), 0));
		views.setOnClickPendingIntent(R.id.up_button, PendingIntent.getBroadcast(context, 0, new Intent(VOLUME_UP), 0));
		
		appWidgetManager.updateAppWidget(appWidgetId, views);
	}
}


