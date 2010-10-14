package com.alexrothberg.afitness;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.alexrothberg.afitness.DbAdapter.Activities;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class DebugActivity extends Activity implements OnClickListener {
	private EditText txt;
	private Button saveWorkouts_btn;
	
	DbAdapter dbAdapter;
	
	private Map<Long, String> nameCache = new HashMap<Long, String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.debug);
		
		saveWorkouts_btn = (Button)findViewById(R.id.debug_save_workouts_btn);
		txt = (EditText)findViewById(R.id.debug_txt);
		
		saveWorkouts_btn.setOnClickListener(this);
		
		dbAdapter = new DbAdapter(this);
		dbAdapter.open();
	}
	
	@Override
	protected void onDestroy() {
		dbAdapter.close();
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		Cursor allActivities = dbAdapter.getAllActivities();
		startManagingCursor(allActivities);
		
		long record_date_last = 0L;
		int record_date_pos = allActivities.getColumnIndex(Activities.KEY_RECORD_DATE);
		
		long exercise_last = 0L;
		int exercise_pos = allActivities.getColumnIndex(Activities.KEY_EXERCISE);
		
		float weight_last = -1f;
		int weight_pos = allActivities.getColumnIndex(Activities.KEY_WEIGHT);
		
		int reps_pos = allActivities.getColumnIndex(Activities.KEY_REPS);
		
		boolean first = true;
		StringBuilder builder = new StringBuilder();
		while(allActivities.moveToNext()){
			boolean suppress_nl = false;

			long record_date_this = allActivities.getLong(record_date_pos);
			
			if(record_date_last != record_date_this){
				if(!first){
					builder.append('\n');					
				}else{
					first = false;
				}
				builder.append(formatDate(record_date_this));
				builder.append('\n');
				suppress_nl = true;
				exercise_last = 0L;
				record_date_last = record_date_this;
			}
			
			long exercise_this = allActivities.getLong(exercise_pos);
			if(exercise_last != exercise_this){
				if(!suppress_nl)
					builder.append('\n');
				builder.append(getExerciseName(exercise_this));
				builder.append(": ");
				exercise_last = exercise_this;
				weight_last = -1f;
			}
			
			float weight_this = allActivities.getFloat(weight_pos);
			if(weight_this != weight_last){
				builder.append(Float.toString(weight_this));
				builder.append("lbs x ");
				weight_last = weight_this;
			}
			
			builder.append(reps_pos);
			builder.append(' ');
		}
		txt.setText(builder);
	}
	
	private String getExerciseName(long exercise_id){
		String cacheVal = nameCache.get(exercise_id);
		if (cacheVal != null){
			return cacheVal;
		}else{
			String val = dbAdapter.getExerciseName(exercise_id);
			nameCache.put(exercise_id, val);
			return val;
		}
		
	}
	
	private String formatDate(long dateLong){
		return Utilities.getDateString(dateLong, this);
	}
	
}
