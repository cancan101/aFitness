package com.alexrothberg.afitness;

import com.alexrothberg.afitness.DbAdapter.Activities;
import com.alexrothberg.afitness.DbAdapter.Exercises;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

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
		}	
		
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
