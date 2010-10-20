package com.alexrothberg.afitness;


import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.alexrothberg.afitness.DbAdapter.Exercises;
import com.alexrothberg.afitness.DbAdapter.MuscleGroups;
import com.alexrothberg.afitness.DbAdapter.Muscles;

public class CreateExercise extends Activity implements OnClickListener {
	private Button save_btn, cancel_btn;
	private Long exercise_id = null;
	private String exercise_name = "";
	private Spinner muscle_group_spinner;
	private Spinner muscle_spinner;
	private TextView exercise_name_txt;
	private DbAdapter dbAdapter;
	
	private long requested_mg, requested_muscle;
	
	private Cursor muscleGroups;
	private Cursor muscles;
	
	public static final String MUSCLE_GROUP_PREFIX ="MuscleGroups"; 
	public static final String MUSCLE_PREFIX ="Muscles"; 
	
	private static final String TAG = "CreateExercise";
	private int goto_pos = -1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null){
			//exercise_id = (Long)savedInstanceState.getSerializable(Exercises._ID);
			//exercise_name = (String)savedInstanceState.getSerializable(Exercises.KEY_NAME);
		}else{
	        Bundle extras = getIntent().getExtras();
	        if (extras != null){
	        	exercise_id =  extras.getLong(Exercises._ID);
	        	exercise_name = extras.getString(Exercises.KEY_NAME);
	        	requested_mg = extras.getLong(MUSCLE_GROUP_PREFIX + ":" + MuscleGroups._ID);
	        	requested_muscle = extras.getLong(MUSCLE_PREFIX + ":" + Muscles._ID);
	        }else{
	        	setTitle("New Exercise");
	        	// We are creating new exercise
	        }
        }
		
		assert(exercise_id==null);

		dbAdapter = new DbAdapter(this);
		dbAdapter.open();
		
		
		setContentView(R.layout.exercises_create);
		getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		
		muscle_group_spinner = (Spinner)findViewById(R.id.muscle_group_spinner);
		muscle_spinner = (Spinner)findViewById(R.id.muscle_spinner);
		
		muscleGroups = dbAdapter.fetchAllMuscleGroups();
		startManagingCursor(muscleGroups);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, muscleGroups, new String[]{ MuscleGroups.KEY_NAME }, new int[]{ android.R.id.text1 });
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		muscle_group_spinner.setAdapter(adapter);
		muscle_group_spinner.setPrompt("Select muscle group");
		
		save_btn = (Button)findViewById(R.id.exercise_create_save_btn);
		save_btn.setOnClickListener(this);
		
		cancel_btn = (Button)findViewById(R.id.exercise_create_cancel_btn);
		cancel_btn.setOnClickListener(this);
		
		exercise_name_txt = (EditText)findViewById(R.id.exercise_create_name);
		
		exercise_name_txt.setText(exercise_name);

		if (!TextUtils.isEmpty(exercise_name)){
			setTitle("Editing: " + exercise_name);
		}else{
			cancel_btn.setVisibility(Button.INVISIBLE);
			save_btn.setText("Create");
		}

		muscle_group_spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				setMuscleSpinner(id);				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub				
			}
		});
		
		if(requested_muscle != 0L && requested_mg == 0L){
			Log.v(TAG, "requested_muscle provided but no requested_mg. Looking up mg.");
			requested_mg = dbAdapter.getMuscleGroupForMuscle(requested_muscle);
		}
		
		Log.v(TAG, "requested_mg=" + requested_mg);
		Log.v(TAG, "requested_muscle=" + requested_muscle);
		if(requested_mg != 0L){			
			selectMuscleGroup(requested_mg);
			setMuscleSpinner(requested_mg);
			if(requested_muscle != 0L){
				selectMuscle(requested_muscle);				
			}
			
		}
		
	}

	private void selectMuscle(long requestedMuscle) {
		do{
			if(muscles.getLong(muscles.getColumnIndex(Muscles._ID)) == requestedMuscle){
				int position = muscles.getPosition();
				Log.d(TAG, "Setting muscle_spinner.selection=" + position);
				//muscle_spinner.setSelection(position);
				goto_pos = position;
				return;
			}
		}while(muscles.moveToNext());
		
	}

	private void selectMuscleGroup(long requestedMg) {
		do{
			if(muscleGroups.getLong(muscleGroups.getColumnIndex(MuscleGroups._ID)) == requestedMg){
				int position = muscleGroups.getPosition();
				muscle_group_spinner.setSelection(position);
				return;
			}
		}while(muscleGroups.moveToNext());
				
	}

	protected void setMuscleSpinner(long musclegroup_id) {
		muscles = dbAdapter.fetchMusclesForMuscleGroup(musclegroup_id);
		startManagingCursor(muscles);
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, muscles, new String[]{ Muscles.KEY_NAME }, new int[]{ android.R.id.text1 });
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		muscle_spinner.setAdapter(adapter);
		muscle_spinner.setPrompt("Select muscle");
		
		if(goto_pos != -1){
			muscle_spinner.setSelection(goto_pos);
			goto_pos = -1;
		}
		
	}
	
	@Override
		protected void onDestroy() {
			super.onDestroy();
			dbAdapter.close();
		}

	@Override
	public void onClick(View v) {
		if (v == save_btn){
			saveWorkout();
		}else if(v == cancel_btn){
			cancelEdit();
		}
		
	}

	private void saveWorkout() {
		String exercise_name = exercise_name_txt.getText().toString().trim();
		if (!TextUtils.isEmpty(exercise_name)){
			
			DbAdapter mDbHelper = new DbAdapter(this);
	        mDbHelper.open();
	        if (exercise_id == null || exercise_id==0L){
	        	long exercise_id = mDbHelper.createExercise(exercise_name);
	        	assert(exercise_id > 0);
	        	
	        	long muscle_id = muscle_spinner.getSelectedItemId();
	        	assert(muscle_id != Spinner.INVALID_ROW_ID);

	        	long muscleGroup_id = muscle_group_spinner.getSelectedItemId();
	        	assert(muscleGroup_id != Spinner.INVALID_ROW_ID);
	        	
	        	mDbHelper.recordPrimaryMuscle(exercise_id, muscle_id);
	        	mDbHelper.recordMuscleGroup(exercise_id, muscleGroup_id);
	        	
	        	Toast toast = Toast.makeText(this, "Exercise Created: " + exercise_name, Toast.LENGTH_LONG);
	        	toast.show();
	        	
	        }else{
	        	throw new RuntimeException("not here!");
	        //	mDbHelper.renameWorkout(workout_id, workout_name);
	        }
	        mDbHelper.close();
	        
			setResult(RESULT_OK);
			finish();
		}
		
	}

	private void cancelEdit() {
		setResult(RESULT_CANCELED);
		finish();
	}
}
