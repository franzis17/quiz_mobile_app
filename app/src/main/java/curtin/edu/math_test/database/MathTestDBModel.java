package curtin.edu.math_test.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

import curtin.edu.math_test.models.Student;
import curtin.edu.math_test.models.Test;
import curtin.edu.math_test.database.MathTestDBSchema.StudentTable;
import curtin.edu.math_test.database.MathTestDBSchema.TestTable;

/**
 *  Contains all operational database interactions for Math Test app
 */
public class MathTestDBModel {

    private SQLiteDatabase db;

    public void load(Context context) {
        this.db = new MathTestDBHelper(context.getApplicationContext()).getWritableDatabase();
    }

    /** Only used when resetting database */
    private void resetDB(Context context) {
        if (db.isOpen()) {
            db.close();
            context.deleteDatabase("math_test.db");
        } else {
            Log.d("On DB reset", "Database is already closed");
        }
    }

    // ----- Add Values -----

    public void addStudent(Student student) {
        ContentValues cv = new ContentValues();
        cv.put(StudentTable.Cols.ID, student.getID());
        cv.put(StudentTable.Cols.IMAGE, student.getPhoto());
        cv.put(StudentTable.Cols.FIRST_NAME, student.getFirstName());
        cv.put(StudentTable.Cols.LAST_NAME, student.getLastName());
        db.insert(StudentTable.NAME, null, cv);
    }

    /** Needs student's id as a reference to whose test it is */
    public void addTest(Student student, Test test) {
        ContentValues cv = new ContentValues();
        cv.put(TestTable.Cols.STUDENT_ID, student.getID());
        cv.put(TestTable.Cols.START_DATE, test.getStartDate());
        cv.put(TestTable.Cols.START_TIME, test.getStartTime());
        cv.put(TestTable.Cols.TOTAL_MARKS, test.getTotalMarks());
        cv.put(TestTable.Cols.TOTAL_TIME, test.getTotalTime());
        db.insert(TestTable.NAME, null, cv);
    }

    // ----- Edit Values -----

    public void editStudent(Student student) {
        ContentValues cv = new ContentValues();
        cv.put(StudentTable.Cols.ID, student.getID());
        cv.put(StudentTable.Cols.IMAGE, student.getPhoto());
        cv.put(StudentTable.Cols.FIRST_NAME, student.getFirstName());
        cv.put(StudentTable.Cols.LAST_NAME, student.getLastName());

        String[] whereValue = new String[] { String.valueOf(student.getID()) };
        db.update(StudentTable.NAME, cv, StudentTable.Cols.ID + " = ?", whereValue);
    }

    // ----- Delete Values -----

    public void deleteStudent(Student student) {
        String[] whereValue = new String[] { String.valueOf(student.getID()) };
        db.delete(StudentTable.NAME, StudentTable.Cols.ID + " = ?", whereValue);

        // Delete Student's Test
        db.delete(TestTable.NAME, TestTable.Cols.STUDENT_ID + " = ?", whereValue);
    }

    // ----- Getters -----

    public ArrayList<Student> getStudents() {
        ArrayList<Student> studentList = new ArrayList<>();

        Cursor cursor = db.query(StudentTable.NAME, null, null, null, null, null, null);

        try (MathTestDBCursor dbCursor = new MathTestDBCursor(cursor)) {
            dbCursor.moveToFirst();
            while (!dbCursor.isAfterLast()) {
                studentList.add(dbCursor.getStudent());
                dbCursor.moveToNext();
            }
        }

        return studentList;
    }

    /** Get all the Student's Test in the database using the Student's ID */
    public ArrayList<Test> getStudentTestList(Student student) {
        ArrayList<Test> testList = new ArrayList<>();

        String whereClause = TestTable.Cols.STUDENT_ID + "=?";
        String[] whereValues = new String[] { String.valueOf(student.getID()) };

        Cursor cursor = db.query(TestTable.NAME, null, whereClause, whereValues, null, null, null);

        try (MathTestDBCursor dbCursor = new MathTestDBCursor(cursor)) {
            dbCursor.moveToFirst();
            while (!dbCursor.isAfterLast()) {
                testList.add(dbCursor.getTest());
                dbCursor.moveToNext();
            }
        }

        return testList;
    }

}
