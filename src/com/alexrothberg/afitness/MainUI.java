package com.alexrothberg.afitness;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class MainUI extends TabActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.afitness_main);
		Resources res = getResources();
		
		TabHost tabHost = getTabHost();
		Bundle extras = new Bundle();
		tabHost.addTab(tabHost.newTabSpec("exercises").setIndicator("Exercises", res.getDrawable(R.drawable.dumbbell)).setContent(new Intent(this, MuscleGroupChooser.class)));
		tabHost.addTab(tabHost.newTabSpec("my_workouts").setIndicator("My Workouts", res.getDrawable(R.drawable.pencil)).setContent(new Intent(this, MyWorkouts.class)));
		//tabHost.addTab(tabHost.newTabSpec("routines").setIndicator("Routines").setContent(new Intent(this, RecordExercise.class)));
		//tabHost.addTab(tabHost.newTabSpec("logs").setIndicator("Logs").setContent(new Intent(this, MyWorkouts.class)));
	}	
}
