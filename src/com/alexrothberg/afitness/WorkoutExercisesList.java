package com.alexrothberg.afitness;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
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
	
	private static final String TAG = "WorkoutExercisesList";
	
	private static final String[] from = { Exercises.KEY_NAME };
	private static final int[] to = { R.id.std_list_item_name_txt };
	
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
		
		registerForContextMenu(getListView());
		
		fillData();
	}

	private void fillData() {
		exercises = mDbHelper.fetchExercisesForWorkout(workout_id);
		startManagingCursor(exercises);		

		
		ListAdapter adapter = new SimpleCursorAdapter(this, R.layout.std_list_item, exercises, from, to);
		
		setListAdapter(adapter);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		viewExercise(position, id);
	}
	
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	mDbHelper.close();
    }
    
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }

        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }

        // Setup the menu header
        menu.setHeaderTitle(workout_name + ": " + cursor.getString(cursor.getColumnIndex(Exercises.KEY_NAME)));
        
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.my_workout_exercises_context, menu);
		
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }
		
		switch(item.getItemId()){
			case R.id.my_workout_exercise_context_view:
				viewExercise(info.position, info.id);
				return true;
			case R.id.my_workout_exercise_context_delete:
				deleteWorkoutExercise(info.position, info.id);
		}
		return super.onContextItemSelected(item);
	}

	private void deleteWorkoutExercise(int position, long id) {
		exercises.moveToPosition(position);
		Long workout_exercise_id = exercises.getLong(exercises.getColumnIndex( DbAdapter.ENTRY_ID));
		deleteWorkoutExercise(workout_exercise_id);		
	}
	
	private void deleteWorkoutExercise(long workout_exercise_id){
		int ret = mDbHelper.deleteWorkoutExercise(workout_exercise_id);
		assert(ret == 1);
		fillData();
	}

	private void viewExercise(int position, long id) {
		exercises.moveToPosition(position);
		String exercise_name = exercises.getString(exercises.getColumnIndex(Exercises.KEY_NAME));
		Long exercise_id = exercises.getLong(exercises.getColumnIndex( Exercises._ID));
		viewExercise(exercise_id, exercise_name);
	}
	
	private void viewExercise(long exercise_id, String exercise_name){
		Intent intent = new Intent(this, RecordExercise.class);
		intent.putExtra(Exercises.KEY_NAME, exercise_name);
		intent.putExtra(Exercises._ID, exercise_id);
		startActivity(intent);		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.my_workout_menu_add:
			addExercise();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	private void addExercise() {
		Intent intent = new Intent(this, MuscleGroupChooser.class);
		intent.putExtra(MuscleGroupChooser.REQUEST_CODE_ID, MuscleGroupChooser.REQUEST_CODE_CHOOSE_EXERCISE);
		startActivityForResult(intent, MuscleGroupChooser.REQUEST_CODE_CHOOSE_EXERCISE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
			case MuscleGroupChooser.REQUEST_CODE_CHOOSE_EXERCISE:
				if(resultCode == RESULT_OK){
					long exercise_id = data.getLongExtra(Exercises._ID, 0);
					assert(exercise_id > 0);
					mDbHelper.addWorkoutExercise(workout_id, exercise_id);
				}
				break;
			default:
				super.onActivityResult(requestCode, resultCode, data);
				break;
			
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.my_workout_menu, menu);
	    return true;
	}
}
