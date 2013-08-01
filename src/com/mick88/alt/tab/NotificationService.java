package com.mick88.alt.tab;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

//Service that runs in the background
//updates notification and widgets
public class NotificationService extends Service
{
	int ID_NOTIFICATION = 1;
	int maxShownApps = 20;
	int notificationIcon = R.drawable.ic_notification;
	int timerFrequency = 5000;
	TaskManager taskManager;
	
	RemoteViews lastRunningApps=null;
	RemoteViews widgetRunningApps=null;
	List <RunningTaskInfo> appList=null;
	String notificationService = Context.NOTIFICATION_SERVICE;
	NotificationManager notificationManager;
	Notification notification=null;
	Timer timer;
	KeyguardManager keyguardManager=null;
	String launcherPackage="";
	String thisPackageName;
	String lastTopTaskPackage="";
	
	void detectLauncher()
	{
		final Intent i = new Intent(Intent.ACTION_MAIN);
		i.addCategory(Intent.CATEGORY_HOME);
		final ResolveInfo resolve = getPackageManager().resolveActivity(i, 0);
		if (resolve.activityInfo != null)
		{
			if (resolve.activityInfo.packageName.equals("android"/*no default launcher*/) == false )
			{
				launcherPackage = resolve.activityInfo.packageName;
				Log.i("LocalService", "Launcher detected: "+launcherPackage);
				taskManager.ignorePackage(launcherPackage);
			}
		}
	}
	
	public RemoteViews getNotificationView(int number/*number of last apps to be shown*/)
	{		
		ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
		RunningTaskInfo task = activityManager.getRunningTasks(1).get(0);
		
		String topPackage = task.baseActivity.getPackageName();
		
		if (lastTopTaskPackage.equals(topPackage) == false) //checking if app on top is different than last time
		{
			lastTopTaskPackage = topPackage;
		}
		else if (lastRunningApps != null) //nothing changed, return last running apps
		{
			return lastRunningApps;
		}
		
		/*add current task to the list*/
		if (taskManager.addCurrentTask(task) == true || lastRunningApps == null)
		{
			/*reconstruct RemoteViews*/			
			int n=0;
			TaskData[] tasks = taskManager.getTaskArray();
			if (number > tasks.length) number = tasks.length;
			
			RemoteViews RunningApps = new RemoteViews(thisPackageName, R.layout.layout_notification);
			RemoteViews widget = new RemoteViews(thisPackageName, R.layout.layout_widget_grid);
			
			/*add remote views to the layout*/
			for (int i=0; i < number; i++)
			{
				TaskData taskData = tasks[tasks.length-1-i];
				if ((i==0 && taskData.packageNameEquals(topPackage)) || taskData.valid == false) continue; //hides top app from list
				RemoteViews childView = new RemoteViews(thisPackageName, R.layout.notification_element);	
				
				childView.setBitmap(R.id.imageButton2, "setImageBitmap", taskData.getBitmap());
				childView.setOnClickPendingIntent(R.id.imageButton2, taskData.getPendingIntent());
	
				RunningApps.addView(R.id.parent_frame, childView);
				widget.addView(R.id.parent_grid, childView);
				
				n++;
				if (n == number) break;
			}
			
			if (n==0) //no views were actually added
			{
				widgetRunningApps = new RemoteViews(thisPackageName, R.layout.widget_layout);
				return new RemoteViews(thisPackageName, R.layout.notification_element_empty);
			}
			widgetRunningApps=widget;
			return RunningApps;
		}
		else return lastRunningApps;
	}
	
	@SuppressWarnings("deprecation")
	public void createNotification()
	{
		notification = new Notification(notificationIcon, null, Long.MAX_VALUE);
		notification.contentView = getNotificationView(TaskManager.MAX_ELEMENTS);		
				
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;

		startForeground(ID_NOTIFICATION, notification);
		
		Log.d("LocalService", "Notification displayed");
	}
	
	void updateWidget(RemoteViews remoteViews)
	{
		Context context = getApplicationContext();
		AppWidgetManager widgetMan = AppWidgetManager.getInstance(context);
		ComponentName thisWidget = new ComponentName(context, AltTabWidgetProvider.class);
		
		int [] widgetIds = widgetMan.getAppWidgetIds(thisWidget);
		
		
		widgetMan.updateAppWidget(widgetIds, null);
		widgetMan.updateAppWidget(widgetIds, remoteViews);
	}
	
	void updateNotification()
	{		
		if (notification == null) createNotification();	
		else 
		{
			notification.contentView = getNotificationView(TaskManager.MAX_ELEMENTS);
//			notification.contentView = getRunningAppsTaskMan(maxShownApps);
			
			if (notification.contentView.equals(lastRunningApps) == false) //list is different
			{				
//				notificationManager.cancel(ID_NOTIFICATION);
				stopForeground(true);
				startForeground(ID_NOTIFICATION, notification);
//				notificationManager.notify(ID_NOTIFICATION, notification);

				lastRunningApps = notification.contentView;
				updateWidget(widgetRunningApps);
			}
		}
	}

	
	@Override
	public void onCreate()
	{
		super.onCreate();
		//Log.d("NotificationService", "Service created");
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
	
	class UpdateNotification extends TimerTask
	{
		@Override
		public void run()
		{
			if (keyguardManager.inKeyguardRestrictedInputMode() == false)
			{
				updateNotification();
			}
		}
		
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
        Log.d("LocalService", "Alt Tab service started");  
        
        /*Initialize context*/
        Context context = getApplicationContext();
        thisPackageName = getPackageName();
        
        /*Initialize task manager*/
        taskManager = new TaskManager(context.getPackageManager(), context);
        detectLauncher(); //detects launcher and adds it to ignored packages
        taskManager.initalize(((ActivityManager)this.getSystemService(ACTIVITY_SERVICE)).getRunningTasks(TaskManager.MAX_ELEMENTS));
        
        /*Initialize notifications*/
        notificationManager = (NotificationManager) getSystemService(notificationService);
        updateNotification();
        
        /*Start timer*/
        timer = new Timer();
        keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        timer.scheduleAtFixedRate(new UpdateNotification(), timerFrequency, timerFrequency);        
        return START_STICKY;
    }

    @Override
    public void onDestroy() 
    {
    	super.onDestroy();
    	timer.cancel();
    	notificationManager.cancel(ID_NOTIFICATION);
    	Log.d("LocalService", "Service destroyed.");
    }

}
