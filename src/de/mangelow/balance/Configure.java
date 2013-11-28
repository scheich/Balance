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
import java.util.Calendar;
import java.util.Currency;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;

public class Configure extends PreferenceActivity {

	private final String TAG = "B";
	private final boolean D = true;

	private Context context;

	private AppWidgetHost aw_host;
	private int aw_id = AppWidgetManager.INVALID_APPWIDGET_ID;

	private LinearLayout ll, ll_b;
	private ListView lv_p;
	private Button b_ok, b_cancel;

	int restarted;
	int versionCode = -1;
	int refresh = REFRESH_DEFAULT;

	public static final String PREF_FILE = "Prefs";

	public static final String RESTARTED = "restarted";
	public static final int RESTARTED_DEFAULT = 0;

	public static final String BALANCE = "balance";
	public static final String BALANCE_DEFAULT = "?";

	public static final String BALANCEDATE = "balancedate";
	public static final String BALANCEDATE_DEFAULT = "";

	public static final String USSDCODE = "ussdcode";
	public static final String USSDCODE_DEFAULT = "*100#";

	public static final String REFRESH = "refresh";
	public static final int REFRESH_DEFAULT = 0;

	public static final String GETDECIMAL = "getdecimal";
	public static final boolean GETDECIMAL_DEFAULT = true;

	public static final String CURRENCYSYMBOL = "currencysymbol";
	public static final String CURRENCYSYMBOL_DEFAULT = Currency.getInstance(Locale.getDefault()).getSymbol();

	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);
		if(D)Log.d(TAG, "onCreate");

		this.getWindow().setLayout((int)(getWindowManager().getDefaultDisplay().getWidth()*0.9f),LinearLayout.LayoutParams.WRAP_CONTENT);

		context = getApplicationContext();		

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			aw_id = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
			if(D)Log.d(TAG, "appWidgetId: "+aw_id);
		}
		if (aw_id == AppWidgetManager.INVALID_APPWIDGET_ID) {
			if(D)Log.d(TAG, "INVALID_APPWIDGET_ID");
			finish();
		}

		//

		aw_host = new AppWidgetHost(context, 2037);
		aw_host.startListening();

		ll = new LinearLayout(context);
		ll.setOrientation(LinearLayout.VERTICAL);

		//

		ll_b = new LinearLayout(context);
		ll_b.setOrientation(LinearLayout.HORIZONTAL);
		ll_b.setGravity(Gravity.BOTTOM);

		b_ok = new Button(context);
		b_ok.setText(getString(android.R.string.ok));
		b_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if(restarted==versionCode) {
					Intent i = new Intent();
					i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, aw_id );
					i.setData(Uri.withAppendedPath(Uri.parse(AppWidgetManager.EXTRA_APPWIDGET_ID + "://widget/id/"),String.valueOf(aw_id)));
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					setResult(RESULT_OK, i);

					String ussdcode = loadStringPref(context, USSDCODE, USSDCODE_DEFAULT);
					ussdcode = ussdcode.replaceAll("#", "");

					if(refresh>REFRESH_DEFAULT) {
						String nextrefresh = setRTCAlarm(context, refresh);
						Toast.makeText(context, context.getResources().getString(R.string.nextrrefresh) + " " + nextrefresh, Toast.LENGTH_LONG).show();
					}

					startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + ussdcode + Uri.encode("#"))));

				}
				else {
					aw_host.deleteAppWidgetId(aw_id);
					setResult(RESULT_CANCELED);
				}

				finish();

			}
		});
		ll_b.addView(b_ok, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT,1));

		b_cancel = new Button(context);
		b_cancel.setText(getString(android.R.string.cancel));
		b_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				aw_host.deleteAppWidgetId(aw_id);
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		ll_b.addView(b_cancel, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT,1));

		//

		lv_p = new ListView(this); 
		lv_p.setId(android.R.id.list);

		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(context);
		root.bind(lv_p); 

		//

		restarted = loadIntPref(context, RESTARTED, RESTARTED_DEFAULT);
		try {
			versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		if(restarted==versionCode) {

			final EditTextPreference etp_ussd = new EditTextPreference(this);
			etp_ussd.getEditText().setInputType(InputType.TYPE_CLASS_PHONE);
			etp_ussd.setTitle(context.getResources().getString(R.string.p_ussdcode));
			etp_ussd.setSummary(USSDCODE_DEFAULT);
			etp_ussd.setDialogTitle(context.getResources().getString(R.string.p_currencysymbol_dialog));
			etp_ussd.setDefaultValue(USSDCODE_DEFAULT);
			etp_ussd.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				public boolean onPreferenceChange(Preference p, Object o) {

					String value = o.toString();

					if(value.length()==0) {
						b_ok.setEnabled(false);
						Toast.makeText(context, "You have to enter a ussd code!", Toast.LENGTH_LONG).show();
					}
					else {
						b_ok.setEnabled(true);
					}

					p.setSummary(value);
					saveStringPref(context, USSDCODE, value);

					return true;
				}
			});
			root.addPreference(etp_ussd);

			final String [] refresh_entries = context.getResources().getStringArray(R.array.refresh);
			int length_refresh_entries = refresh_entries.length;

			String [] refresh_values = new String[length_refresh_entries];
			for (int i = 0; i < length_refresh_entries; i++)refresh_values[i] = String.valueOf(i);

			ListPreference lp_refresh = new ListPreference(this);
			lp_refresh.setTitle(context.getResources().getString(R.string.p_refresh));
			lp_refresh.setEntries(refresh_entries);
			lp_refresh.setEntryValues(refresh_values);
			lp_refresh.setSummary(refresh_entries[refresh]);
			lp_refresh.setValue(String.valueOf(refresh));
			lp_refresh.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					final String summary = newValue.toString();
					ListPreference lp = (ListPreference) preference;	
					refresh = lp.findIndexOfValue(summary);

					lp.setSummary(refresh_entries[refresh]);
					saveIntPref(context, REFRESH, refresh);

					return true;
				}
			}); 
			root.addPreference(lp_refresh);

			//

			final CheckBoxPreference cbp_getdecimal = new CheckBoxPreference(context);
			final EditTextPreference etp_currencysymbol = new EditTextPreference(this);

			cbp_getdecimal.setTitle(context.getResources().getString(R.string.p_getdecimal));
			cbp_getdecimal.setSummary(context.getResources().getString(R.string.p_getdecimal_summary));
			cbp_getdecimal.setChecked(GETDECIMAL_DEFAULT);
			cbp_getdecimal.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				public boolean onPreferenceChange(Preference p, Object o) {
					boolean newvalue = Boolean.parseBoolean(o.toString());

					etp_currencysymbol.setEnabled(false);
					if(newvalue)etp_currencysymbol.setEnabled(true);

					saveBooleanPref(context, GETDECIMAL, newvalue);

					return true;
				}
			});
			root.addPreference(cbp_getdecimal);

			etp_currencysymbol.setTitle(context.getResources().getString(R.string.p_currencysymbol));
			etp_currencysymbol.setSummary(CURRENCYSYMBOL_DEFAULT);
			etp_currencysymbol.setDialogTitle(context.getResources().getString(R.string.p_currencysymbol_dialog));
			etp_currencysymbol.setText(CURRENCYSYMBOL_DEFAULT);
			etp_currencysymbol.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				public boolean onPreferenceChange(Preference p, Object o) {

					String value = o.toString();
					p.setSummary(value);
					saveStringPref(context, CURRENCYSYMBOL, value);

					return true;
				}
			});
			root.addPreference(etp_currencysymbol);
		}
		else {

			b_cancel.setVisibility(View.GONE);

			Preference p_restart = new Preference(context);
			p_restart.setTitle(context.getResources().getString(R.string.pleaserestart));
			p_restart.setSummary(context.getResources().getString(R.string.pleaserestart_text));
			root.addPreference(p_restart);
		}

		//

		lv_p.setAdapter(root.getRootAdapter()); 

		ll.addView(lv_p, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT,1));
		ll.addView(ll_b, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT,0.1f));

		setContentView(ll,new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		getListView().setBackgroundColor(Color.BLACK);
		setPreferenceScreen(root);
	}

	//

	@SuppressWarnings("deprecation")
	public static String setRTCAlarm(Context context, int updateRate) {

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		
		switch (updateRate) {

		case 1: // Every hour
			c.add(Calendar.HOUR_OF_DAY,1);
			break;
		case 2: // Every 6 hours
			c.add(Calendar.HOUR_OF_DAY,6);
			break;
		case 3: // Every 12 hours
			c.add(Calendar.HOUR_OF_DAY,12);
			break;
		case 4: // Every day
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.add(Calendar.DAY_OF_YEAR,1);
			break;
		case 5: // Every Monday		
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			int weekday = c.get(Calendar.DAY_OF_WEEK);  
			int days = 7;
			if (weekday != Calendar.MONDAY)	{
				days = (Calendar.SATURDAY - weekday + 2) % 7;
			}
			c.add(Calendar.DAY_OF_YEAR, days);

			break;
		case 6: // Every first day of the month
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.DAY_OF_MONTH, 1);
			c.add(Calendar.MONTH, 1);
			break;
		}

		Intent active = new Intent(context, Receiver.class);
		active.setAction(Receiver.ACTION_WIDGET_REFRESH);
		PendingIntent sender = PendingIntent.getBroadcast(context, 784327894, active, PendingIntent.FLAG_CANCEL_CURRENT);

		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		
		if(updateRate==0) {
			am.cancel(sender);
			return "Cancel RTC";
		}
		else {
			am.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), sender);
			return c.getTime().toLocaleString();

		}
	}
	
	//

	public static void saveBooleanPref(Context context,String name, boolean value) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(PREF_FILE, 0).edit();
		prefs.putBoolean(name, value);
		prefs.commit();
	}
	public static Boolean loadBooleanPref(Context context, String name, boolean defaultvalue) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_FILE, 0);
		boolean bpref = prefs.getBoolean(name, defaultvalue);
		return bpref;
	}	
	public static void saveIntPref(Context context,String name, int value) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(PREF_FILE, 0).edit();
		prefs.putInt(name, value);
		prefs.commit();
	}
	public static int loadIntPref(Context context, String name, int defaultvalue) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_FILE, 0);
		int pref = prefs.getInt(name, defaultvalue);
		return pref;
	}
	public static void saveStringPref(Context context,String name, String value) {
		SharedPreferences.Editor prefs = context.getSharedPreferences(PREF_FILE, 0).edit();
		prefs.putString(name, value);
		prefs.commit();
	}
	public static String loadStringPref(Context context, String name, String defaultvalue) {
		SharedPreferences prefs = context.getSharedPreferences(PREF_FILE, 0);
		String pref = prefs.getString(name, defaultvalue);
		return pref;
	}
}
