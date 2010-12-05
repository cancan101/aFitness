package com.alexrothberg.afitness;

import android.content.Context;
import android.database.Cursor;
import android.widget.AlphabetIndexer;
import android.widget.SectionIndexer;

public class SimpleCursorAdapterAlphabetIndexer extends SimpleCursorAdapterImage implements SectionIndexer{
	private final SectionIndexer alphabetIndexer;
	private static final String alphabet = " ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	public SimpleCursorAdapterAlphabetIndexer(Context context, int layout, Cursor c, String[] from, int[] to, int sortedColumnIndex){
		super(context, layout, c, from, to);
		alphabetIndexer = new AlphabetIndexer(c, sortedColumnIndex, alphabet);
	}

	@Override
	public int getPositionForSection(int section) {
		int ret = alphabetIndexer.getPositionForSection(section);
		return ret;
	}

	@Override
	public int getSectionForPosition(int position) {
		return alphabetIndexer.getSectionForPosition(position);
	}

	@Override
	public Object[] getSections() {
		return alphabetIndexer.getSections();
	}
	
}
