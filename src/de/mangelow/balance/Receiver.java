package de.mangelow.balance;
/***
*
* Licensed under the Apache License, Version 2.0 (the "License"); you may
* not use this file except in compliance with the License. You may obtain
* a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.PowerManager;

public class Receiver extends BroadcastReceiver {

	public static String ACTION_WIDGET_REFRESH = "ACTION_WIDGET_REFRESH";

	@Override
	public void onReceive(Context context, Intent intent) {

		String action = intent.getAction();
		int refresh = Configure.loadIntPref(context, Configure.REFRESH, Configure.REFRESH_DEFAULT);

		if(action.equals(Intent.ACTION_BOOT_COMPLETED)) {
			context.startService(new Intent(context, USSDService.class));
			try {
				int versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
				Configure.saveIntPref(context, Configure.RESTARTED, versionCode);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			
			if(refresh>0)Configure.setRTCAlarm(context, refresh);
		}

		if(action.equals(ACTION_WIDGET_REFRESH)) {

			if(((PowerManager) context.getSystemService(Context.POWER_SERVICE)).isScreenOn()) {
				Configure.setRTCAlarm(context, 30);
			}
			else {

				String ussdcode = Configure.loadStringPref(context, Configure.USSDCODE, Configure.USSDCODE_DEFAULT);
				ussdcode = ussdcode.replaceAll("#", "");

				Intent i = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + ussdcode + Uri.encode("#")));
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(i);

				if(refresh>0)Configure.setRTCAlarm(context, refresh);
			}
		}
	}
}
