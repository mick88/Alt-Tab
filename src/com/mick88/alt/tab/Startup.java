package com.mick88.alt.tab;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Startup extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent arg1)
	{
		// TODO Auto-generated method stub
		context.startService(new Intent(context, NotificationService.class));
	}

}
