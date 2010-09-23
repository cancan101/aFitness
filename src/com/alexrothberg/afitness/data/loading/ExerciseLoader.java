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

import com.alexrothberg.afitness.DbAdapter;
import com.alexrothberg.afitness.DbAdapter.DatabaseHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ExerciseLoader {
	private static final String TAG = "ExerciseLoader";
	private final Context context;
	private final SQLiteDatabase mDb;
	
	public ExerciseLoader(Context context, SQLiteDatabase mDb){
		this.context = context;
		
		this.mDb = mDb;
	}
	
	
	public void load(){
		try {
			InputStream is = context.getAssets().open("exercises.xml");
			
	        DocumentBuilder builder=DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder();
	        
	        Document doc=builder.parse(is, null);
	        
	        Node root = doc.getDocumentElement();
	        
	        doProcessExercisesXML(root);
	        
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
		}		
	}
	
	private void doProcessExercisesXML(Node root) throws Exception {
		NodeList exercises = root.getChildNodes();
		
		for(int i=0; i<exercises.getLength();i++){
			Node exercise_node = exercises.item(i);
			if(exercise_node.getNodeType() == Node.ELEMENT_NODE){	        		
				doProcessExercise(exercise_node);
			}	        	
		}
	}

	private void doProcessExercise(Node exercise_node) throws Exception {
		assert(exercise_node.getNodeName().equals("Exercise"));

		String exercise_name = exercise_node.getAttributes().getNamedItem("name").getNodeValue();
		long exercise_id = DbAdapter.DatabaseHelper.createExercise(exercise_name, mDb);
		
		Log.v(TAG, "exercise: " + exercise_name +  "(" +exercise_id  +")");
		
		
		NodeList children = exercise_node.getChildNodes();
        for(int i=0; i<children.getLength();i++){
        	Node child_node = children.item(i);
        	if(child_node.getNodeType() == Node.ELEMENT_NODE){
        		if(child_node.getNodeName().equals("MajorMuscles")){
        			doProcessMajorMuscles(child_node, exercise_id);
        		}else if(child_node.getNodeName().equals("SecondaryMuscles")){
        			doProcessSecondaryMuscles(child_node, exercise_id);
        		}else{
        			throw new Exception("Unkown Exercise paramter: " + child_node.getNodeName());
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
		NodeList children = muscles_node.getChildNodes();
        for(int i=0; i<children.getLength();i++){
        	Node muscle_node = children.item(i);
        	if(muscle_node.getNodeType() == Node.ELEMENT_NODE){        		
        		doProcessMuscle(muscle_node, exercise_id, primary);
        	}
        }
		
	}


	private void doProcessMuscle(Node muscle_node, long exercise_id,
			boolean primary) {
		assert(muscle_node.getNodeName().equals("Muscle"));
		String muscle_name = muscle_node.getAttributes().getNamedItem("name").getNodeValue();
		
		long muscle_id = DbAdapter.DatabaseHelper.getMuscleFromName(muscle_name, this.mDb);
		long muscleGroup = DbAdapter.DatabaseHelper.getMuscleGroupForMuscle(muscle_id, this.mDb);
		
		DbAdapter.DatabaseHelper.recordMuscle(exercise_id, muscle_id, primary, mDb);
		
		if(primary){
			DbAdapter.DatabaseHelper.recordMuscleGroup(exercise_id, muscleGroup, mDb);
		}
		
	}



	

}
