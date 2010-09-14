package com.alexrothberg.afitness;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.alexrothberg.afitness.DbAdapter.Exercises;
import com.alexrothberg.afitness.DbAdapter.MuscleGroups;
import com.alexrothberg.afitness.DbAdapter.Muscles;

public class ActivityList extends ListActivity {
	private String muscleGroup;
	private long muscleGroupId;
	private String muscleName;
	private long muscleId;
	private Cursor c;
	private DbAdapter adapter;
	private static final String TAG = "ActivityList"; 

	private int requestCode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestCode = getIntent().getIntExtra(MuscleGroupChooser.REQUEST_CODE_ID, MuscleGroupChooser.REQUEST_CODE_NONE);

		
		TextView empty = new TextView(this);
		empty.setText( "No Exercises");
		getListView().setEmptyView(empty);
		adapter = new DbAdapter(this);
		adapter.open();
		
		Bundle extras = getIntent().getExtras();
		if (extras != null){
			muscleGroupId = extras.getLong(MuscleGroupChooser.MUSCLE_GROUP_PREFIX + MuscleGroups._ID);
			muscleGroup = extras.getString(MuscleGroupChooser.MUSCLE_GROUP_PREFIX + MuscleGroups.KEY_NAME);
			muscleName = extras.getString(MuscleChooser.MUSCLE_PREFIX + Muscles.KEY_NAME);
			muscleId = extras.getLong(MuscleChooser.MUSCLE_PREFIX + Muscles._ID);
		}

		if(muscleId > 0){
			Log.v(TAG, "muscleId="+muscleId);
			c = adapter.fetchExercisesForMuscle(muscleId);
			setTitle(muscleName);
		}else if(muscleGroupId > 0){
			Log.v(TAG , "muscleGroupId="+muscleGroupId);
			// Muscle group is specified
			c = adapter.fetchExercisesForMuscleGroup(muscleGroupId);
			setTitle(muscleGroup);
		}else{
			c = adapter.fetchAllExercises();
			setTitle("All Exercises");			
		}
		startManagingCursor(c);
		
		String[] from = { Exercises.KEY_NAME };
		int[] to = { R.id.std_list_item_name_txt };
		
		setListAdapter(new SimpleCursorAdapter(this, R.layout.std_list_item, c, from, to));
		
		//ListView lv = getListView();
		//lv.setFastScrollEnabled(true);
		//lv.setTextFilterEnabled(true);
		
	}
	

	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		Intent intent = new Intent(this, RecordExercise.class);
		intent.putExtra(Exercises._ID, id);
		Log.v(TAG, getListView().getItemAtPosition(position).toString());
		
        Cursor cursor = (Cursor) getListAdapter().getItem(position);
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }
		String exercise_name = cursor.getString(cursor.getColumnIndex(Exercises.KEY_NAME));
		
		intent.putExtra(Exercises.KEY_NAME, exercise_name);
		
		if( requestCode == MuscleGroupChooser.REQUEST_CODE_CHOOSE_EXERCISE){
        	setResult(RESULT_OK, intent);
        	finish();			
		}else{
			startActivity(intent);
		}
		
	}
 	
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	adapter.close();
    }

}
