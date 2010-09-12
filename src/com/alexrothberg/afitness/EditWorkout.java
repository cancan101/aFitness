package com.alexrothberg.afitness;


import com.alexrothberg.afitness.DbAdapter.Workouts;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class EditWorkout extends Activity implements OnClickListener {
	private Button save_btn, cancel_btn;
	private EditText workout_name_txt;
	private Long workout_id = null;
	private String workout_name = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null){
			workout_id = (Long)savedInstanceState.getSerializable(Workouts._ID);
			workout_name = (String)savedInstanceState.getSerializable(Workouts.KEY_NAME);
		}else{
	        Bundle extras = getIntent().getExtras();
	        if (extras != null){
	        	workout_id =  extras.getLong(Workouts._ID);
	        	workout_name = extras.getString(Workouts.KEY_NAME);
	        }else{
	        	setTitle("New Workout");
	        	// We are creating new workout
	        }
        }
		
		if (!TextUtils.isEmpty(workout_name)){
			setTitle("Renaming: " + workout_name);
		}

		
		setContentView(R.layout.edit_workout);
		save_btn = (Button)findViewById(R.id.edit_workout_save_btn);
		save_btn.setOnClickListener(this);
		
		cancel_btn = (Button)findViewById(R.id.edit_workout_cancel_btn);
		cancel_btn.setOnClickListener(this);
		
		workout_name_txt = (EditText)findViewById(R.id.edit_workout_workout_name_txt);
		
		workout_name_txt.setText(workout_name);
		
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
		String workout_name = workout_name_txt.getText().toString().trim();
		if (!workout_name.equals("")){
			
			DbAdapter mDbHelper = new DbAdapter(this);
	        mDbHelper.open();
	        if (workout_id == null){
	        	mDbHelper.createWorkout(workout_name);
	        }else{
	        	mDbHelper.renameWorkout(workout_id, workout_name);
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
