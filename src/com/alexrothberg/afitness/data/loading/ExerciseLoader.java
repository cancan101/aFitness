package com.alexrothberg.afitness.data.loading;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.alexrothberg.afitness.DbAdapter;
import com.alexrothberg.afitness.R;

public class ExerciseLoader {
	private static final String TAG = "ExerciseLoader";
	private final Context context;
	private final SQLiteDatabase mDb;
	
	public ExerciseLoader(Context context, SQLiteDatabase mDb){
		this.context = context;
		
		this.mDb = mDb;
	}
	
	private final int STATE_NONE=0;
	private final int STATE_DOCUMENT=1;
	private final int STATE_EXERCISES=2;
	private final int STATE_EXERCISE=3;
	private final int STATE_PRIMARY=4;
	private final int STATE_SECONDARY=5;
	
	public void loadFast(){		
		XmlResourceParser xpp = context.getResources().getXml(R.xml.exercises);
		mDb.beginTransaction();
		try {
			xpp.next();
			int eventType = xpp.getEventType();
			int state = STATE_NONE;
			long exercise_id = 0L;
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_DOCUMENT) {
					assert(state == STATE_NONE);
					state = STATE_DOCUMENT;
					// stringBuffer.append("--- Start XML ---");
				} else if (eventType == XmlPullParser.START_TAG) {
					assert(state != STATE_NONE);
					if(xpp.getName().equals("Exercises")){
						assert(state == STATE_DOCUMENT);
						state = STATE_EXERCISES;
					}else if (xpp.getName().equals("Exercise")){
						assert(state == STATE_EXERCISES);
						state = STATE_EXERCISE;
						final String exercise_name = xpp.getAttributeValue(null, "name");
						exercise_id = DbAdapter.DatabaseHelper.createExercise(exercise_name, mDb);
						Log.v(TAG, "exercise: " + exercise_name +  "(" +exercise_id  +")");
					}else if (xpp.getName().equals("MajorMuscles")){
						assert(state == STATE_EXERCISE);
						state = STATE_PRIMARY;
					}else if (xpp.getName().equals("SecondaryMuscles")){
						assert(state == STATE_EXERCISE);
						state = STATE_SECONDARY;						
					}else if (xpp.getName().equals("Muscle")){
						assert(state == STATE_PRIMARY || state == STATE_SECONDARY);
						final String muscle_name = xpp.getAttributeValue(null, "name");
						recordMuscle(exercise_id, state == STATE_PRIMARY, muscle_name);
						Log.v(TAG, "exercise muscle: " + muscle_name +  "(" + Boolean.toString(state == STATE_PRIMARY)  +")");
					}else if (xpp.getName().equals("image")){
						final String image = xpp.nextText().trim();
						Log.v(TAG, "image: " + image);
						recordImage(exercise_id, image);
					}
					// stringBuffer.append("\nSTART_TAG: "+xpp.getName());
				} else if (eventType == XmlPullParser.END_TAG) {
					if(xpp.getName().equals("Exercises")){
						assert(state == STATE_EXERCISES);
						state = STATE_DOCUMENT;
					}else if (xpp.getName().equals("Exercise")){
						assert(state == STATE_EXERCISE);
						state = STATE_EXERCISES;
					}else if (xpp.getName().equals("MajorMuscles")){
						assert(state == STATE_PRIMARY);
						state = STATE_EXERCISE;
					}else if (xpp.getName().equals("SecondaryMuscles")){
						assert(state == STATE_SECONDARY);
						state = STATE_EXERCISE;						
					}
				} else if (eventType == XmlPullParser.TEXT) {
					// stringBuffer.append("\nTEXT: "+xpp.getText());
				}
				eventType = xpp.next();
			}
			mDb.setTransactionSuccessful();
		} catch (XmlPullParserException e) {
			Log.e(TAG, "XmlPullParserException", e);
		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
		}finally{
			mDb.endTransaction();
		}
		
	}
	
	private void recordImage(long exerciseId, String image) {
		DbAdapter.DatabaseHelper.setImage(exerciseId, image, mDb);
	}

	public void load(){
		try {
			
			InputStream is = context.getAssets().open("exercises.xml");
			
	        DocumentBuilder builder=DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder();
	        
	        Document doc=builder.parse(is, null);
	        is.close();
	        
	        final Node root = doc.getDocumentElement();
	        
	        mDb.beginTransaction();
	        doProcessExercisesXML(root);
	        mDb.setTransactionSuccessful();
	        
		} catch (IOException e) {
			Log.e(TAG, "exercises.xml parse: IOException", e);
		} catch (SAXException e) {
			Log.e(TAG, "exercises.xml parse: SAXException", e);
		} catch (ParserConfigurationException e) {
			Log.e(TAG, "exercises.xml parse: ParserConfigurationException", e);
		} catch (FactoryConfigurationError e) {
			Log.e(TAG, "exercises.xml parse: FactoryConfigurationError", e);
		} catch (Exception e) {
			Log.e(TAG, "exercises.xml parse: Exception", e);			
		}finally{
			mDb.endTransaction();
		}
	}
	
	private void doProcessExercisesXML(Node root) throws Exception {
		final NodeList exercises = root.getChildNodes();
		for(int i=0; i<exercises.getLength();i++){
			final Node exercise_node = exercises.item(i);
			if(exercise_node.getNodeType() == Node.ELEMENT_NODE){	        		
				doProcessExercise(exercise_node);
			}	        	
		}
	}

	private void doProcessExercise(Node exercise_node) throws Exception {
		assert(exercise_node.getNodeName().equals("Exercise"));

		final String exercise_name = exercise_node.getAttributes().getNamedItem("name").getNodeValue();
		final long exercise_id = DbAdapter.DatabaseHelper.createExercise(exercise_name, mDb);
		
		Log.v(TAG, "exercise: " + exercise_name +  "(" +exercise_id  +")");
		
		
		final NodeList children = exercise_node.getChildNodes();
        for(int i=0; i<children.getLength();i++){
        	final Node child_node = children.item(i);
        	if(child_node.getNodeType() == Node.ELEMENT_NODE){
        		if(child_node.getNodeName().equals("MajorMuscles")){
        			doProcessMajorMuscles(child_node, exercise_id);
        		}else if(child_node.getNodeName().equals("SecondaryMuscles")){
        			doProcessSecondaryMuscles(child_node, exercise_id);
        		}else{
        			//throw new Exception("Unkown Exercise paramter: " + child_node.getNodeName());
        		}
        	}	        	
        }
	}
	
	private void doProcessSecondaryMuscles(Node muscles_node, long exercise_id) {
		doProcessMuscles(muscles_node, exercise_id, false);
	}

	private void doProcessMajorMuscles(Node muscles_node, long exercise_id) {
		doProcessMuscles(muscles_node, exercise_id, true);		
	}
	
	private void doProcessMuscles(Node muscles_node, long exercise_id, boolean primary) {
		final NodeList children = muscles_node.getChildNodes();
        for(int i=0; i<children.getLength();i++){
        	final Node muscle_node = children.item(i);
        	if(muscle_node.getNodeType() == Node.ELEMENT_NODE){        		
        		doProcessMuscle(muscle_node, exercise_id, primary);
        	}
        }
		
	}


	private void doProcessMuscle(Node muscle_node, long exercise_id,
			boolean primary) {
		assert(muscle_node.getNodeName().equals("Muscle"));
		String muscle_name = muscle_node.getAttributes().getNamedItem("name").getNodeValue();
		
		recordMuscle(exercise_id, primary, muscle_name);
		
	}

	private void recordMuscle(final long exercise_id, final boolean primary, final String muscle_name) {
		final long muscle_id = DbAdapter.DatabaseHelper.getMuscleFromName(muscle_name, this.mDb);
		assert(muscle_id > 0);
		final long muscleGroup = DbAdapter.DatabaseHelper.getMuscleGroupForMuscle(muscle_id, this.mDb);
		assert(muscleGroup > 0);
		
		DbAdapter.DatabaseHelper.recordMuscle(exercise_id, muscle_id, primary, mDb);
		
		if(primary){
			DbAdapter.DatabaseHelper.recordMuscleGroup(exercise_id, muscleGroup, mDb);
		}
	}	

}
