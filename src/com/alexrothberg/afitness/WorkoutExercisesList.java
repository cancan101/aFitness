package com.alexrothberg.afitness;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.alexrothberg.afitness.DbAdapter.Exercises;
import com.alexrothberg.afitness.DbAdapter.Workouts;

public class WorkoutExercisesList extends ListActivity {
	private Long workout_id; 
	private String workout_name;
	private Cursor exercises;
	private DbAdapter mDbHelper;
	
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
	        	throw new IllegalArgumentException("No workout supplied to WorkoutExercisesList");
	        }
        }       
		
		setTitle(workout_name);
		
		mDbHelper = new DbAdapter(this);
        mDbHelper.open();
		
		exercises = mDbHelper.fetchExercisesForWorkout(workout_id);
		startManagingCursor(exercises);
		
		String[] from = { Exercises.KEY_NAME };
		int[] to = { R.id.std_list_item_name_txt };
		
		ListAdapter adapter = new SimpleCursorAdapter(this, R.layout.std_list_item, exercises, from, to);
		
		setListAdapter(adapter);		
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(this, RecordExercise.class);
		intent.putExtra(Exercises._ID, id);
		exercises.moveToPosition(position);
		String exercise_name = exercises.getString(exercises.getColumnIndex(Exercises.KEY_NAME));
		intent.putExtra(Exercises.KEY_NAME, exercise_name);
		startActivity(intent);
	}
	
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	mDbHelper.close();
    }
}
