package com.alexrothberg.afitness;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class RecordExercisePrefActivity extends PreferenceActivity {
	private static final boolean SHOW_NOTES_DEFAULT = false;
	private static final String SHOW_NOTES_KEY = "show_notes";
	
	private static final long SET_REST_TIME_DEFAULT = 60L;
	private static final String SET_REST_TIMER_KEY = "set_rest_time";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.record_workout_prefs);
	}
	
	public static boolean getShowNotes(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SHOW_NOTES_KEY, SHOW_NOTES_DEFAULT);
	}
	
	public static long getSetRestTime(Context context){
		return Long.parseLong(PreferenceManager.getDefaultSharedPreferences(context).getString(SET_REST_TIMER_KEY, Long.toString(SET_REST_TIME_DEFAULT)));
	}
}
