package com.alexrothberg.afitness;


import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.alexrothberg.afitness.DbAdapter.Activities;
import com.alexrothberg.afitness.DbAdapter.Exercises;
import com.alexrothberg.afitness.DbAdapter.UNITS;
import com.alexrothberg.afitness.DbAdapter.Workouts;


public class RecordExercise extends ListActivity implements OnClickListener, OnDateSetListener{
	public static class MySimpleCursorAdapter extends SimpleCursorAdapter{
		private final int positionId;
		public MySimpleCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to, int positionId) {
			super(context, layout, c, from, to);
			this.positionId=positionId;
		}
		
		private static class ViewHolder{
			public final TextView units_txt;
			public final  TextView set_txt;
			public ViewHolder(TextView units_txt, TextView set_txt){
				this.units_txt=units_txt;
				this.set_txt=set_txt;
			}
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {			
			View ret = super.getView(position, convertView, parent);
			TextView set_txt, units_txt;
			ViewHolder viewHolder = (ViewHolder)ret.getTag();
			if(viewHolder==null){
				set_txt = ((TextView)ret.findViewById(positionId));
				units_txt = ((TextView)ret.findViewById(R.id.units_txt));
				ret.setTag(new ViewHolder(units_txt, set_txt));
			}else{			
				set_txt = viewHolder.set_txt;
				units_txt = viewHolder.units_txt;
			}			
			
			set_txt.setText("Set " + (position+1) + ": "); 
			Cursor c = getCursor();
			int units = c.getInt(c.getColumnIndex(Activities.KEY_UNITS));
			
			if(units == UNITS.LBS.ordinal()){
				units_txt.setText("lbs");
			}else if(units == UNITS.KGS.ordinal()){
				units_txt.setText("kgs");
			}else if(units == UNITS.PLATES.ordinal()){
				units_txt.setText("plates");
			}else{
				units_txt.setText("(units=#" + units + ")");
			}
				
			
			return ret;
		}
		
	}
	
	private static final String TAG = "RecordExercise";
	
	private DbAdapter mDbHelper;
	
	private Button recordBtn;
	private TextView weightEntryTxt;
	private TextView repsEntryTxt;
	
	private ImageButton restTimerBtn, historyBtn, recordDatebtn;
	
	private Long exercise_id;
	private String exercise_name;

	private int mYear;

	private int mMonth;

	private int mDay;
	
	private static final int DATE_DIALOG_ID = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_exercise);
        
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        
        
        mDbHelper = new DbAdapter(this);
        mDbHelper.open();
        
        recordBtn = (Button)findViewById(R.id.recordBtn);
        recordBtn.setOnClickListener(this);
        
        weightEntryTxt = (TextView)findViewById(R.id.weightEntryTxt);
        repsEntryTxt = (TextView)findViewById(R.id.repsEntryTxt);
        
        restTimerBtn = (ImageButton)findViewById(R.id.restTimerBtn);
        restTimerBtn.setOnClickListener(this);

        historyBtn = (ImageButton)findViewById(R.id.historyBtn);
        historyBtn.setOnClickListener(this);
        
        recordDatebtn = (ImageButton)findViewById(R.id.recordDatebtn);
        recordDatebtn.setOnClickListener(this);
        
        Bundle extras = getIntent().getExtras();
        exercise_id = extras.getLong(Exercises._ID);
        exercise_name = extras.getString(Exercises.KEY_NAME);
        updateTitle();
        
        fillData();
        
        registerForContextMenu(getListView());
    
    }

	private void updateTitle() {
		Calendar calendar = getCalendar();
		
		String date_str = null;
		if(compareDates(calendar, Calendar.getInstance())){
			date_str = "today";
		}else{
			date_str = "on " + DateFormat.getDateInstance().format(calendar.getTime());
		}
		setTitle("Log Entry for " + exercise_name + " " + date_str);
	}
	
	private boolean compareDates(Calendar a, Calendar b){
		return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
		a.get(Calendar.MONTH) == b.get(Calendar.MONTH) &&
		a.get(Calendar.DATE) == b.get(Calendar.DATE);
	}
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	mDbHelper.close();
    }

	private void fillData() {
		Date record_date = getCalendar().getTime();

		Cursor allActivities = mDbHelper.fetchAllActivities(exercise_id, record_date);
		startManagingCursor(allActivities);
        int list_item_layout = R.layout.session_history_list_item;
        String[] from = new String[]{Activities.KEY_REPS, Activities.KEY_WEIGHT};
        int[] to = new int[]{ R.id.rep_txt, R.id.weight_txt};

		ListAdapter adapter = new MySimpleCursorAdapter(this, list_item_layout, allActivities, from, to, R.id.rep_num);
		setListAdapter(adapter);
	}

	@Override
	public void onClick(View v) {
		if(v == recordBtn){
			onRecordClick();
		}else if(v == restTimerBtn){
//	        Intent i = new Intent(this, ActivityList.class);
//	        startActivity(i);
		}else if(v==historyBtn){
//	        Intent i = new Intent(this, MainUI.class);
//	        startActivity(i);			
		}else if(v==recordDatebtn){
			showDialog(DATE_DIALOG_ID);
		}

	}

	private void onRecordClick() {
		String rep_str = repsEntryTxt.getText().toString().trim();
		String weight_str = weightEntryTxt.getText().toString().trim();
		
		if (TextUtils.isEmpty(rep_str) || TextUtils.isEmpty(weight_str)){
			Log.v(TAG, "Attempted to record activity w/o filling in weight and reps");
			return;
		}
		
		try{
			int reps = Integer.parseInt(rep_str);
			float weight = Float.parseFloat(weight_str);
			Log.i(TAG, "Recording activity");
			Date record_date = getCalendar().getTime();
			
			long activity_id =  mDbHelper.recordActivity(exercise_id, record_date, reps, weight, UNITS.LBS);
			if(activity_id == -1){
				Log.e(TAG, "Insertion failed");
			}else{
				fillData();
			}
		}catch(NumberFormatException ex){
			Log.e(TAG, "Unable to parse activity input", ex);
		}		
	}

	private Calendar getCalendar() {
		Calendar calendar = new GregorianCalendar(mYear, mMonth, mDay);
		return calendar;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
	    switch (id) {
	    case DATE_DIALOG_ID:
	        return new DatePickerDialog(this,
	                    this,
	                    mYear, mMonth, mDay);
	    }
	    return null;

	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		
		if(mYear != year || mDay != dayOfMonth || mMonth != monthOfYear){
			mYear = year;
			mDay = dayOfMonth;
			mMonth = monthOfYear;
			updateTitle();
			fillData();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.record_exercise_menu, menu);
	    return true;
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
        menu.setHeaderTitle("Set: " + (info.position+1));
        
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.record_exercises_context, menu);
		
	}	
}