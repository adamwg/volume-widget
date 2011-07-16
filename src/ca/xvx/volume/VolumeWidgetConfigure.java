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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RemoteViews;
import android.widget.Toast;

public class VolumeWidgetConfigure extends Activity {
	private static final String TAG = "VolumeWidgetConfigure";
	
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

		Log.d(TAG, "Configuring app widget " + String.valueOf(appWidgetId));
		final String PREFS_NAME = this.getString(R.string.prefs_base_name) + String.valueOf(appWidgetId);
		final RadioGroup streamSel = (RadioGroup)findViewById(R.id.stream_selection);
		final Button add = (Button)findViewById(R.id.ok_button);

		add.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final Context context = VolumeWidgetConfigure.this;
					
					int selected = streamSel.getCheckedRadioButtonId();
					int streamid;
					switch(selected) {
					case R.id.stream_ringtone:
						streamid = AudioManager.STREAM_RING;
						break;
					case R.id.stream_media:
						streamid = AudioManager.STREAM_MUSIC;
						break;
					case R.id.stream_alarm:
						streamid = AudioManager.STREAM_ALARM;
						break;
					case R.id.stream_notification:
						streamid = AudioManager.STREAM_NOTIFICATION;
						break;
					default:
						streamid = -1;
						break;
					}

					if(streamid == -1) {
						Toast.makeText(context, context.getString(R.string.ERR_no_selection), Toast.LENGTH_LONG).show();
						return;
					}
					Log.d(TAG, "Widget " + String.valueOf(appWidgetId) + " stream = " + String.valueOf(streamid));

					SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME,
																				  Context.MODE_WORLD_READABLE).edit();
					prefs.putInt(context.getString(R.string.STREAM_PREF), streamid);
					prefs.commit();

					VolumeWidgetProvider.updateWidget(context, AppWidgetManager.getInstance(context),
													  appWidgetId, streamid);
					
					Intent resultValue = new Intent();
					resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
					setResult(RESULT_OK, resultValue);
					finish();
				}
			});
	}
}