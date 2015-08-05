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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.alexrothberg.afitness.DbAdapter.DatabaseHelper;

public class MuscleLoader {
	private static final String TAG = "MuscleLoader";
	private final Context context;
	private final SQLiteDatabase mDb;
	
	public MuscleLoader(Context context, SQLiteDatabase mDb){
		this.context = context;
		
		this.mDb = mDb;
	}
	
	
	public void loadMuscles(){
		try {
			final InputStream is = context.getAssets().open("muscles.xml");
			
	        final DocumentBuilder builder=DocumentBuilderFactory
            .newInstance()
            .newDocumentBuilder();
	        
	        final Document doc=builder.parse(is, null);
	        is.close();
	        
	        final Node root = doc.getDocumentElement();
	        
	        mDb.beginTransaction();
	        doProcessMusclesXML(root);
	        mDb.setTransactionSuccessful();
	        
		} catch (IOException e) {
			Log.e(TAG, "muscles.xml parse: IOException", e);
		} catch (SAXException e) {
			Log.e(TAG, "muscles.xml parse: SAXException", e);
		} catch (ParserConfigurationException e) {
			Log.e(TAG, "muscles.xml parse: ParserConfigurationException", e);
		} catch (FactoryConfigurationError e) {
			Log.e(TAG, "muscles.xml parse: FactoryConfigurationError", e);
		}finally{
			mDb.endTransaction();
		}
	}
	
	private void doProcessMusclesXML(Node root) {
		NodeList muscleGroups = root.getChildNodes();
		
		for(int i=0; i<muscleGroups.getLength();i++){
			Node muscleGroup_node = muscleGroups.item(i);
			if(muscleGroup_node.getNodeType() == Node.ELEMENT_NODE){				
				doProcessMuscleGroup(muscleGroup_node);
			}	        	
		}
	}

	private void doProcessMuscleGroup(Node muscleGroup_node) {
		assert(muscleGroup_node.getNodeName().equals("MuscleGroup"));
		
		String muscleGroup_name = muscleGroup_node.getAttributes().getNamedItem("name").getNodeValue();
		long muscleGroup_id = ensureMuscleGroupExists(muscleGroup_name);
		
		Log.v(TAG, "muscleGroup_node: " + muscleGroup_name + "(" +muscleGroup_id  +")");
		NodeList muscles = muscleGroup_node.getChildNodes();
        for(int i=0; i<muscles.getLength();i++){
        	Node muscle_node = muscles.item(i);
        	if(muscle_node.getNodeType() == Node.ELEMENT_NODE){				
        		doProcessMuscle(muscle_node, muscleGroup_id);
        	}	        	
        }
	}
	
	private long ensureMuscleGroupExists(String muscleGroup_name) {
		return DatabaseHelper.createMuscleGroup(muscleGroup_name, mDb);
	}

	private void doProcessMuscle(Node muscleNode, long muscleGroup_id) {
		assert(muscleNode.getNodeName().equals("Muscle"));
		
		final String muscle_name = muscleNode.getAttributes().getNamedItem("name").getNodeValue();
		long muscle_id = ensureMuscleExists(muscle_name, muscleGroup_id);
		Log.v(TAG, "muscleNode: " + muscle_name +"(" + muscle_id +")");	
	}

	private long ensureMuscleExists(String muscle_name, long muscleGroup_id) {
		return DatabaseHelper.createMuscle(muscle_name, muscleGroup_id, mDb);
	}
	

}
