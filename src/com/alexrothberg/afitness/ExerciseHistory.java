package com.alexrothberg.afitness;
import java.util.Calendar;
import java.util.Date;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.alexrothberg.afitness.DbAdapter.Activities;
import com.alexrothberg.afitness.DbAdapter.UNITS;


public class ExerciseHistory extends ListActivity {
	private DbAdapter dbAdapter;
	private Bundle extras;
	private long exerciseId;
	private long dateLong;
	private String exercise_name;
	
	private static final String TAG = "ExerciseHistory";
	
	private static enum STATE {
		DATES,
		EXERCISES,
	}
	
	private STATE state;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		dbAdapter = new DbAdapter(this);
		dbAdapter.open();
		
		extras = getIntent().getExtras();
		
		exerciseId = extras.getLong(DbAdapter.Exercises._ID);
		exercise_name = extras.getString(DbAdapter.Exercises.KEY_NAME);
		
		setDateState();
		
		fillData();
	}

	private void setDateState() {
		setTitle("History for: " + exercise_name + "("+ exerciseId + ")");
		
		state = STATE.DATES;
	}

	private void fillData() {
		ListAdapter adapter = null;
		if(state == STATE.DATES){
			Cursor workoutDates = dbAdapter.getWorkoutDates(exerciseId);
			startManagingCursor(workoutDates);
			
			String[] from = { Activities.KEY_RECORD_DATE };
			int[] to = { R.id.std_list_item_name_txt };
			adapter =  new SimpleCursorAdapter(this, R.layout.std_list_item, workoutDates, from, to);
			
			final Context context = this;
			
			((SimpleCursorAdapter)adapter).setViewBinder(new SimpleCursorAdapter.ViewBinder() {
				
				@Override
				public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
					if (view.getId() == R.id.std_list_item_name_txt){
						long date_long = cursor.getLong(columnIndex);
						Calendar workout_date = Calendar.getInstance();
						workout_date.setTimeInMillis(date_long);
	
					    String label = Utilities.getDateString(workout_date, context);
						
						((TextView) view).setText(label);
						return true;
					}else{
						return false;
					}
				}
	
			});
		}else{
			Cursor allActivities = dbAdapter.fetchAllActivities(exerciseId, new Date(dateLong));
			startManagingCursor(allActivities);
	        int list_item_layout = R.layout.std_list_item;
	        String[] from = new String[]{Activities.KEY_REPS, Activities.KEY_WEIGHT};
	        int[] to = new int[]{ R.id.std_list_item_name_txt};

			adapter = new SetCursorAdapter(this, list_item_layout, allActivities, from, to);
			setListAdapter(adapter);
			
		}
		setListAdapter(adapter);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);		
		
		if(state == STATE.DATES){
	        Cursor cursor = (Cursor) getListAdapter().getItem(position);
	        if (cursor == null) {
	            return;
	        }
			long dateLong = cursor.getLong(cursor.getColumnIndex(Activities.KEY_RECORD_DATE));
			
			viewActiviesForDate(dateLong);
		}
				
	}
	
	@Override
	public void onBackPressed() {
		if(state == STATE.EXERCISES){
			setDateState();
			fillData();
		}else{
			super.onBackPressed();			
		}
	}

	private void viewActiviesForDate(long dateLong) {
//		Intent intent = new Intent(this, RecordExercise.class);
//		intent.putExtra(Activities.KEY_RECORD_DATE, dateLong);
//		intent.putExtra(Exercises._ID, exerciseId);
//		intent.putExtra(Exercises.KEY_NAME, exercise_name);
//		startActivity(intent);
		this.dateLong=dateLong;
		setExerciseState();
		fillData();
	}

	private void setExerciseState() {
		state = STATE.EXERCISES;
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(dateLong);
		setTitle("History for: " + exercise_name + "("+ exerciseId + ")  on " + Utilities.getDateString(calendar, this));
	}
	
	public static class SetCursorAdapter extends SimpleCursorAdapter{
		public SetCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);

		}
		
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {			
			View ret = super.getView(position, convertView, parent);

			StringBuilder builder = new StringBuilder();
			builder.append("Set " + (position+1) + ": "); 
			Cursor c = getCursor();
			builder.append(c.getInt(c.getColumnIndex(Activities.KEY_REPS)));
			builder.append("@");
			builder.append(Float.toString(c.getFloat(c.getColumnIndex(Activities.KEY_WEIGHT))));
			
			int units = c.getInt(c.getColumnIndex(Activities.KEY_UNITS));
			
			
			if(units == UNITS.LBS.ordinal()){
				builder.append("lbs");
			}else if(units == UNITS.KGS.ordinal()){
				builder.append("kgs");
			}else if(units == UNITS.PLATES.ordinal()){
				builder.append("plates");
			}else{
				builder.append("(units=#" + units + ")");
			}				
			
			((TextView)ret).setText(builder);
			
			return ret;
		}
		
	}

	

}
