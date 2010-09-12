package com.alexrothberg.afitness;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.util.Log;


public class DbAdapter {
	public static final class MuscleGroups implements BaseColumns{
		private MuscleGroups(){}
		
		private static final String NAME_ABDOMINALS = "Abdominals";
		private static final String NAME_BACK = "Back";
		private static final String NAME_CHEST= "Chest";


		
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
		
		public static final String DATABASE_CREATE = String.format(
				"create table %s (" +
					"%s integer primary key autoincrement, " +
					"%s text not null);"
				, DATABASE_TABLE, _ID, KEY_NAME);
		
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
    public static final int DATABASE_VERSION = 5;
    
    private static final String TAG = "DbAdapter";
    
    private DatabaseHelper mDbHelper;
    private final Context mCtx;
    private SQLiteDatabase mDb; 

	private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.v(TAG, "Creating tables");
			
			db.execSQL(MuscleGroups.DATABASE_CREATE);
			db.execSQL(Exercises.DATABASE_CREATE);
			db.execSQL(Equipments.DATABASE_CREATE);
			db.execSQL(Muscles.DATABASE_CREATE);
			db.execSQL(ExerciseMuscles.DATABASE_CREATE);
			db.execSQL(ExerciseMuscleGroups.DATABASE_CREATE);
            db.execSQL(Workouts.DATABASE_CREATE);
			db.execSQL(WorkoutExercises.DATABASE_CREATE);
            db.execSQL(Activities.DATABASE_CREATE);	
            
            createMuscleGroups(db);
            insertDummyData(db);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            
            // Do these in the reverse order of the create
            db.execSQL("DROP TABLE IF EXISTS " + Activities.DATABASE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + WorkoutExercises.DATABASE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + Workouts.DATABASE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + Equipments.DATABASE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + ExerciseMuscles.DATABASE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + ExerciseMuscleGroups.DATABASE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + Muscles.DATABASE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + Exercises.DATABASE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + MuscleGroups.DATABASE_TABLE);
            
            onCreate(db);			
		}
		
	    public static long createExercise(String name, SQLiteDatabase db){
	    	ContentValues values = new ContentValues();
	    	values.put(Exercises.KEY_NAME, name);
	    	return db.insert(Exercises.DATABASE_TABLE, null, values);
	    }
	    
	    public static long createEquipment(String name, SQLiteDatabase db){
	    	ContentValues values = new ContentValues();
	    	values.put(Equipments.KEY_NAME, name);
	    	return db.insert(Equipments.DATABASE_TABLE, null, values);
	    }
	    
	    public static long createWorkout(String name, SQLiteDatabase db) {
	        ContentValues values = new ContentValues();
	        values.put(Workouts.KEY_NAME, name);

	        return db.insert(Workouts.DATABASE_TABLE, null, values);
	    }
	    
	    public static long renameWorkout(long workout_id, String new_name, SQLiteDatabase db) {
	        ContentValues values = new ContentValues();
	        values.put(Workouts.KEY_NAME, new_name);
	        
	        return db.update(Workouts.DATABASE_TABLE, values, Workouts._ID + "= ?", new String[]{workout_id + ""});
	    }
	    
	    public static long addWorkoutExercise(long workout_id, long exercise_id, SQLiteDatabase db){
	    	 ContentValues values = new ContentValues();
	    	 values.put(WorkoutExercises.KEY_WORKOUT, workout_id);
	    	 values.put(WorkoutExercises.KEY_EXERCISE, exercise_id);
	    	 return db.insert(WorkoutExercises.DATABASE_TABLE, null, values);
	    }
	    
	    public static long createMuscle(String name, long muscleGroup_id, SQLiteDatabase db) {
	    	 ContentValues values = new ContentValues();
	    	 values.put(Muscles.KEY_NAME, name);
	    	 values.put(Muscles.KEY_MUSCLE_GROUP_ID, muscleGroup_id);
	    	 return db.insert(Muscles.DATABASE_TABLE, null, values);			
		}
	    
		public static long addExerciseEquipment(long exercise, long equipment, SQLiteDatabase db) {
			//TODO
			return 0L;
		}
		
		public static long recordMuscle(long exercise, long muscle, boolean isPrimary, SQLiteDatabase db){
	    	 ContentValues values = new ContentValues();
	    	 values.put(ExerciseMuscles.KEY_EXERCISE, exercise);
	    	 values.put(ExerciseMuscles.KEY_MUSCLE, muscle);
	    	 values.put(ExerciseMuscles.KEY_ISPRIMARY, isPrimary);

	    	 return db.insert(ExerciseMuscles.DATABASE_TABLE, null, values);			
		}
		
		public static long recordMuscleGroup(long exercise, long muscleGroup_id, SQLiteDatabase db){
	    	 ContentValues values = new ContentValues();
	    	 values.put(ExerciseMuscleGroups.KEY_EXERCISE, exercise);
	    	 values.put(ExerciseMuscleGroups.KEY_MUSCLE_GROUP, muscleGroup_id);

	    	 return db.insert(ExerciseMuscleGroups.DATABASE_TABLE, null, values);			
		}
		
		public static long recordSecondaryMuscle(long exercise, long muscle, SQLiteDatabase db){
			return recordMuscle(exercise, muscle, false, db);
		}
		
		public static long recordPrimaryMuscle(long exercise, long muscle, SQLiteDatabase db){
			return recordMuscle(exercise, muscle, true, db);
			
		}
		
		private static void createMuscleGroups(SQLiteDatabase db){
			String[] names = {
				MuscleGroups.NAME_ABDOMINALS,
				"Arms",
				MuscleGroups.NAME_BACK,
				MuscleGroups.NAME_CHEST,
				"Legs",
				"Shoulders",
				};
			
			for(String name : names){
				createMuscleGroup(name, db);
			}
		}
		
		private static long getMuscleGroupByName(String muscleGroup_name, SQLiteDatabase db){
			Cursor cursor =  db.query(
					MuscleGroups.DATABASE_TABLE, 
					new String[]{MuscleGroups._ID}, 
					MuscleGroups.KEY_NAME + " = ?", 
					new String[]{muscleGroup_name}, null, null, null);
			
			if (!cursor.moveToFirst()){
				return -1;
			}
			return cursor.getLong(0);
		}

		private static long createMuscleGroup(String name, SQLiteDatabase db) {
	    	 ContentValues values = new ContentValues();
	    	 values.put(MuscleGroups.KEY_NAME, name);

	    	 return db.insert(MuscleGroups.DATABASE_TABLE, null, values);			
		}

		private static void insertDummyData(SQLiteDatabase db) {
			long benchPress = createExercise("Bench Press", db);
			long pullups = createExercise("Pullups", db);
			
			long squat = createExercise("Squat", db);
			long dips = createExercise("Dips", db);
			createExercise("Flies", db);
			createExercise("Chin-ups", db);
			createExercise("Dumbell Shoulder Press", db);
			createExercise("Skullcrushers", db);
			createExercise("Front Squat", db);
			createExercise("Inclined Press", db);



			
			long upperBody = createWorkout("Upper Body", db);
			long lowerBody = createWorkout("Lower Body", db);
			
			addWorkoutExercise(lowerBody, squat, db);
			addWorkoutExercise(upperBody, pullups, db);
			addWorkoutExercise(upperBody, benchPress, db);
			
			createEquipment("Dumbbells", db);
			createEquipment("Swiss Ball", db);
			long dipMachine = createEquipment("Dip Machine", db);

			long flatBench = createEquipment("Flat Bench", db);
			
			addExerciseEquipment(dips, dipMachine, db);
			addExerciseEquipment(benchPress, flatBench, db);

			long muscleGroup_id_back = getMuscleGroupByName(MuscleGroups.NAME_BACK, db);
			long muscleGroup_id_chest= getMuscleGroupByName(MuscleGroups.NAME_CHEST, db);

			long trapezius = createMuscle("Trapezius", muscleGroup_id_back, db);	
			long pectoralis = createMuscle("Pectoralis", muscleGroup_id_chest, db);
			createMuscle("Deltoids", muscleGroup_id_chest, db);	
			createMuscle("Rotator Cuff", muscleGroup_id_chest, db);	
			createMuscle("Gastrocnemius", muscleGroup_id_chest, db);	
			createMuscle("Soleus", muscleGroup_id_chest, db);	
			createMuscle("Hamstrings", muscleGroup_id_chest, db);	
			createMuscle("Glutes", muscleGroup_id_chest, db);	
			createMuscle("Quadriceps", muscleGroup_id_chest, db);	
			createMuscle("Wrist Flxors", muscleGroup_id_chest, db);	
			createMuscle("Biceps Brachii", muscleGroup_id_chest, db);	
			createMuscle("Triceps Brachii", muscleGroup_id_chest, db);	


			
			recordPrimaryMuscle(benchPress, pectoralis, db);
			recordPrimaryMuscle(pullups, trapezius, db);

			
			recordMuscleGroup(benchPress, muscleGroup_id_chest, db);

			
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
        		null, null, null, null, null);
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
		return builder.query(mDb, new String[] {"we." + WorkoutExercises._ID + " as " + WorkoutExercises._ID, "we." + WorkoutExercises.KEY_EXERCISE + " as " + WorkoutExercises.KEY_EXERCISE, "e." + Exercises.KEY_NAME + " as " + Exercises.KEY_NAME}, "we." + WorkoutExercises.KEY_WORKOUT + "=" + workout_id, null, null, null, null);
    }
    
    public long createExercise(String name){
    	return DatabaseHelper.createExercise(name, mDb);
    }

	public Cursor fetchAllExercises() {
		return mDb.query(
        		Exercises.DATABASE_TABLE, 
        		new String[] {
        				Exercises._ID, 
        				Exercises.KEY_NAME}
        		, null, null, null, null, Exercises.KEY_NAME);
	}
	
    public Cursor fetchExercisesForMuscleGroup(long musclegroup_id){
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(ExerciseMuscleGroups.DATABASE_TABLE + " emg join " + Exercises.DATABASE_TABLE + " e on emg." + ExerciseMuscleGroups.KEY_EXERCISE + " = e." + Exercises._ID);
		return builder.query(mDb, new String[] {"emg." + ExerciseMuscleGroups._ID + " as " + ExerciseMuscleGroups._ID, "emg." + ExerciseMuscleGroups.KEY_EXERCISE + " as " + ExerciseMuscleGroups.KEY_EXERCISE, "e." + Exercises.KEY_NAME + " as " + Exercises.KEY_NAME}, "emg." + ExerciseMuscleGroups.KEY_MUSCLE_GROUP + "=" + musclegroup_id, null, null, null, null);
    }
    
    public Cursor fetchExercisesForMuscle(long muscle_id){
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(ExerciseMuscles.DATABASE_TABLE + " em join " + Exercises.DATABASE_TABLE + " e on em." + ExerciseMuscles.KEY_EXERCISE + " = e." + Exercises._ID);
		return builder.query(mDb, new String[] {"em." + ExerciseMuscles._ID + " as " + ExerciseMuscles._ID, "em." + ExerciseMuscles.KEY_EXERCISE + " as " + ExerciseMuscles.KEY_EXERCISE, "e." + Exercises.KEY_NAME + " as " + Exercises.KEY_NAME}, "em." + ExerciseMuscles.KEY_MUSCLE + " = ?", new String[]{ muscle_id + ""}, null, null, null);
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
    			Muscles.KEY_NAME);
    }
    
    public Cursor fetchAllMuscles(){
    	return mDb.query(
    			Muscles.DATABASE_TABLE, 
    			new String[]{ 
    					Muscles._ID,
    					Muscles.KEY_NAME,
    			}, null, null, null, null, Muscles.KEY_NAME);    	
    }

	public int deleteWorkout(long id) {
		return mDb.delete(Workouts.DATABASE_TABLE, Workouts._ID + "=?", new String[]{id +""});		
	}

}
