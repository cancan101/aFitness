package com.alexrothberg.afitness;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

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
	
	private static final int REQUEST_CODE_CREATE_EXERCISE = 1;
	
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
		registerForContextMenu(getListView());

		populateList();
		
		//ListView lv = getListView();
		//lv.setFastScrollEnabled(true);
		//lv.setTextFilterEnabled(true);
		
	}



	private void populateList() {
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
	}
	

	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		viewExercise(position, id);
		
	}



	private void viewExercise(int position, long id) {
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.activity_list_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.activity_list_menu_add_exercise:
			addExercise();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void addExercise() {
		Intent intent = new Intent(this, CreateExercise.class);
		
		if(muscleGroupId != 0L){
			intent.putExtra(CreateExercise.MUSCLE_GROUP_PREFIX +":" + MuscleGroups._ID, muscleGroupId);
		}
		
		if(muscleId != 0L){
			intent.putExtra(CreateExercise.MUSCLE_PREFIX +":" + Muscles._ID, muscleId);
		}
		
		startActivityForResult(intent, REQUEST_CODE_CREATE_EXERCISE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
			case REQUEST_CODE_CREATE_EXERCISE:
				if(resultCode == RESULT_OK){
					populateList();
				}
				break;
			default:
				super.onActivityResult(requestCode, resultCode, data);
				break;
			
		}		
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
        
        int position = info.position;
        String exerciseName = getExerciseNameFromPosition(position);

        if(exerciseName == null){
        	return;
        }
        
        // Setup the menu header
        menu.setHeaderTitle(exerciseName);
        
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_list_context, menu);
		
		if(requestCode == MuscleGroupChooser.REQUEST_CODE_CHOOSE_EXERCISE){
			MenuItem viewItem = menu.findItem(R.id.activity_list_context_view);
			viewItem.setTitle("Select Exercise");
			viewItem.setTitleCondensed("Select");
		}
		
	}



	private String getExerciseNameFromPosition(int position) {
		String exerciseName = null;
        Cursor cursor = (Cursor) getListAdapter().getItem(position);
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
        	exerciseName = null;
        }
        exerciseName = cursor.getString(cursor.getColumnIndex(Exercises.KEY_NAME));
		return exerciseName;
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
			case R.id.activity_list_context_view:
				viewExercise(info.position, info.id);
				return true;
			case R.id.activity_list_context_delete:
				deleteWorkoutExercise(info.position, info.id);
				return true;
		}
		return super.onContextItemSelected(item);
	}



	private boolean deleteWorkoutExercise(int position, final long exercise_id) {
        final String exerciseName = getExerciseNameFromPosition(position);
        if(exerciseName == null){
        	return false;
        }
        
        final Context context = this;
        
		new AlertDialog.Builder(this)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setTitle("Delete Exercise?")
        .setMessage("Are you sure that you want to delete " + exerciseName + "? This will delete all records of this exercise!")
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            	int num_rows_deleted = adapter.deleteExercise(exercise_id);
            	Toast toast = Toast.makeText(context, "Deleted: " + exerciseName + " (" + num_rows_deleted +  ")", Toast.LENGTH_LONG);
            	toast.show();
            	populateList();
            	
            }

        })
        .setNegativeButton("No", null)
        .show();

        return true;
    }	

}
