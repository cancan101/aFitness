package com.alexrothberg.afitness;


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.alexrothberg.afitness.DbAdapter.Activities;
import com.alexrothberg.afitness.DbAdapter.Exercises;
import com.alexrothberg.afitness.DbAdapter.UNITS;


public class RecordExercise extends ListActivity implements OnClickListener, OnDateSetListener{
	public static class MySimpleCursorAdapter extends SimpleCursorAdapter{
		private final int positionId, unitsId;
		
		private final int units_column_index;
		
		public MySimpleCursorAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to, int positionId, int unitsId) {
			super(context, layout, c, from, to);
			this.positionId=positionId;
			this.unitsId=unitsId;
			this.units_column_index = c.getColumnIndex(Activities.KEY_UNITS);
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
				units_txt = ((TextView)ret.findViewById(unitsId));
				ret.setTag(new ViewHolder(units_txt, set_txt));
			}else{			
				set_txt = viewHolder.set_txt;
				units_txt = viewHolder.units_txt;
			}			
			
			set_txt.setText("Set " + (position+1) + ": "); 
			Cursor c = getCursor();
			UNITS units = UNITS.values()[c.getInt(units_column_index)];
						
			units_txt.setText( Utilities.getUnitLabel(units) );
			
			return ret;
		}


	}
	
	private static final String TAG = "RecordExercise";
	
	private DbAdapter mDbHelper;
	
	private Button recordBtn;
	private EditText weightEntryTxt;
	private EditText repsEntryTxt;
	private EditText notesEntryTxt;
	
	private TextView notesLbl;
	
	private Button restTimerBtn;
	private View historyBtn;
	
	private Long exercise_id;
	private String exercise_name;

	private int mYear;

	private int mMonth;

	private int mDay;
	
	private static final int DATE_DIALOG_ID = 0;
	
	private CountDownTimer timer;
	
	//private static List<Object> foo;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_exercise);
                
        mDbHelper = new DbAdapter(this);
        mDbHelper.open();
        
        recordBtn = (Button)findViewById(R.id.recordBtn);
        recordBtn.setOnClickListener(this);
        
        weightEntryTxt = (EditText)findViewById(R.id.weightEntryTxt);
        repsEntryTxt = (EditText)findViewById(R.id.repsEntryTxt);
        
        restTimerBtn = (Button)findViewById(R.id.restTimerBtn);
        restTimerBtn.setOnClickListener(this);
        
        historyBtn = findViewById(R.id.historyBtn);
        historyBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showHistory();				
			}
		});
        
        notesLbl = (TextView)findViewById(R.id.notesLbl);
        notesEntryTxt = (EditText)findViewById(R.id.notesTxt);
        
        registerForContextMenu(getListView());    
        
        loadIntentData();
        

    }

	private void loadIntentData() {
		long recordDate = getIntent().getLongExtra(Activities.KEY_RECORD_DATE, Long.MIN_VALUE);
        final Calendar c;
        if(recordDate > Long.MIN_VALUE){
        	c = Calendar.getInstance();
        	c.setTimeInMillis(recordDate);
        }else{        
	        c = Calendar.getInstance();
        }

        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);        
        
        Bundle extras = getIntent().getExtras();
        exercise_id = extras.getLong(Exercises._ID);
        exercise_name = extras.getString(Exercises.KEY_NAME);
        
        assert(exercise_name.equals(mDbHelper.getExerciseName(exercise_id)));
        
        onDateChange();
	}
    
    @Override
    protected void onNewIntent(Intent intent) {
    	super.onNewIntent(intent);
    	Log.v(TAG, "START: " + intent.getStringExtra(Exercises.KEY_NAME));
    	setIntent(intent);
        loadIntentData();
    }

	private void updateTitle() {
		Calendar calendar = getCalendar();
		
		String date_str = Utilities.getRelativeDateString(calendar);
//		setTitle("Log Entry for " + exercise_name + "(" +exercise_id+ ")" + " " + date_str);
		if(date_str.equals("Today")){
			setTitle(exercise_name);
		}else{
			setTitle(exercise_name + " for " + date_str);
		}

	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
        Cursor cursor = (Cursor) getListAdapter().getItem(position);
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }
		int reps = cursor.getInt(cursor.getColumnIndex(Activities.KEY_REPS));
		float weight = cursor.getFloat(cursor.getColumnIndex(Activities.KEY_WEIGHT));
		
		setWorkoutInfo(weight, reps);

	}

    
    private void setWorkoutInfo(float weight, int reps) {
    	repsEntryTxt.setText(Integer.toString(reps));
    	weightEntryTxt.setText(Utilities.floatToString(weight));
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

		ListAdapter adapter = new MySimpleCursorAdapter(this, list_item_layout, allActivities, from, to, R.id.rep_num, R.id.units_txt);
		setListAdapter(adapter);
	}

	@Override
	public void onClick(View v) {
		if(v == recordBtn){
			onRecordClick();
		}else if(v == restTimerBtn){
			onTimerClick();
		}


	}
	
	private long getTimerInterval(){
		return RecordExercisePrefActivity.getSetRestTime(this);
	}
	

	private void onTimerClick() {
		final RecordExercise that = this;
		final Intent notificationIntent = new Intent(that.getIntent());
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		Log.v(TAG, "WOULD START: " + notificationIntent.getStringExtra(Exercises.KEY_NAME));
		final PendingIntent contentIntent = PendingIntent.getActivity(that, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
		
		if(timer == null){
			timer = new CountDownTimer(getTimerInterval() * 1000, 450){
				private long last = Long.MAX_VALUE;
				
				@Override
				public void onFinish() {
					timer = null;
//					Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//					vibrator.vibrate(2000);
					
					final NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					
					final Notification notification = new Notification(android.R.drawable.stat_notify_sync_noanim, "Rest Complete", System.currentTimeMillis());

					notification.setLatestEventInfo(getApplicationContext(), "Rest After Exercise Complete", "Select to return to " + that.exercise_name, contentIntent);
					
					notification.defaults |= Notification.DEFAULT_SOUND;
					notification.defaults |= Notification.DEFAULT_VIBRATE;
					notification.flags |= Notification.FLAG_AUTO_CANCEL;
					mNotificationManager.cancel(1);
					mNotificationManager.notify(1, notification);
					resetTimer();
				}
	
				@Override
				public void onTick(long millisUntilFinished) {
					final long seconds_left = millisUntilFinished/1000;
					if(seconds_left != this.last){
						restTimerBtn.setText(Long.toString(seconds_left));
						last = seconds_left;
					}
				}			
			};
			timer.start();
		}else{
			timer.cancel();
			resetTimer();
			timer = null;
		}
		
	}

	private void showHistory() {
		Intent i = new Intent(this, ExerciseHistory.class);
		i.putExtra(Exercises._ID, exercise_id);
		i.putExtra(Exercises.KEY_NAME, exercise_name);
		startActivity(i);
	}
	
	@Override
	protected void onResume() {
		super.onResume();

        if(RecordExercisePrefActivity.getShowNotes(this)){
        	notesEntryTxt.setVisibility(View.VISIBLE);
        	notesLbl.setVisibility(View.VISIBLE);
        }else{
        	notesEntryTxt.setVisibility(View.GONE);
        	notesLbl.setVisibility(View.GONE);       	
        }
	}

	private void changeDate() {
		showDialog(DATE_DIALOG_ID);
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
			UNITS units =  UNITS.LBS; //TODO: make this user settable 
				
			long activity_id =  mDbHelper.recordActivity(exercise_id, record_date, reps, weight, units);
			
        	Toast toast = Toast.makeText(this, Utilities.floatToString(weight) + Utilities.getUnitLabel(units) + " for " + reps + " reps" , Toast.LENGTH_SHORT);
        	toast.show();
        	
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
			onDateChange();
		}
	}

	private void onDateChange() {
		updateTitle();
		fillData();
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
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.record_excercise_menu_change_date:
				changeDate();
				return true;
			case R.id.record_excercise_menu_history:
				showHistory();
				return true;
			case R.id.record_exercise_menu_prefs:
				showPrefs();
				return true;
		}
		return super.onContextItemSelected(item);
	}

	private void showPrefs() {
		final Intent i = new Intent(this, RecordExercisePrefActivity.class);
		startActivity(i);
	}

	private void deleteWorkout(long activity_id) {
		mDbHelper.deleteActivity(activity_id);
		fillData();
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
			case R.id.record_exercises_context_delete:
				deleteWorkout(info.id);
				return true;
		}
		return super.onContextItemSelected(item);

	}

	private void resetTimer() {
		restTimerBtn.setText("Timer");
	}
	
}