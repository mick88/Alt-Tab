package com.mick88.alt.tab;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

public class TaskManager
{
	public PackageManager packageManager;
	public Context context;
	
	public final static int MAX_ELEMENTS = 13;//15 
	public final static int QUEUE_CAPACITY = MAX_ELEMENTS + 10;
	public ArrayList<TaskData> tasks = new ArrayList<TaskData>();
	
	ArrayList<String> ignoredPackages = new ArrayList<String>();
	
	public TaskManager(PackageManager packageManager, Context context)
	{
		this.packageManager = packageManager;
		this.context = context;
	}
	
	public void ignorePackage(String packageName)
	{
		if (isPackageIgnored(packageName) == false) ignoredPackages.add(packageName);
		Log.d("AltTabbService", "Package "+packageName+" will be ignored.");
	}
	
	boolean isPackageIgnored(String packageName)
	{
		return ignoredPackages.contains(packageName);
	}
	
	public TaskData getTask(String packageName)
	{
		try
		{
			for(TaskData task : tasks)
			{
				if (task.packageNameEquals(packageName)) return task;
			}
		}
		catch (java.util.ConcurrentModificationException e)
		{
			Log.e("AltTab", "Something's gone wrong while looking for package "+packageName+". Collection was modified!");
			Log.d("AltTab", "AltTab saves the day by using a 'for' loop");
			
			for (int i=0; i < tasks.size(); i++)
			{
				if (tasks.get(i).packageNameEquals(packageName)) return tasks.get(i);
			}
		}
		return null;
	}
	
	boolean isTaskOnTop(TaskData task)
	{
		return (tasks.indexOf(task) == tasks.size()-1);
	}
	
	public TaskData getTopTask()
	{
		int size=tasks.size();
		if (size == 0) return null;
		return tasks.get(size-1);
	}
	
	public TaskData[] getTaskArray()
	{
		return tasks.toArray(new TaskData[tasks.size()]);
	}
	
	public TaskData[] getTasks(int number)
	{
		int size = tasks.size();
		TaskData [] result = new TaskData[size];
		
		for (int i=0; i<number; i++)
		{
			result[i] = tasks.get(size-1-i);
		}
		return result;
	}
	
	public void initalize(List<RunningTaskInfo> runningTasks)
	{
		for (int i=runningTasks.size()-1; i >= 0; i--)
		{
			addCurrentTask(runningTasks.get(i));
		}
	}
	
	void trimList()
	{
		while(tasks.size() >= QUEUE_CAPACITY) tasks.remove(0);
	}
	
	public boolean addCurrentTask(RunningTaskInfo taskInfo)
	{
		/*Should return false if there was NO change*/
		
		String packageName =  taskInfo.baseActivity.getPackageName();
	
		TaskData task = getTask(packageName);		
		
		if (task == null)
		{
			task = new TaskData(taskInfo, this);
			tasks.add(task);
			trimList();
		}
		else
		{
			if (isTaskOnTop(task)) return false;
			else 
			{
				/*move to top*/
				tasks.remove(task);
				tasks.add(task);
			}
		}
		return true;
	}
}
