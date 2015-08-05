package com.alexrothberg.afitness;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

public class MainUI extends TabActivity {
	private WeakReference<LogActivity> logActivityReference = null;
	
	private static final String TAB_NAME_LOGS = "logs";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.afitness_main);
		Resources res = getResources();
		
		TabHost tabHost = getTabHost();
//		Bundle extras = new Bundle();
		tabHost.addTab(tabHost.newTabSpec("exercises").setIndicator("Exercises", res.getDrawable(R.drawable.dumbbell)).setContent(new Intent(this, MuscleGroupChooser.class)));
		tabHost.addTab(tabHost.newTabSpec("my_workouts").setIndicator("My Workouts", res.getDrawable(R.drawable.pencil)).setContent(new Intent(this, MyWorkouts.class)));
		//tabHost.addTab(tabHost.newTabSpec("routines").setIndicator("Routines").setContent(new Intent(this, RecordExercise.class)));
		tabHost.addTab(tabHost.newTabSpec(TAB_NAME_LOGS).setIndicator("Logs", res.getDrawable(R.drawable.history)).setContent(new Intent(this, LogActivity.class)));
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			
			@Override
			public void onTabChanged(String tabId) {
				if(tabId.equals(TAB_NAME_LOGS)){
					if(logActivityReference == null || logActivityReference.get() == null){
						throw new RuntimeException("logActivityReference not in use");
					}else{
						logActivityReference.get().goDateMode();
					}
				}
				
			}
		});
	}
	
	public void registerLogActivity(final LogActivity logActivity){
		if(logActivityReference != null && logActivityReference.get() != null){
			throw new RuntimeException("logActivityReference in use");
		}
		
		logActivityReference = new WeakReference<LogActivity>(logActivity);		
	}
}
