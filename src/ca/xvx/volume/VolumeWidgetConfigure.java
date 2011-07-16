package ca.xvx.volume;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RemoteViews;
import android.widget.Toast;

public class VolumeWidgetConfigure extends Activity {
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setResult(RESULT_CANCELED);
		setContentView(R.layout.configure_layout);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		final int appWidgetId;
		if(extras != null) {
			appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
										AppWidgetManager.INVALID_APPWIDGET_ID);
		} else {
			finish();
			return;
		}

		final String PREFS_NAME = this.getString(R.string.prefs_base_name) + String.valueOf(appWidgetId);
		final RadioGroup streamSel = (RadioGroup)findViewById(R.id.stream_selection);
		final Button add = (Button)findViewById(R.id.ok_button);

		add.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final Context context = VolumeWidgetConfigure.this;
					
					int selected = streamSel.getCheckedRadioButtonId();
					int streamid;
					String name;
					switch(selected) {
					case R.id.stream_ringtone:
						streamid = AudioManager.STREAM_RING;
						name = context.getString(R.string.ringtone_name);
						break;
					case R.id.stream_media:
						streamid = AudioManager.STREAM_MUSIC;
						name = context.getString(R.string.media_name);
						break;
					case R.id.stream_alarm:
						streamid = AudioManager.STREAM_ALARM;
						name = context.getString(R.string.alarm_name);
						break;
					case R.id.stream_notification:
						streamid = AudioManager.STREAM_NOTIFICATION;
						name = context.getString(R.string.notification_name);
						break;
					default:
						streamid = -1;
						name = null;
						break;
					}

					if(streamid == -1) {
						Toast.makeText(context, context.getString(R.string.ERR_no_selection), Toast.LENGTH_LONG).show();
						return;
					}

					SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
					prefs.putInt("stream", streamid);
					prefs.commit();

					AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
					int volume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
					int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
					RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
					views.setTextViewText(R.id.name, name);
					views.setProgressBar(R.id.volume_bar, max, volume, false);
					views.setOnClickPendingIntent(R.id.down_button,
												  PendingIntent.getBroadcast(context, 0,
																			 new Intent(context.getString(R.string.VOLUME_DOWN)), 0));
					views.setOnClickPendingIntent(R.id.up_button,
												  PendingIntent.getBroadcast(context, 0,
																			 new Intent(context.getString(R.string.VOLUME_UP)), 0));

					AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
					appWidgetManager.updateAppWidget(appWidgetId, views);

					Intent resultValue = new Intent();
					resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
					setResult(RESULT_OK, resultValue);
					finish();
				}
			});
	}
}