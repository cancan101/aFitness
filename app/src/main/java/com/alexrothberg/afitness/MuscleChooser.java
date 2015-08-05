package com.alexrothberg.afitness;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.alexrothberg.afitness.DbAdapter.MuscleGroups;
import com.alexrothberg.afitness.DbAdapter.Muscles;
import com.alexrothberg.afitness.DbAdapter.OpenHandler;

public class MuscleChooser extends ListActivity {
	private static final String TAG = "ExerciseMuscleChooser";

	private long muscleGroupId = 0L;
	private String muscleGroup;
	private Bundle extras;
	
	private DbAdapter adapter;
	private int requestCode;
	
	public static final String MUSCLE_PREFIX = "Muscles:";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestCode = getIntent().getIntExtra(MuscleGroupChooser.REQUEST_CODE_ID, MuscleGroupChooser.REQUEST_CODE_NONE);
		extras = getIntent().getExtras();

		
		adapter = new DbAdapter(this);
		adapter.open(new OpenHandler() {
			
			@Override
			public void onSuccess() {
				populateList();
			}
		});
	}

	private void populateList() {
		Cursor muscles = null;
		
		if (extras != null){
			muscleGroupId = extras.getLong(MuscleGroupChooser.MUSCLE_GROUP_PREFIX + MuscleGroups._ID);
			muscleGroup = extras.getString(MuscleGroupChooser.MUSCLE_GROUP_PREFIX + MuscleGroups.KEY_NAME);			
		}
		
		if (muscleGroupId != 0L){
			setTitle("Muscles for " + muscleGroup);
			
			Log.v(TAG, "fetching muscles for " + muscleGroup + "(" + muscleGroupId + ")");
			muscles = adapter.fetchMusclesForMuscleGroup(muscleGroupId);
		}else{
			muscles = adapter.fetchAllMuscles();
			setTitle("All Muscles");			
		}
		
		Cursor combined = null;
		
		if( requestCode != MuscleGroupChooser.REQUEST_CODE_CHOOSE_MUSCLE){
			MatrixCursor extraMusclesItems = new MatrixCursor(new String[] { Muscles._ID, Muscles.KEY_NAME});
			if (muscleGroup != null){
				extraMusclesItems.addRow(new Object[] {0, "All " + muscleGroup  +" Exercises"});
			}else{
				extraMusclesItems.addRow(new Object[] {0, "All Exercises"});
			}
			
			combined = new MergeCursor(new Cursor[]{extraMusclesItems, muscles});
		}else{
			combined = muscles;
		}

		startManagingCursor(combined);

		String[] from = { Muscles.KEY_NAME };
		int[] to = { R.id.std_list_item_name_txt };
		
		setListAdapter(new ColorSpecial(this, R.layout.std_list_item, combined, from, to));
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
	    inflater.inflate(R.menu.muscle_chooser_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.muscle_chooser_menu_add_exercise:
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
		
		startActivity(intent);		
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
			
        Cursor cursor = (Cursor) getListAdapter().getItem(position);
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }
        Intent i = new Intent(this, ActivityList.class);
        if(extras != null){
        	i.putExtras(extras);
        }
        
        if(id>0){
        	String muscle = cursor.getString(cursor.getColumnIndex(Muscles.KEY_NAME));
	        i.putExtra(MUSCLE_PREFIX + Muscles._ID, id);
	        i.putExtra(MUSCLE_PREFIX + Muscles.KEY_NAME, muscle);	        
        }
        
        if(requestCode == MuscleGroupChooser.REQUEST_CODE_CHOOSE_MUSCLE){
        	assert(id > 0);
        	setResult(RESULT_OK, i);
        	finish();
        }else if(requestCode != MuscleGroupChooser.REQUEST_CODE_NONE){
        	startActivityForResult(i, requestCode);
        }else{        
        	startActivity(i);
        }
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == this.requestCode && resultCode==RESULT_OK){
        	setResult(resultCode, data);
        	finish();			
		}else{
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	protected class ColorSpecial extends SimpleCursorAdapter{
		public ColorSpecial(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View ret = super.getView(position, convertView, parent);
			Cursor cursor = (Cursor) getItem( position);
			long id = cursor.getLong(cursor.getColumnIndex(MuscleGroups._ID));
			if (id == 0){
	        	ret.setBackgroundResource(R.color.red);
	        }else{
	        	ret.setBackgroundColor(android.R.color.transparent);
	        }
			return ret;
		}
	}


}
