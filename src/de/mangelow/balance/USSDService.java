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
import java.util.Date;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.android.internal.telephony.IExtendedNetworkService;

public class USSDService extends Service {

	private Context context;
	
	private final IExtendedNetworkService.Stub mBinder = new IExtendedNetworkService.Stub () {
		public void clearMmiString() throws RemoteException {}
		public void setMmiString(String number) throws RemoteException {}
		public CharSequence getMmiRunningText() throws RemoteException {
			return context.getResources().getString(R.string.balanceisupdating);		
		}
		public CharSequence getUserMessage(CharSequence cs) throws RemoteException {

			String text = String.valueOf(cs);
			Configure.saveStringPref(context, Configure.BALANCE, text);
			Configure.saveStringPref(context, Configure.BALANCEDATE, new Date(System.currentTimeMillis()).toLocaleString());

			AppWidgetManager aw_manager = AppWidgetManager.getInstance(context);
		    Intent i_update = new Intent(context, Widget.class);
		    i_update.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		    i_update.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,aw_manager.getAppWidgetIds(new ComponentName(context, Widget.class)));
		    context.sendBroadcast(i_update);
		    
			return null;
		}
	};
	@Override
	public IBinder onBind(Intent intent) {
		
		context = getApplicationContext();
		
		return mBinder;
	}	
}
