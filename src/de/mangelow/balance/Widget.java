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
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

public class Widget extends AppWidgetProvider {

	private static final String TAG = "B";
	public static final boolean D = true;	

	@Override
	public void onUpdate(Context context, AppWidgetManager aw_manager, int[] aw_ids) {
		if(D)Log.d(TAG, "onUpdate");

		final int N = aw_ids.length;
		for (int i=0; i<N; i++) {

			int aw_id = aw_ids[i];

			int restart = Configure.loadIntPref(context, Configure.RESTARTED, Configure.RESTARTED_DEFAULT);
			int versionCode = -1;
			try {
				versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
			} catch (NameNotFoundException e1) {}

			String balance = Configure.loadStringPref(context, Configure.BALANCE, Configure.BALANCE_DEFAULT);

			String balance_date = Configure.loadStringPref(context, Configure.BALANCEDATE, Configure.BALANCEDATE_DEFAULT);
			String ussdcode = Configure.loadStringPref(context, Configure.USSDCODE, Configure.USSDCODE_DEFAULT);
			ussdcode = ussdcode.replaceAll("#", "");

			boolean getdecimal = Configure.loadBooleanPref(context, Configure.GETDECIMAL, Configure.GETDECIMAL_DEFAULT);
			if(getdecimal) {
				String currencysymbol = Configure.loadStringPref(context, Configure.CURRENCYSYMBOL, Configure.CURRENCYSYMBOL_DEFAULT);
				try {
					balance = String.valueOf(Float.parseFloat(String
							.valueOf(balance).replaceAll(",", ".")
							.replaceAll(".*?([\\d.]+).*", "$1"))) + currencysymbol;
				} catch (Exception e) {}
			}

			Intent i_call = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + ussdcode + Uri.encode("#")));			
			PendingIntent pi = PendingIntent.getActivity(context, 0, i_call, 0);

			RemoteViews views = new RemoteViews(context.getPackageName(),R.layout.widget);

			if(restart==versionCode) {
				
				views.setTextViewText(R.id.balance, balance);
				views.setTextViewText(R.id.date, balance_date);
				views.setOnClickPendingIntent(R.id.widget, pi);
				
			}
			else {
				
				views.setTextViewText(R.id.balance, context.getResources().getString(R.string.pleaserestart));
				views.setTextViewText(R.id.date, context.getResources().getString(R.string.pleaserestart_text));
				
			}
			
			aw_manager.updateAppWidget(aw_id, views);

		}
	}
	@Override
	public void onEnabled(Context context) {
		if(D)Log.d(TAG, "onEnabled");

		PackageManager pm = context.getPackageManager();
		pm.setComponentEnabledSetting(
				new ComponentName("de.mangelow.balance", ".Widget"),
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
				PackageManager.DONT_KILL_APP);
	}
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {

		SharedPreferences.Editor prefs = context.getSharedPreferences(Configure.PREF_FILE, 0).edit();
		prefs.remove(Configure.BALANCE);
		prefs.remove(Configure.BALANCEDATE);
		prefs.remove(Configure.USSDCODE);
		prefs.remove(Configure.GETDECIMAL);
		prefs.remove(Configure.CURRENCYSYMBOL);
		prefs.commit();
		
		Configure.setRTCAlarm(context, 0);
	}
	@Override
	public void onDisabled(Context context) {
		if(D)Log.d(TAG, "onDisabled");

		PackageManager pm = context.getPackageManager();
		pm.setComponentEnabledSetting(
				new ComponentName("de.mangelow.balance", ".Widget"),
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
				PackageManager.DONT_KILL_APP);
	}
}