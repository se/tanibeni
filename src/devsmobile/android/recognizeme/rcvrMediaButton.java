package devsmobile.android.recognizeme;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

public class rcvrMediaButton extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		String intentAction = intent.getAction();

		if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
			return;
		}

		KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

		if (event == null) {
			return;
		}

		int action = event.getAction();
		if (action == KeyEvent.ACTION_DOWN) {
			Intent i = new Intent(context, actMain.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			Bundle b = new Bundle();
			b.putBoolean("fromReceiver", true);
			
			i.putExtras(b);

			context.startActivity(i);
		}

		abortBroadcast();
	}
}
