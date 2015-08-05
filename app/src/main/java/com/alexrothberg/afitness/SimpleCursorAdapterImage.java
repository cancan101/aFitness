package com.alexrothberg.afitness;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

public class SimpleCursorAdapterImage extends SimpleCursorAdapter{
	private final Context context;

	public SimpleCursorAdapterImage(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		
		this.context=context;
	}
	
	/***
	 * This cache never needs to be invalidated as long as the program runs
	 */
	private final static Map<String, Integer> resource_cache = new HashMap<String, Integer>();	
	
	@Override
	public void setViewImage(ImageView v, String value) {
		if(value == null){
			v.setImageDrawable(null);
			return;
		}
		
		final int resource_id = getResourceFromName(value); 
		if(resource_id != 0){
			v.setImageResource(resource_id);
		}else{
			//super.setViewImage(v, value);
			v.setImageDrawable(null);
			return;
		}
	}
	

	private int getResourceFromName(String value) {
		final Integer cached_value = resource_cache.get(value);
		if(cached_value != null){
			return cached_value;
		}else{
			final Integer ret =  context.getResources().getIdentifier(value + "_t", "drawable", "com.alexrothberg.afitness");
			//Log.v(TAG, "resolving: " + value + "_t" +" to " + ret);
			resource_cache.put(value, ret);
			return ret;
		}
	}

}
