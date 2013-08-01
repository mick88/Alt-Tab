package com.mick88.alt.tab;

import android.app.ActivityManager.RunningTaskInfo;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

public class TaskData
{
	String packageName;
	PendingIntent pendingIntent;
	Bitmap bitmap=null;
	BitmapDrawable icon=null;
	boolean valid=true;
	
	public TaskData(RunningTaskInfo taskInfo, TaskManager taskManager)
	{
		ComponentName component = taskInfo.baseActivity;
		this.packageName = component.getPackageName();
	
		try
		{
			Intent intent = taskManager.packageManager.getLaunchIntentForPackage(packageName);
			//intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			this.pendingIntent = PendingIntent.getActivity(taskManager.context, 0, intent, 0);
		}
		catch (NullPointerException e)
		{
			valid=false;
		}
		
		try
		{
			this.icon = (BitmapDrawable) taskManager.packageManager.getActivityIcon(component);
			this.bitmap = icon.getBitmap();
		}
		catch (NameNotFoundException e)
		{
			valid=false;
		}
		
	}
	
	public boolean isValid()
	{
		return valid;
	}
	
	public String getPackageName()
	{
		return packageName;
	}
	
	public Bitmap getBitmap()
	{
		return bitmap;
	}
	
	public PendingIntent getPendingIntent()
	{
		return pendingIntent;
	}
	
	@Override
	public String toString()
	{
		return packageName;
	}
	
	public boolean packageNameEquals(String packageName)
	{
		return this.packageName.equals(packageName);
	}
	
	@Override
	public boolean equals(Object o)
	{		
		return packageName.equals(((TaskData)o).packageName);
	}
}
