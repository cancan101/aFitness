package com.alexrothberg.afitness;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.alexrothberg.afitness.DbAdapter.Activities;
import com.alexrothberg.afitness.DbAdapter.Exercises;

public class LogActivity extends ListActivity {
	private DbAdapter adapter;
	
	private enum STATE { DATES, EXERCISES };
	
	private STATE state;
	
	private long selected_date_long;
	
	private Cursor allWorkoutDates, allEercises;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		adapter = new DbAdapter(this);
		adapter.open();
		
		goDateMode();
	}

	private void goDateMode() {
		state = STATE.DATES;
		showDates();
	}

	private void showDates() {
		if(allWorkoutDates != null){
			allWorkoutDates.close();
		}

		if(allEercises != null){
			allEercises.close();
		}
		
		allWorkoutDates = adapter.getAllWorkoutDates();
		startManagingCursor(allWorkoutDates);
		setListAdapter(new WorkoutDateSummary(this,  R.layout.workout_summary_list_item , allWorkoutDates, new String[]{ BaseColumns._ID, "exercise_count", "activity_count" }, new int[]{ R.id.workout_summary_date_txt, R.id.workout_summary_exercises_txt, R.id.workout_summary_activities_txt} ));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		if(state == STATE.DATES){
			selectDate(id);
		}else if(state == STATE.EXERCISES){
	        Cursor cursor = (Cursor) getListAdapter().getItem(position);
	        if (cursor == null) {
	            // For some reason the requested item isn't available, do nothing
	            return;
	        }
			String exercise_name = cursor.getString(cursor.getColumnIndex(Exercises.KEY_NAME));
			
			selectExeciseDate(id, exercise_name);
		}
		
	}
	
	private void selectExeciseDate(final long exercise_id, final String exercise_name) {
		Intent intent = new Intent(this, RecordExercise.class);
		
		intent.putExtra(Exercises._ID, exercise_id);
		intent.putExtra(Exercises.KEY_NAME, exercise_name);
		intent.putExtra(Activities.KEY_RECORD_DATE, this.selected_date_long);
		
		startActivity(intent);
		
	}

	private void selectDate(long id) {
		selected_date_long = id;
		goExerciseMode();
	}

	private void goExerciseMode() {
		state = STATE.EXERCISES;
		showExercises();		
	}

	private void showExercises() {
		if(allWorkoutDates != null){
			allWorkoutDates.close();
		}

		if(allEercises != null){
			allEercises.close();
		}
		
		allEercises = adapter.getAlExerciseOn(selected_date_long);
		startManagingCursor(allEercises);
		setListAdapter(new SimpleCursorAdapter(this,  R.layout.workout_summary_exercises_list_item , allEercises, new String[]{ Exercises.KEY_NAME, "activity_count" }, new int[]{ R.id.workout_summary_exercises_name_txt, R.id.workout_summary_exercises_activities_txt} ));
	}
	
	@Override
	public void onBackPressed() {
		if(state == STATE.EXERCISES){
			goDateMode();
		}else{
			super.onBackPressed();			
		}
	}

	@Override
	protected void onDestroy() {
		adapter.close();
		super.onDestroy();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.log_menu_debug:
				Intent intent = new Intent(this, DebugActivity.class);
				startActivity(intent);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void addExercise() {
		Intent intent = new Intent(this, CreateExercise.class);
		startActivity(intent);		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.log_menu, menu);
	    return true;
	}	
	
	private static final class WorkoutDateSummary extends SimpleCursorAdapter{
		private final int date_column_index;
		private final Context context;
		public WorkoutDateSummary(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
			
			this.date_column_index = c.getColumnIndex(BaseColumns._ID);
			this.context=context;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View ret =  super.getView(position, convertView, parent);
			
			TextView date_txt = (TextView)ret.findViewById(R.id.workout_summary_date_txt);
			date_txt.setText(Utilities.getRelativeDateString(getCursor().getLong(date_column_index), context));
			
			return ret;
		}		
	}
}
