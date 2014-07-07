package com.mick88.alt.tab;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;

public class AltTabWidgetProvider extends AppWidgetProvider
{
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
		      int[] appWidgetIds)
	{
		Log.d("AltTab", "onUpdate called for widget");
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
}
