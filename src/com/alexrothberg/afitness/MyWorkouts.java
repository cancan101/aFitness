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

import com.alexrothberg.afitness.DbAdapter.Workouts;


public class MyWorkouts extends ListActivity  {
	
	private DbAdapter mDbHelper;
	
    private final int list_item_layout = R.layout.std_list_item;

    private final String[] from = new String[]{Workouts.KEY_NAME};
    private final int[] to = new int[]{ R.id.std_list_item_name_txt};
    private Cursor allWorkouts;
    
    
    private static final String TAG = "MyWorkouts";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_workouts);
				
        mDbHelper = new DbAdapter(this);
        mDbHelper.open();
        
        registerForContextMenu(getListView());        
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
        menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(Workouts.KEY_NAME)));
        
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.my_workouts_context, menu);
		
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
			case R.id.my_workouts_context_view:
				viewWorkout(info.position, info.id);
				return true;
			case R.id.my_workouts_context_rename:
				renameWorkout(info.position, info.id);
				return true;
			case R.id.my_workouts_context_delete:
				deleteWorkout(info.id);
				return true;
		}
		return super.onContextItemSelected(item);
	}
	
	private void renameWorkout(int position, long id) {
		Intent intent = new Intent(this, EditWorkout.class);
		intent.putExtra(Workouts._ID, id);
        Cursor cursor = (Cursor) getListAdapter().getItem(position);
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }
		String workout_name = cursor.getString(cursor.getColumnIndex(Workouts.KEY_NAME));
		intent.putExtra(Workouts.KEY_NAME, workout_name);
		startActivity(intent);		
	}
	
	private void deleteWorkout(long id) {
		mDbHelper.deleteWorkout(id);
		fillData();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.my_workout_menu_add:
			addWorkout();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.my_workout_menu, menu);
	    return true;
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		viewWorkout(position, id);
	}

	private void viewWorkout(int position, long id) {
		Intent intent = new Intent(this, WorkoutExercisesList.class);
		intent.putExtra(Workouts._ID, id);
        Cursor cursor = (Cursor) getListAdapter().getItem(position);
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }
		String workout_name = cursor.getString(cursor.getColumnIndex(Workouts.KEY_NAME));
		intent.putExtra(Workouts.KEY_NAME, workout_name);
		startActivity(intent);
	}
	


	private void fillData() {
		allWorkouts = mDbHelper.fetchAllWorkouts();
		startManagingCursor(allWorkouts);

		ListAdapter adapter = new SimpleCursorAdapter(this, list_item_layout, allWorkouts, from, to);
		setListAdapter(adapter);
	}

	
	@Override
	protected void onResume() {
		super.onResume();
		fillData();
	}



	private void addWorkout() {
		Intent intent = new Intent(this, EditWorkout.class);
		startActivity(intent);
	}
	
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	mDbHelper.close();
    }

}