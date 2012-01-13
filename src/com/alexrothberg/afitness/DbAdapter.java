package com.alexrothberg.afitness;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.util.Log;

import com.alexrothberg.afitness.data.loading.ExerciseLoader;
import com.alexrothberg.afitness.data.loading.MuscleLoader;


public class DbAdapter {
	public static final String ENTRY_ID = "entry_id";
	
	public static final class MuscleGroups implements BaseColumns{
		private MuscleGroups(){}
			
		public static final String DATABASE_TABLE = "muscle_groups";
		
		public static final String KEY_NAME = "name";
		
	    public static final String DATABASE_CREATE = String.format(
	    		"create table %s ("+
            		"%s integer primary key autoincrement, "+
                    "%s text not null);"
        		, DATABASE_TABLE, _ID, KEY_NAME);		
	}
	
	public static final class Muscles implements BaseColumns{	
		private Muscles(){}
		
		public static final String DATABASE_TABLE = "muscles";
		
		public static final String KEY_NAME = "name";
		public static final String KEY_MUSCLE_GROUP_ID = "musclegroup_id";
		
		public static final String DATABASE_CREATE = String.format(
				"create table %s (" +
					"%s integer primary key autoincrement, " +
					"%s string not null, " +
					"%s integer not null);"
				, DATABASE_TABLE, _ID, KEY_NAME, KEY_MUSCLE_GROUP_ID);
	
	}
	
	public static final class Equipments implements BaseColumns{
		private Equipments(){}
		
		private static final String DATABASE_TABLE = "equipments";
		
		public static final String KEY_NAME = "name";

		public static final String DATABASE_CREATE = String.format(
				"create table %s (" +
					"%s integer primary key autoincrement, " +
					"%s text not null);"
				, DATABASE_TABLE, _ID, KEY_NAME);
	}

    public static final class Workouts implements BaseColumns {
    	private Workouts() {}
    	
		public static final String DATABASE_TABLE = "workouts";
		
	    public static final String KEY_NAME = "name";

	    public static final String DATABASE_CREATE = String.format(
	    		"create table %s (" +
		    		"%s integer primary key autoincrement, " +
	                "%s text not null);"
	    		, DATABASE_TABLE, _ID, KEY_NAME);
    	
    }
    
	public static final class Activities implements BaseColumns {
		private Activities(){}
		
		public static final String DATABASE_TABLE = "activities";
	    
	    public static final String KEY_WEIGHT = "weight";
	    public static final String KEY_REPS = "reps";
	    public static final String KEY_EXERCISE = "exercise_id";
	    public static final String KEY_RECORD_DATE = "record_date";
	    public static final String KEY_UNITS = "units";

	    public static final String DATABASE_CREATE = String.format(
	            "create table %s (" + 
		    		"%s integer primary key autoincrement, " +
		            "%s integer not null, " +
		            "%s integer not null, " +
		            "%s integer not null, "+
		            "%s integer not null, "+
		            "%s real not null);"
	            , DATABASE_TABLE, _ID, KEY_EXERCISE, KEY_RECORD_DATE, KEY_REPS, KEY_UNITS, KEY_WEIGHT);
		
	}
	
	public static final class Exercises implements BaseColumns {
		private Exercises(){}
		public static final String DATABASE_TABLE = "exercises";
		
		public static final String KEY_NAME = "name";
		public static final String KEY_IMAGE = "image";
		public static final String KEY_UNIQUE_ID = "guid";
		
		
		public static final String DATABASE_CREATE = String.format(
				"create table %s (" +
					"%s integer primary key autoincrement, " +
					"%s text not null," +
					"%s integer not null," +
					"%s text);"
				, DATABASE_TABLE, _ID, KEY_NAME, KEY_UNIQUE_ID, KEY_IMAGE);
		
		public static final String DATABSE_ADD_IMAGE = String.format(
				"ALTER TABLE %s ADD COLUMN %s text", DATABASE_TABLE, KEY_IMAGE);
		
	}
	
	public static final class WorkoutExercises implements BaseColumns {
		private WorkoutExercises(){}
		public static final String DATABASE_TABLE = "workout_exercises";
		
		public static final String KEY_WORKOUT = "workout_id";
		public static final String KEY_EXERCISE = "exercise_id";
		
		public static final String DATABASE_CREATE = String.format(
				"create table %s (" +
					"%s integer primary key autoincrement, " +
					"%s integer not null, " +
					"%s integer not null);"
				, DATABASE_TABLE, _ID, KEY_WORKOUT, KEY_EXERCISE);
	}
	
	public static final class ExerciseMuscleGroups implements BaseColumns {
		private ExerciseMuscleGroups(){}
		public static final String DATABASE_TABLE = "exercises_musclegroups";
		
		public static final String KEY_MUSCLE_GROUP = "musclegroup_id";
		public static final String KEY_EXERCISE = "exercise_id";
		
		public static final String DATABASE_CREATE = String.format(
				"create table %s (" +
					"%s integer primary key autoincrement, " +
					"%s integer not null, " +
					"%s integer not null);"
				, DATABASE_TABLE, _ID, KEY_MUSCLE_GROUP, KEY_EXERCISE);
	}
	
	public static final class ExerciseMuscles implements BaseColumns {
		private ExerciseMuscles(){}
		public static final String DATABASE_TABLE = "exercises_muscles";
		
		public static final String KEY_MUSCLE = "muscle_id";
		public static final String KEY_EXERCISE = "exercise_id";
		public static final String KEY_ISPRIMARY = "is_primary";
		
		public static final String DATABASE_CREATE = String.format(
				"create table %s (" +
					"%s integer primary key autoincrement, " +
					"%s integer not null, " +
					"%s integer not null, " +
					"%s integer not null);"
				, DATABASE_TABLE, _ID, KEY_MUSCLE, KEY_EXERCISE, KEY_ISPRIMARY);
	}

    public static final String DATABASE_NAME = "data";
    public static final int DATABASE_VERSION = 42;
    
    private static final String TAG = "DbAdapter";
    
    private DatabaseHelper mDbHelper;
    private final Context mCtx;
    private SQLiteDatabase mDb; 
    
    
	public static class DatabaseHelper extends SQLiteOpenHelper {
		protected final Context context;
		
		private final static ContentValues values  = new ContentValues();
		
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			this.context=context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
//			ProgressDialog dialog = ProgressDialog.show(context, "", 
//                    "First time app used. Loading data. Please wait...", true);
			createAllTables(db);
			
            loadInitialValues(db);
//            dialog.dismiss();
		}

		private void loadInitialValues(SQLiteDatabase db) {
			new MuscleLoader(this.context, db).loadMuscles();
            
            new ExerciseLoader(context, db).loadFast();
            
            insertDummyData(db);
		}

		private void createAllTables(SQLiteDatabase db) {
			Log.v(TAG, "Creating tables");
			
			db.execSQL(MuscleGroups.DATABASE_CREATE);
			db.execSQL(Exercises.DATABASE_CREATE);
			db.execSQL(Equipments.DATABASE_CREATE);
			db.execSQL(Muscles.DATABASE_CREATE);
			db.execSQL(ExerciseMuscleGroups.DATABASE_CREATE);
			db.execSQL(ExerciseMuscles.DATABASE_CREATE);
            db.execSQL(Workouts.DATABASE_CREATE);
			db.execSQL(WorkoutExercises.DATABASE_CREATE);
            db.execSQL(Activities.DATABASE_CREATE);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion);
            
//			ProgressDialog dialog = ProgressDialog.show(context, "", 
//                    "Upgrading databases. Please wait...", true);
            
            if(oldVersion >= 15 && oldVersion <= 41){
            	if(oldVersion<=31){
            		db.execSQL(Exercises.DATABSE_ADD_IMAGE);
            	}
            	noDrop(db);
            }else{
            	throw new RuntimeException("Bad versions!");
//            	drop(db);
            }         
//            dialog.dismiss();
            			
		}
		
		private void noDrop(SQLiteDatabase db) {
			 loadInitialValues(db);
		}

		private void drop(SQLiteDatabase db) {
            // Do these in the reverse order of the create
            db.execSQL("DROP TABLE IF EXISTS " + Activities.DATABASE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + WorkoutExercises.DATABASE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + Workouts.DATABASE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + ExerciseMuscles.DATABASE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + ExerciseMuscleGroups.DATABASE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + Muscles.DATABASE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + Equipments.DATABASE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + Exercises.DATABASE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + MuscleGroups.DATABASE_TABLE);
            
            onCreate(db);
			
		}

		private static long containsExercises(int hashCode, SQLiteDatabase db){
			final Cursor c = db.query(Exercises.DATABASE_TABLE, new String[]{ Exercises._ID}, Exercises.KEY_UNIQUE_ID + "=?", new String[]{hashCode +""}, null, null, null);
			int count = c.getCount();
			
			assert(count <= 1);
			
			final long ret;
			if(count==0){
				ret = -1;
			}else{
				c.moveToFirst();
				ret = c.getLong(0); //We can hardcode the index here
			}
			c.deactivate();
			return ret;
		}
		
		public static long containsMuscleGroup(String muscleGroup_name, SQLiteDatabase db){
			
			Cursor c = db.query(MuscleGroups.DATABASE_TABLE, new String[]{ MuscleGroups._ID}, MuscleGroups.KEY_NAME + "=?", new String[]{muscleGroup_name}, null, null, null);
			int count = c.getCount();
			
			assert(count <= 1);
			
			final long ret;
			if(count==0){
				ret = -1;
			}else{
				c.moveToFirst();
				ret = c.getLong(0); //We can hardcode the index here
			}
			c.deactivate();
			return ret;
		}
		
		public static long getMuscleFromName(final String muscle_name, final SQLiteDatabase db){
			return containsMuscle( muscle_name, -1, db);
		}
		
		private static Map<String, Long> muscle_name_cache = new HashMap<String, Long>();		
		public static long containsMuscle(final String muscle_name, long muscleGroup_id, final SQLiteDatabase db){
			final Long cached_value = muscle_name_cache.get(muscle_name);
			if(cached_value != null){
				Log.v(TAG,"hit: " + muscle_name +"=" + cached_value);
				return cached_value;
			}else{
				Log.v(TAG,"nohit: " + muscle_name);
				final Cursor c = db.query(Muscles.DATABASE_TABLE, new String[]{ Muscles._ID, Muscles.KEY_MUSCLE_GROUP_ID}, "UPPER(" + Muscles.KEY_NAME + ")=?", new String[]{muscle_name.toUpperCase()}, null, null, null);
				final int count = c.getCount();
				
				assert(count <= 1);
				
				final long ret;
				if(count==0){
					ret = -1;
				}else{
					c.moveToFirst();
					if(muscleGroup_id != -1){
						assert(c.getLong(1) == muscleGroup_id);
					}
					ret = c.getLong(0); //We can hardcode the index here
				}
				c.deactivate();
				if(ret != -1){
					muscle_name_cache.put(muscle_name, ret);
				}
				return ret;
			}
		}
		
		
	    public static long createExercise(String name, SQLiteDatabase db){
	    	return createExercise(name, null, db);
	    }
	    
	    public static long createExercise(String name, String image, SQLiteDatabase db){
	    	final int hashCode = name.hashCode();
	    	
	    	final long oldValue = containsExercises(hashCode, db);
	    	if(oldValue != -1){
	    		return oldValue;
	    	}else{	    	
		    	values.clear();
		    	values.put(Exercises.KEY_NAME, name);
		    	values.put(Exercises.KEY_UNIQUE_ID, hashCode);
		    	values.put(Exercises.KEY_IMAGE, image);
		    	return db.insert(Exercises.DATABASE_TABLE, null, values);
	    	}
	    }
	    
	    public static int setImage(long exercise_id, String image, SQLiteDatabase db){
	    	values.clear();
	    	values.put(Exercises.KEY_IMAGE, image);
	    	return db.update(Exercises.DATABASE_TABLE, values, Exercises._ID+"=?", new String[]{ Long.toString(exercise_id) });
	    }
	    
	    
	    public static long createEquipment(String name, SQLiteDatabase db){
	    	values.clear();
	    	values.put(Equipments.KEY_NAME, name);
	    	return db.insert(Equipments.DATABASE_TABLE, null, values);
	    }
	    
	    public static long createWorkout(String name, SQLiteDatabase db) {
	    	values.clear();
	        values.put(Workouts.KEY_NAME, name);

	        return db.insert(Workouts.DATABASE_TABLE, null, values);
	    }
	    
	    public static long renameWorkout(long workout_id, String new_name, SQLiteDatabase db) {
	    	values.clear();
	        values.put(Workouts.KEY_NAME, new_name);
	        
	        return db.update(Workouts.DATABASE_TABLE, values, Workouts._ID + "= ?", new String[]{workout_id + ""});
	    }
	    
	    public static long addWorkoutExercise(long workout_id, long exercise_id, SQLiteDatabase db){
	    	values.clear();
	    	values.put(WorkoutExercises.KEY_WORKOUT, workout_id);
	    	values.put(WorkoutExercises.KEY_EXERCISE, exercise_id);
	    	return db.insert(WorkoutExercises.DATABASE_TABLE, null, values);
	    }
	    

	    
		public static long addExerciseEquipment(long exercise, long equipment, SQLiteDatabase db) {
			//TODO
			return 0L;
		}

		private static long exerciseContainsMuscleGroup(long exercise, long muscleGroup, SQLiteDatabase db){
			final Cursor cursor = db.query(
					ExerciseMuscleGroups.DATABASE_TABLE,
					new String[]{ ExerciseMuscleGroups._ID },
					ExerciseMuscleGroups.KEY_EXERCISE + "=?" + 
						"AND " + ExerciseMuscleGroups.KEY_MUSCLE_GROUP + "=?",
					new String[]{ 	
									Long.toString(exercise), 
									Long.toString(muscleGroup) 
								}, 
					null, // groupBy
					null, //having
					null //orderBy
					);
			
			final long ret;
			if (!cursor.moveToFirst()){
				ret = 0;
			}else{
				ret = cursor.getLong(0);
				if (cursor.moveToNext()){
					Log.v(TAG, "exerciseContainsMuscleGroup: containts multiple entries");
				}
			}
			cursor.deactivate();
			return ret;			
		}
		
		public static long recordMuscleGroup(long exercise, long muscleGroup_id, SQLiteDatabase db){
			if (muscleGroup_id <=0){
				throw new RuntimeException("muscleGroup_id=" + muscleGroup_id);
			}
			final long existing = exerciseContainsMuscleGroup(exercise, muscleGroup_id, db);
			if (existing > 0){
				return existing;
			}
			
			values.clear();
			values.put(ExerciseMuscleGroups.KEY_EXERCISE, exercise);
			values.put(ExerciseMuscleGroups.KEY_MUSCLE_GROUP, muscleGroup_id);
		
			return db.insert(ExerciseMuscleGroups.DATABASE_TABLE, null, values);			
		}

		private static long exerciseContainsMuscle(long exercise, long muscle, SQLiteDatabase db){
			final Cursor cursor = db.query(
					ExerciseMuscles.DATABASE_TABLE,
					new String[]{ ExerciseMuscles._ID },
						ExerciseMuscles.KEY_EXERCISE + "=?" + 
						"AND " + ExerciseMuscles.KEY_MUSCLE + "=?",
					new String[]{ 	
									Long.toString(exercise), 
									Long.toString(muscle) 
								}, 
					null, // groupBy
					null, //having
					null //orderBy
					);
			
			final long ret;
			if (!cursor.moveToFirst()){
				ret = 0;
			}else{
				ret = cursor.getLong(0);
				if (cursor.moveToNext()){
					Log.v(TAG, "exerciseContainsMuscle: containts multiple entries");
				}
			}
			cursor.deactivate();
			return ret;			
		}
		
		public static long recordMuscle(long exercise, long muscle, boolean isPrimary, SQLiteDatabase db){
			final long existing = exerciseContainsMuscle(exercise, muscle, db);
			if (existing > 0){
				return existing;
			}
			
			values.clear();
			values.put(ExerciseMuscles.KEY_EXERCISE, exercise);
			values.put(ExerciseMuscles.KEY_MUSCLE, muscle);
			values.put(ExerciseMuscles.KEY_ISPRIMARY, isPrimary);
			 
			return db.insert(ExerciseMuscles.DATABASE_TABLE, null, values);			
		}
		
		public static long recordSecondaryMuscle(long exercise, long muscle, SQLiteDatabase db){
			return recordMuscle(exercise, muscle, false, db);
		}
		
		public static long recordPrimaryMuscle(long exercise, long muscle, SQLiteDatabase db){
			return recordMuscle(exercise, muscle, true, db);			
		}
		
		
		private static long getMuscleGroupByName(String muscleGroup_name, SQLiteDatabase db){
			final Cursor cursor =  db.query(
					MuscleGroups.DATABASE_TABLE, 
					new String[]{MuscleGroups._ID}, 
					MuscleGroups.KEY_NAME + " = ?", 
					new String[]{muscleGroup_name}, null, null, null);
			
			final long ret;
			if (!cursor.moveToFirst()){
				ret = -1;
			}else{
				ret = cursor.getLong(0);
			}
			cursor.deactivate();
			return ret;
		}

		public static long createMuscleGroup(String muscleGroup_name, SQLiteDatabase db) {
			long oldValue = containsMuscleGroup(muscleGroup_name, db);
			
			if(oldValue != -1){
				return oldValue;
			}else{
			
		    	 values.clear();
		    	 values.put(MuscleGroups.KEY_NAME, muscleGroup_name);
	
		    	 return db.insert(MuscleGroups.DATABASE_TABLE, null, values);
			}
		}
		
	    public static long createMuscle(String muscle_name, long muscleGroup_id, SQLiteDatabase db) {
	    	long oldValue = containsMuscle(muscle_name, muscleGroup_id, db);
	    	
			if(oldValue != -1){
				return oldValue;
			}else{	    	
				values.clear();
				values.put(Muscles.KEY_NAME, muscle_name);
				values.put(Muscles.KEY_MUSCLE_GROUP_ID, muscleGroup_id);
				return db.insert(Muscles.DATABASE_TABLE, null, values);
			}
		}
	    
	    private static Map<Long, Long> muscle_to_group_cache = new HashMap<Long, Long>();
	    public static long getMuscleGroupForMuscle(long muscle_id, SQLiteDatabase db){
	    	final Long cached = muscle_to_group_cache.get(muscle_id);
	    	if(cached != null){
	    		return cached;
	    	}
	    	
	    	final Cursor cursor = db.query(
	    			Muscles.DATABASE_TABLE, 
	    			new String[]{ Muscles.KEY_MUSCLE_GROUP_ID }, 
	    			Muscles._ID +"=?", 
	    			new String[]{ muscle_id +""}, 
	    			null, 
	    			null, //having
	    			null //orderBy
	    			);
	    	final long ret;
	    	int rows = cursor.getCount();
	    	assert (rows <= 1);
	    	if(rows == 0){
	    		ret = -1;
	    	}else{
	    		cursor.moveToFirst();
	    		ret = cursor.getLong(0);	
	    		muscle_to_group_cache.put(muscle_id, ret);
	    	}
	    	cursor.deactivate();
	    	
	    	return ret;
	    }

		private void insertDummyData(SQLiteDatabase db) {

//			
//			long upperBody = createWorkout("Upper Body", db);
//			long lowerBody = createWorkout("Lower Body", db);
//			
//			addWorkoutExercise(lowerBody, squat, db);
//			addWorkoutExercise(upperBody, pullups, db);
//			addWorkoutExercise(upperBody, benchPress, db);
//			
//			createEquipment("Dumbbells", db);
//			createEquipment("Swiss Ball", db);
//			long dipMachine = createEquipment("Dip Machine", db);
//
//			long flatBench = createEquipment("Flat Bench", db);
//			
//			addExerciseEquipment(dips, dipMachine, db);
//			addExerciseEquipment(benchPress, flatBench, db);
//
			
		}


	}
    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public DbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

	/**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public DbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        mDbHelper.close();
    } 
    
    public Cursor fetchAllWorkouts(){
        return mDb.query(
        		Workouts.DATABASE_TABLE, 
        		new String[] {
        			Workouts._ID, 
        			Workouts.KEY_NAME}, 
        		null, null, null, null, null);
    }
    
    
    public Cursor fetchAllMuscleGroups(){
        return mDb.query(
        		MuscleGroups.DATABASE_TABLE, 
        		new String[] {
        				MuscleGroups._ID, 
        				MuscleGroups.KEY_NAME}, 
        		null, null, null, null, MuscleGroups.KEY_NAME);
    }
    
    public long createWorkout(String name) {
    	return DatabaseHelper.createWorkout(name, mDb);
    }
    
    public long renameWorkout(long id, String name){
    	return DatabaseHelper.renameWorkout(id, name, mDb);
    }
   
    public Cursor fetchAllActivities(long exercise_id, Date record_date){
        return mDb.query(
        		Activities.DATABASE_TABLE, 
        		new String[] {
        			Activities._ID, 
        			Activities.KEY_REPS,
        			Activities.KEY_WEIGHT,
        			Activities.KEY_UNITS}, 
        		Activities.KEY_EXERCISE + "=" + exercise_id + " and " + Activities.KEY_RECORD_DATE + "=" + record_date.getTime(), //where
        		null, null, null, null);
    }
    
    public enum UNITS{
    	LBS,
    	KGS,
    	PLATES,
    }
    
    public long recordActivity(long exercise_id, Date record_date, int reps, float weight, UNITS units) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(Activities.KEY_REPS, reps);
        initialValues.put(Activities.KEY_EXERCISE, exercise_id);
        initialValues.put(Activities.KEY_WEIGHT, weight);
        initialValues.put(Activities.KEY_RECORD_DATE, record_date.getTime());
        initialValues.put(Activities.KEY_UNITS, units.ordinal());
        return mDb.insert(Activities.DATABASE_TABLE, null, initialValues);
    }
    
    public Cursor fetchExercisesForWorkout(long workout_id){
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(WorkoutExercises.DATABASE_TABLE + " we join " + Exercises.DATABASE_TABLE + " e on we." + WorkoutExercises.KEY_EXERCISE + " = e." + Exercises._ID);
		return builder.query(
				mDb, 
				new String[] {
							"e." + Exercises._ID + " as " + Exercises._ID, 
							"we." + WorkoutExercises._ID + " as " + ENTRY_ID, 
							"we." + WorkoutExercises.KEY_EXERCISE + " as " + WorkoutExercises.KEY_EXERCISE, 
							"e." + Exercises.KEY_NAME + " as " + Exercises.KEY_NAME,
							Exercises.KEY_IMAGE
						}, 
						"we." + WorkoutExercises.KEY_WORKOUT + "=?", 
						new String[]{ Long.toString(workout_id) }, 
						null, 
						null, 
						null
						);
    }
    
    public long createExercise(String name){
    	return DatabaseHelper.createExercise(name, mDb);
    }

	public Cursor fetchAllExercises() {
		return mDb.query(
        		Exercises.DATABASE_TABLE, 
        		new String[] {
        				Exercises._ID, 
        				Exercises.KEY_NAME,
    					Exercises.KEY_IMAGE
        				}
        		, null, null, null, null, 
        		Exercises.KEY_NAME + " COLLATE NOCASE" //orderBy
        		);
	}
	
    public Cursor fetchExercisesForMuscleGroup(long musclegroup_id){
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(ExerciseMuscleGroups.DATABASE_TABLE + " emg join " + Exercises.DATABASE_TABLE + " e on emg." + ExerciseMuscleGroups.KEY_EXERCISE + " = e." + Exercises._ID);
		return builder.query(mDb, 
				new String[] {
					"e." + Exercises._ID + " as " + Exercises._ID, 
					"emg." + ExerciseMuscleGroups.KEY_EXERCISE + " as " + ExerciseMuscleGroups.KEY_EXERCISE, 
					"e." + Exercises.KEY_NAME + " as " + Exercises.KEY_NAME,
					Exercises.KEY_NAME,
					Exercises.KEY_IMAGE,
				}, 
				"emg." + ExerciseMuscleGroups.KEY_MUSCLE_GROUP + "=?", 
				new String[]{Long.toString(musclegroup_id)}, 
				null, 
				null, 
				Exercises.KEY_NAME + " COLLATE NOCASE" //orderBy
				);
    }
    
    public Cursor fetchExercisesForMuscle(long muscle_id){
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(ExerciseMuscles.DATABASE_TABLE + " em join " + Exercises.DATABASE_TABLE + " e on em." + ExerciseMuscles.KEY_EXERCISE + " = e." + Exercises._ID);
		return builder.query(
				mDb, 
				new String[] {
						"e." + Exercises._ID + " as " + Exercises._ID, 
						"em." + ExerciseMuscles.KEY_EXERCISE + " as " + ExerciseMuscles.KEY_EXERCISE, 
						"e." + Exercises.KEY_NAME + " as " + Exercises.KEY_NAME,
						Exercises.KEY_NAME,
						Exercises.KEY_IMAGE,
				}, 
				"em." + ExerciseMuscles.KEY_MUSCLE + " = ?", 
				new String[]{Long.toString(muscle_id)}, 
				null, 
				null, 
				Exercises.KEY_NAME + " COLLATE NOCASE" //orderBy
				);
    }
    
    public Cursor fetchMusclesForMuscleGroup(long musclegroup_id){
    	return mDb.query(
    			Muscles.DATABASE_TABLE, 
    			new String[]{ 
    					Muscles._ID,
    					Muscles.KEY_NAME,
    			}, 
    			Muscles.KEY_MUSCLE_GROUP_ID + " = ?",
    			new String[]{ musclegroup_id+""}, 
    			null, 
    			null, 
    			Muscles.KEY_NAME
    			);
    }    

    
    public Cursor fetchAllMuscles(){
    	return mDb.query(
    			Muscles.DATABASE_TABLE, 
    			new String[]{ 
    					Muscles._ID,
    					Muscles.KEY_NAME,
    			}, null, null, null, null, 
    			Muscles.KEY_NAME
    			);    	
    }
    
    /**
     * Responsible for deleting a workout and any associated exercises for this workout.
     * 
     * @param workoutExerciseId
     * @return the number of rows deleted
     */
	public int deleteWorkout(long workoutExerciseId) {
		int deleted = 0;
		deleted += mDb.delete(WorkoutExercises.DATABASE_TABLE, WorkoutExercises._ID + "=?", new String[]{workoutExerciseId +""});		
		deleted += mDb.delete(Workouts.DATABASE_TABLE, Workouts._ID + "=?", new String[]{workoutExerciseId +""});
		return deleted;
	}

	public Cursor getWorkoutDates(long exerciseId) {
		return mDb.query(
				true, 
				Activities.DATABASE_TABLE, 
				new String[]{Activities.KEY_RECORD_DATE, Activities.KEY_RECORD_DATE + " as _id" }, 
				Activities.KEY_EXERCISE + "=?", 
				new String[]{ exerciseId +""},
				null, null, Activities.KEY_RECORD_DATE + " desc", null);		
	}
	
    public long addWorkoutExercise(long workout_id, long exercise_id){
    	return DatabaseHelper.addWorkoutExercise(workout_id, exercise_id, mDb);
    }

	public int deleteWorkoutExercise(long workoutExerciseId) {
		return mDb.delete(WorkoutExercises.DATABASE_TABLE, WorkoutExercises._ID + "=?", new String[]{workoutExerciseId +""});		
	}
	
	public long createMuscleGroup(String muscleGroup_name){
		return DatabaseHelper.createMuscleGroup(muscleGroup_name, mDb);
	}
	
	public long createMuscle(String muscle_name, long muscleGroup_id){
		return DatabaseHelper.createMuscle(muscle_name, muscleGroup_id, mDb);
	}
	
	public long recordPrimaryMuscle(long exercise_id, long muscle_id){
		return DatabaseHelper.recordPrimaryMuscle(exercise_id, muscle_id, this.mDb);
	}
	
	public long recordMuscleGroup(long exercise_id, long muscleGroup_id){
		return DatabaseHelper.recordMuscleGroup(exercise_id, muscleGroup_id, this.mDb);
	}
	
	public long getMuscleGroupForMuscle(long muscle_id){
		return DatabaseHelper.getMuscleGroupForMuscle(muscle_id, this.mDb);
	}
	
	public int deleteActivity(long activity_id){
		return mDb.delete(Activities.DATABASE_TABLE, Activities._ID + "=?", new String[]{Long.toString(activity_id)});				
	}
	
	// TODO: Use cache from DebugActivity
	public String getExerciseName(long exercise_id){
    	Cursor cursor = mDb.query(
    			Exercises.DATABASE_TABLE, 
    			new String[]{ Exercises.KEY_NAME }, 
    			Exercises._ID +"=?", 
    			new String[]{ exercise_id +""}, 
    			null, 
    			null, //having
    			null //orderBy
    			);
    	int rows = cursor.getCount();
    	assert (rows <= 1);
    	
    	final String ret;
    	if(rows == 0){
    		ret = null;
    	}else{
    		cursor.moveToFirst();
    		ret = cursor.getString(0);
    	}
    	cursor.deactivate();
    	return ret;
	}

	public int deleteExercise(long exerciseId) {
		int deleted = 0;
		deleted += deleteAllActivitiesForExercise(exerciseId);
		deleted += removeExerciseFromWorkouts(exerciseId);
		deleted += removeMuscleEntriesForExercise(exerciseId);
		deleted += removeMuscleGroupForExercise(exerciseId);
		
		deleted += deleteExerciseActual(exerciseId);
		return deleted;
		
	}

	// TODO: Invalidate name cache
	private int deleteAllActivitiesForExercise(long exerciseId) {
		return mDb.delete(Activities.DATABASE_TABLE, Activities.KEY_EXERCISE + "=?", new String[]{Long.toString(exerciseId)});		
	}

	private int removeExerciseFromWorkouts(long exerciseId) {
		return mDb.delete(WorkoutExercises.DATABASE_TABLE, WorkoutExercises.KEY_EXERCISE + "=?", new String[]{Long.toString(exerciseId)});				
	}

	private int removeMuscleEntriesForExercise(long exerciseId) {
		return mDb.delete(ExerciseMuscles.DATABASE_TABLE, ExerciseMuscles.KEY_EXERCISE + "=?", new String[]{Long.toString(exerciseId)});			
	}

	private int removeMuscleGroupForExercise(long exerciseId) {
		return mDb.delete(ExerciseMuscleGroups.DATABASE_TABLE, ExerciseMuscleGroups.KEY_EXERCISE + "=?", new String[]{Long.toString(exerciseId)});		
	}

	private int deleteExerciseActual(long exerciseId) {
		return mDb.delete(Exercises.DATABASE_TABLE, Exercises._ID + "=?", new String[]{Long.toString(exerciseId)});			
	}

	public Cursor getAllActivities() {
    	Cursor cursor = mDb.query(
    			Activities.DATABASE_TABLE, 
    			new String[]{ Activities.KEY_EXERCISE, Activities.KEY_RECORD_DATE, Activities.KEY_WEIGHT, Activities.KEY_REPS  }, 
    			null, 
    			null, 
    			null, 
    			null, //having
    			Activities.KEY_RECORD_DATE + ", " + Activities.KEY_EXERCISE + ", " + Activities._ID// order by
    			);
    	return cursor;
	}

	public Cursor getAllWorkoutDates() {
		return mDb.query(
    			Activities.DATABASE_TABLE, 
    			new String[]{ 	Activities.KEY_RECORD_DATE + " as " + BaseColumns._ID, 
    							"count( distinct " + Activities.KEY_EXERCISE + " ) as exercise_count", 
    							"count( * ) as activity_count" }, 
    			null, 
    			null, 
    			Activities.KEY_RECORD_DATE, 
    			null, //having
    			Activities.KEY_RECORD_DATE + " desc" //orderBy
    			);
	}

	public Cursor getAlExerciseOn(long selectedDateLong) {
		return mDb.query(
    			Activities.DATABASE_TABLE + " a join " + Exercises.DATABASE_TABLE + " x on a." + Activities.KEY_EXERCISE + " = x." + Exercises._ID, 
    			new String[]{ "a." + Activities.KEY_EXERCISE + " as " + Exercises._ID, "x." + Exercises.KEY_NAME + " as " + Exercises.KEY_NAME, "count( * ) as activity_count" }, 
    			"a." + Activities.KEY_RECORD_DATE + "=?", 
    			new String[] { Long.toString(selectedDateLong) }, 
    			"a." + Activities.KEY_EXERCISE, 
    			null, //having
    			"min( a." + Activities._ID + " ) asc" //orderBy
    			);
	}
	
	public Cursor getMgDupes() {
		final String sql = "SELECT "+ " MAX(" + ExerciseMuscleGroups._ID + ") as _id ,"  + ExerciseMuscleGroups.KEY_EXERCISE + ", " + ExerciseMuscleGroups.KEY_MUSCLE_GROUP + ", COUNT(*) as COUNT FROM " + ExerciseMuscleGroups.DATABASE_TABLE + " GROUP BY " + ExerciseMuscleGroups.KEY_EXERCISE + ", " + ExerciseMuscleGroups.KEY_MUSCLE_GROUP + " HAVING COUNT(*) > 1";
    	final Cursor cursor = mDb.rawQuery(sql, null);
    	return cursor;
	}
	

	
	public Cursor getBadMg() {
		final String sql = "SELECT " +ExerciseMuscleGroups._ID   +", "+  ExerciseMuscleGroups.KEY_MUSCLE_GROUP + " FROM " + ExerciseMuscleGroups.DATABASE_TABLE + " WHERE " + ExerciseMuscleGroups.KEY_MUSCLE_GROUP + " <= 0";
    	final Cursor cursor = mDb.rawQuery(sql, null);
    	return cursor;
	}
	
	
	
	public Cursor getMDupes() {
		final String sql = "SELECT "+ " MAX(" + ExerciseMuscles._ID + ") as _id ,"  + ExerciseMuscles.KEY_EXERCISE + ", " + ExerciseMuscles.KEY_MUSCLE  + ", COUNT(*) as COUNT FROM " + ExerciseMuscles.DATABASE_TABLE + " GROUP BY " + ExerciseMuscles.KEY_EXERCISE + ", " + ExerciseMuscles.KEY_MUSCLE + " HAVING COUNT(*) > 1";
    	final Cursor cursor = mDb.rawQuery(sql, null);
    	return cursor;
	}
	
	public Cursor getBadM() {
		final String sql = "SELECT " +ExerciseMuscles._ID   +", "+  ExerciseMuscles.KEY_MUSCLE + " FROM " + ExerciseMuscles.DATABASE_TABLE + " WHERE " + ExerciseMuscles.KEY_MUSCLE + " <= 0";
    	final Cursor cursor = mDb.rawQuery(sql, null);
    	return cursor;
	}

	public int deleteDuplicateMG(){
		int ret =0;
		final Cursor dupes = getMgDupes();
		int id_pos = dupes.getColumnIndex(ExerciseMuscleGroups._ID);
		while(dupes.moveToNext()){
			ret += mDb.delete(ExerciseMuscleGroups.DATABASE_TABLE, ExerciseMuscleGroups._ID + "=?", new String[]{ Long.toString(dupes.getLong(id_pos)) }); 
		}
		dupes.close();
		return ret;
	}
	
	public int deleteDuplicateM(){
		int ret =0;
		final Cursor dupes = getMDupes();
		int id_pos = dupes.getColumnIndex(ExerciseMuscles._ID);
		while(dupes.moveToNext()){
			ret += mDb.delete(ExerciseMuscles.DATABASE_TABLE, ExerciseMuscles._ID + "=?", new String[]{ Long.toString(dupes.getLong(id_pos)) }); 
		}
		dupes.close();
		return ret;
	}
	
	public Long getPrimaryMuscleForExercise(Long exerciseId) {
		return 0L;
		
	}

	public List<Long> getSecondaryMusclesForExercise(Long exerciseId) {
		// TODO Auto-generated method stub
		return null;
	}

}
