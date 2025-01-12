package curtin.edu.math_test.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import curtin.edu.math_test.database.MathTestDBSchema.StudentTable;
import curtin.edu.math_test.database.MathTestDBSchema.TestTable;

/**
 *  Creates the Database for MathTest app
 */
public class MathTestDBHelper extends SQLiteOpenHelper {

    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "math_test.db";

    public MathTestDBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + StudentTable.NAME + "(" +
            StudentTable.Cols.ID         + " INTEGER," +
            StudentTable.Cols.IMAGE      + " BLOB, "   +
            StudentTable.Cols.FIRST_NAME + " TEXT, "   +
            StudentTable.Cols.LAST_NAME  + " TEXT "    +
            ");"
        );
        sqLiteDatabase.execSQL("CREATE TABLE " + TestTable.NAME + "(" +
            TestTable.Cols.STUDENT_ID  + " INTEGER, " +
            TestTable.Cols.START_DATE  + " TEXT, "    +
            TestTable.Cols.START_TIME  + " TEXT, "    +
            TestTable.Cols.TOTAL_MARKS + " INTEGER, " +
            TestTable.Cols.TOTAL_TIME  + " INTEGER"   +
            ");"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

}
