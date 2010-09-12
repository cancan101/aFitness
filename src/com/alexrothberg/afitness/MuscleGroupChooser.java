package com.alexrothberg.afitness;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.alexrothberg.afitness.DbAdapter.MuscleGroups;

public class MuscleGroupChooser extends ListActivity{
	private static final String TAG = "MuscleGroupChooser";

	private DbAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setTitle("Muscle Groups");
		
		adapter = new DbAdapter(this);
		adapter.open();


		
		Cursor muscleGroups = adapter.fetchAllMuscleGroups();
		MatrixCursor extras = new MatrixCursor(new String[]{MuscleGroups._ID, MuscleGroups.KEY_NAME});
		extras.addRow(new Object[]{0, "All Exercises"});
		extras.addRow(new Object[]{-1, "All Muscles"});
		Cursor combined = new MergeCursor(new Cursor[]{extras, muscleGroups} );
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
	        i.putExtra("MuscleGroups:" + MuscleGroups._ID, id);
	        i.putExtra("MuscleGroups:" + MuscleGroups.KEY_NAME, muscleGroup);
        }else if (id ==0){
        	i = new Intent(this, ActivityList.class);
        }else{
        	i = new Intent(this, MuscleChooser.class);
        }		
		
		startActivity(i);		
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
	
	
}
