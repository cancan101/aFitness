package com.alexrothberg.afitness;

import java.text.DateFormat;
import java.util.Calendar;

import android.content.Context;

public final class Utilities {
	private Utilities(){}
	
	public static boolean compareDates(final Calendar a, final Calendar b){
		return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
		a.get(Calendar.MONTH) == b.get(Calendar.MONTH) &&
		a.get(Calendar.DATE) == b.get(Calendar.DATE);
	}
	
	public static String getRelativeDateString(final long date_long, final Context context) {
		final Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date_long);
		
		return getRelativeDateString(calendar, context);
	}	
	
	public static String getRelativeDateString(final Calendar calendar) {
		return getRelativeDateString(calendar, DateFormat.getDateInstance());
	}
	
	public static String getRelativeDateString(final Calendar calendar, final Context context) {
		return getRelativeDateString(calendar, android.text.format.DateFormat.getDateFormat(context));
	}
	
	public static String getDateString(final long date_long, final Context context) {
		final Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(date_long);
		
		return getDateString(calendar, android.text.format.DateFormat.getDateFormat(context));
	}
	
	public static String getDateString(final Calendar calendar, final Context context) {
		return getDateString(calendar, android.text.format.DateFormat.getDateFormat(context));
	}
	
	public static String getRelativeDateString(final Calendar calendar, final DateFormat dateFormat) {
		Calendar today = Calendar.getInstance();
		Calendar yesterday = (Calendar) today.clone();
		yesterday.add(Calendar.DATE, -1);
		String label;
		if(compareDates(today, calendar)){
			label = "Today";
		}else if(compareDates(today, calendar)){
			label = "Yesterday";
		}else{
			label = getDateString(calendar, dateFormat);
		}
		return label;
	}

	public static String getDateString(final Calendar calendar,
			final DateFormat dateFormat) {
		return dateFormat.format(calendar.getTime());
	}
	
}
