package curtin.edu.math_test.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import curtin.edu.math_test.models.Student;
import curtin.edu.math_test.models.Test;
import curtin.edu.math_test.database.MathTestDBSchema.StudentTable;
import curtin.edu.math_test.database.MathTestDBSchema.TestTable;

public class MathTestDBCursor extends CursorWrapper {

    public MathTestDBCursor(Cursor cursor) {
        super(cursor);
    }

    public Student getStudent() {
        byte[] photoBytes = getBlob(getColumnIndex(StudentTable.Cols.IMAGE));
        int id = getInt(getColumnIndex(StudentTable.Cols.ID));
        String firstName = getString(getColumnIndex(StudentTable.Cols.FIRST_NAME));
        String lastName = getString(getColumnIndex(StudentTable.Cols.LAST_NAME));
        return new Student(photoBytes, id, firstName, lastName);
    }

    public Test getTest() {
        String startDate = getString(getColumnIndex(TestTable.Cols.START_DATE));
        String startTime = getString(getColumnIndex(TestTable.Cols.START_TIME));
        int totalMarks = getInt(getColumnIndex(TestTable.Cols.TOTAL_MARKS));
        int totalTime = getInt(getColumnIndex(TestTable.Cols.TOTAL_TIME));
        return new Test(startDate, startTime, totalMarks, totalTime);
    }

}
