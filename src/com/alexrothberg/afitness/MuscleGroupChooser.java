package com.alexrothberg.afitness;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.alexrothberg.afitness.DbAdapter.MuscleGroups;
import com.alexrothberg.afitness.DbAdapter.OpenHandler;

public class MuscleGroupChooser extends ListActivity{
	private static final String TAG = "MuscleGroupChooser";

	private DbAdapter adapter;
	private int requestCode;
		
	public static final int REQUEST_CODE_NONE = 0;
	public static final int REQUEST_CODE_CHOOSE_MUSCLE_GROUP = 1;
	public static final int REQUEST_CODE_CHOOSE_MUSCLE = 2;
	public static final int REQUEST_CODE_CHOOSE_EXERCISE = 3;
	
	public static final String REQUEST_CODE_ID = "REQUEST_CODE";
	public static final String MUSCLE_GROUP_PREFIX = "MuscleGroups:";
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestCode = getIntent().getIntExtra(REQUEST_CODE_ID, REQUEST_CODE_NONE);
		
		setTitle("Muscle Groups");
		
		adapter = new DbAdapter(this);
		adapter.open(new OpenHandler() {
			
			@Override
			public void onSuccess() {
				populateList();
			}
		});
	}

	private void populateList() {
		Cursor muscleGroups = adapter.fetchAllMuscleGroups();
		MatrixCursor extras = new MatrixCursor(new String[]{MuscleGroups._ID, MuscleGroups.KEY_NAME});
		
		if(requestCode != REQUEST_CODE_CHOOSE_MUSCLE_GROUP && requestCode != REQUEST_CODE_CHOOSE_MUSCLE){
			extras.addRow(new Object[]{0, "All Exercises"});
		}
		
		if(requestCode != REQUEST_CODE_CHOOSE_MUSCLE_GROUP){
			extras.addRow(new Object[]{-1, "All Muscles"});
		}
		
		Cursor combined;
		if(requestCode != REQUEST_CODE_CHOOSE_MUSCLE_GROUP){
			combined = new MergeCursor(new Cursor[]{extras, muscleGroups} );
		}else{
			combined = muscleGroups;
		}
		startManagingCursor(combined);
		
		String[] from = { MuscleGroups.KEY_NAME };
		int[] to = { R.id.std_list_item_name_txt };
		
		setListAdapter(new ColorSpecial(this, R.layout.std_list_item, combined, from, to));
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		adapter.close();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
			
        Cursor cursor = (Cursor) getListAdapter().getItem(position);
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }
        
        Intent i = null;
        if(id>0){
        	String muscleGroup = cursor.getString(cursor.getColumnIndex(MuscleGroups.KEY_NAME));
        	i = new Intent(this, MuscleChooser.class);
        	i.putExtras(getIntent());
	        i.putExtra(MUSCLE_GROUP_PREFIX + MuscleGroups._ID, id);
	        i.putExtra(MUSCLE_GROUP_PREFIX + MuscleGroups.KEY_NAME, muscleGroup);
        }else if (id ==0){
        	i = new Intent(this, ActivityList.class);
        	i.putExtras(getIntent());
        }else{
        	i = new Intent(this, MuscleChooser.class);
        	i.putExtras(getIntent());
        }
        
        if(requestCode == REQUEST_CODE_CHOOSE_MUSCLE_GROUP){
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
		//Log.v(TAG, resultCode + "");

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
	        }else if(id == -1){
	        	ret.setBackgroundResource(R.color.blue);
	        }else{
	        	ret.setBackgroundColor(android.R.color.transparent);
	        }
			return ret;
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.muscle_group_chooser_menu_add_exercise:
				addExercise();
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
	    inflater.inflate(R.menu.muscle_group_chooser_menu, menu);
	    return true;
	}
	
	
	
}
