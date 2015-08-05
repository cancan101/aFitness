package com.alexrothberg.afitness;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.alexrothberg.afitness.DbAdapter.Activities;
import com.alexrothberg.afitness.DbAdapter.ExerciseMuscleGroups;
import com.alexrothberg.afitness.DbAdapter.ExerciseMuscles;

public class DebugActivity extends Activity implements OnClickListener {
	private EditText txt;
	private Button saveWorkouts_btn, dupeMG_btn, dupeM_btn, kill_dupeMG_btn, kill_dupeM_btn, bad_mg_btn, bad_m_btn;
	
	DbAdapter dbAdapter;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.debug);
		
		saveWorkouts_btn = (Button)findViewById(R.id.debug_save_workouts_btn);
		dupeMG_btn = (Button)findViewById(R.id.dupe_mg_btn);
		dupeM_btn = (Button)findViewById(R.id.dupe_m_btn);
		
		kill_dupeMG_btn = (Button)findViewById(R.id.kill_dupe_mg_btn);
		kill_dupeM_btn = (Button)findViewById(R.id.kill_dupe_m_btn);
		bad_mg_btn= (Button)findViewById(R.id.bad_mg_btn);
		bad_m_btn= (Button)findViewById(R.id.bad_m_btn);
		
		txt = (EditText)findViewById(R.id.debug_txt);
		
		saveWorkouts_btn.setOnClickListener(this);
		dupeMG_btn.setOnClickListener(this);
		dupeM_btn.setOnClickListener(this);
		
		kill_dupeMG_btn.setOnClickListener(this);
		kill_dupeM_btn.setOnClickListener(this);
		bad_mg_btn.setOnClickListener(this);
		bad_m_btn.setOnClickListener(this);
		
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
		if(v == saveWorkouts_btn){
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
		}else if (v == dupeMG_btn){
			Cursor allDupes = dbAdapter.getMgDupes();
			startManagingCursor(allDupes);
			
			int kill_pos = allDupes.getColumnIndex(ExerciseMuscleGroups._ID);			
			int exercise_pos = allDupes.getColumnIndex(ExerciseMuscleGroups.KEY_EXERCISE);
			int mg_pos = allDupes.getColumnIndex(ExerciseMuscleGroups.KEY_MUSCLE_GROUP);
			int count_pos = allDupes.getColumnIndex("COUNT");

			
			StringBuilder builder = new StringBuilder();
			while(allDupes.moveToNext()){
				builder.append(allDupes.getLong(exercise_pos));
				builder.append(" ");
				builder.append(allDupes.getLong(mg_pos));
				builder.append(" ");
				builder.append(allDupes.getInt(count_pos));
				builder.append(" (");
				builder.append(allDupes.getLong(kill_pos));
				builder.append(")");				
				builder.append('\n');
			}
			txt.setText(builder);
		}else if (v == dupeM_btn){
			Cursor allDupes = dbAdapter.getMDupes();
			startManagingCursor(allDupes);
			
			int kill_pos = allDupes.getColumnIndex(ExerciseMuscles._ID);
			int exercise_pos = allDupes.getColumnIndex(ExerciseMuscles.KEY_EXERCISE);
			int mg_pos = allDupes.getColumnIndex(ExerciseMuscles.KEY_MUSCLE);
			int count_pos = allDupes.getColumnIndex("COUNT");

			
			StringBuilder builder = new StringBuilder();
			while(allDupes.moveToNext()){
				builder.append(allDupes.getLong(exercise_pos));
				builder.append(" ");
				builder.append(allDupes.getLong(mg_pos));
				builder.append(" ");
				builder.append(allDupes.getInt(count_pos));
				builder.append(" (");
				builder.append(allDupes.getLong(kill_pos));
				builder.append(")");
				builder.append('\n');
			}
			txt.setText(builder);
		}else if (v == bad_mg_btn){
			Cursor allDupes = dbAdapter.getBadMg();
			startManagingCursor(allDupes);
			
			int id_pos = allDupes.getColumnIndex(ExerciseMuscleGroups._ID);
			int mg_pos = allDupes.getColumnIndex(ExerciseMuscleGroups.KEY_MUSCLE_GROUP);

			
			StringBuilder builder = new StringBuilder();
			while(allDupes.moveToNext()){
				builder.append(allDupes.getLong(id_pos));
				builder.append(" ");
				builder.append(allDupes.getLong(mg_pos));

			
				builder.append('\n');
			}
			txt.setText(builder);
		}else if (v == bad_m_btn){
			Cursor allDupes = dbAdapter.getBadM();
			startManagingCursor(allDupes);
			
			int id_pos = allDupes.getColumnIndex(ExerciseMuscles._ID);
			int mg_pos = allDupes.getColumnIndex(ExerciseMuscles.KEY_MUSCLE);

			
			StringBuilder builder = new StringBuilder();
			while(allDupes.moveToNext()){
				builder.append(allDupes.getLong(id_pos));
				builder.append(" ");
				builder.append(allDupes.getLong(mg_pos));

			
				builder.append('\n');
			}
			txt.setText(builder);
		}else if(v == kill_dupeMG_btn){
			txt.setText(dbAdapter.deleteDuplicateMG() + "");
		}else if(v == kill_dupeM_btn){
			txt.setText(dbAdapter.deleteDuplicateM() + "");
		}
	}
	
	private Map<Long, String> nameCache = new HashMap<Long, String>();
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
