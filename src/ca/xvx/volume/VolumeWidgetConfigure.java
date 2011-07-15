package ca.xvx.volume;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class VolumeWidgetConfigure extends Activity {
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setResult(RESULT_CANCELED);
		setContentView(R.layout.configure_layout);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
		if(extras != null) {
			appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
										AppWidgetManager.INVALID_APPWIDGET_ID);
		}
		if(appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
			return;
		}
	}
}