package com.mick88.alt.tab;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Settings extends Activity 
{
	void doStartService()
	{
		Log.i("NotificationService", "Starting service...");
		startService(new Intent(this, NotificationService.class));
	}
	
	void doStopService()
	{
		stopService(new Intent(this, NotificationService.class));
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        Button enableBtn = (Button) findViewById(R.id.enableBtn);
        Button disableBtn = (Button) findViewById(R.id.disableBtn);
        
        enableBtn.setOnClickListener(new OnClickListener()
		{
			
			public void onClick(View v)
			{
				doStartService();
				
			}
		});
        
        
        disableBtn.setOnClickListener(new OnClickListener()
		{
			
			public void onClick(View v)
			{
				doStopService();				
			}
		});
        
        doStartService();
    }
	
	@Override
	public void onPause()
	{
		super.onPause();
		//doStopService();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.activity_settings, menu);
        return false;
    }
}
