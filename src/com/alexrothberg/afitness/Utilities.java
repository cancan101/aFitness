package com.alexrothberg.afitness;

import java.text.DateFormat;
import java.util.Calendar;

import android.content.Context;

public final class Utilities {
	private Utilities(){}
	
	public static boolean compareDates(Calendar a, Calendar b){
		return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
		a.get(Calendar.MONTH) == b.get(Calendar.MONTH) &&
		a.get(Calendar.DATE) == b.get(Calendar.DATE);
	}
	
	public static String getDateString(Calendar calendar) {
		return getDateString(calendar, DateFormat.getDateInstance());
	}
	
	public static String getDateString(Calendar calendar, final Context context) {
		return getDateString(calendar, android.text.format.DateFormat.getDateFormat(context));
	}
	
	public static String getDateString(Calendar calendar, final DateFormat dateFormat) {
		Calendar today = Calendar.getInstance();
		Calendar yesterday = (Calendar) today.clone();
		yesterday.add(Calendar.DATE, -1);
		String label;
		if(compareDates(today, calendar)){
			label = "Today";
		}else if(compareDates(today, calendar)){
			label = "Yesterday";
		}else{
			label = dateFormat.format(calendar.getTime());
		}
		return label;
	}
	
}
