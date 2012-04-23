/**
 * @author Abdelelah Salama(engobada@cs.washington.edu)
 * @author Chung Han(han@cs.washington.edu)
 * @author Nikhil Karkarey(nikhilkarkarey@gmail.com)
 * 
 * Talking Memories - DbHelper
 * 
 * Designed to meet the requirements of the Winter 2012 UW course, 
 * CSE 481H: Accessibility Capstone
 * 
 * Prepares database to save images and their associated tags
 */

package mobile.accessibility;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {
	private static final String DB_NAME = "db";
	private static final int DB_VERSION = 1;
	public static final String TABLE_NAME = "mediaPath";
	public static final String COL_IMG = "iFile";
	public static final String COL_AUD = "aFile";
	public static final String STRING_CREATE = "CREATE TABLE "+TABLE_NAME+" (_id INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_IMG + " TEXT, "+ COL_AUD + " TEXT);" ;
	
	public DbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}
	
	// Create the database table
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(STRING_CREATE);
	}

	// update the database table
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		onCreate(db);
	}
}
