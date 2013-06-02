package devsmobile.android.recognizeme;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

/**
 * Created by Raider on 6/1/13.
 */
public class wdgtRecognizeMe extends AppWidgetProvider {

    private static final String btnClickEventName = "wdgtibtnRecognizeMe";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        remoteViews.setOnClickPendingIntent(R.id.btnRecognizeMe, getPendingSelfIntent(context, btnClickEventName));

        pushWidgetUpdate(context, remoteViews);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (btnClickEventName.equals(intent.getAction())) {
            Intent i = new Intent(context, actMain.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            Bundle b = new Bundle();
            b.putBoolean("fromReceiver", true);

            i.putExtras(b);

            context.startActivity(i);
        }
    }

    public static void pushWidgetUpdate(Context context, RemoteViews remoteViews) {
        ComponentName myWidget = new ComponentName(context, wdgtRecognizeMe.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(myWidget, remoteViews);
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }
}
