package curtin.edu.math_test.database;

public class MathTestDBSchema {

    public static class StudentTable {
        public static final String NAME = "students";
        public static class Cols {
            public static final String ID = "id";
            public static final String IMAGE = "image";
            public static final String FIRST_NAME = "first_name";
            public static final String LAST_NAME = "last_name";
        }
    }

    public static class TestTable {
        public static final String NAME = "student_tests";
        public static class Cols {
            public static final String STUDENT_ID = "student_id";
            public static final String START_DATE = "start_date";
            public static final String START_TIME = "start_time";
            public static final String TOTAL_MARKS = "total_marks";
            public static final String TOTAL_TIME = "total_time";
        }
    }

}
