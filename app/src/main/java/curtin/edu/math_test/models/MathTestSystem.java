package curtin.edu.math_test.models;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.ArrayList;

import curtin.edu.math_test.database.MathTestDBModel;

/**
 *  A singleton class that saves instances of students and their details to the memory and database
 */
public class MathTestSystem {

    /** Singleton instance */
    private static MathTestSystem myInstance = null;

    private MathTestDBModel dbModel;

    private ArrayList<Student> studentList;

    private static int studentId;
    private Student studentDoingTest;
    private Student studentToEdit;
    private Student studentToViewTest;
    private Test currentTest;

    /** Stores the photo that was selected online */
    private Bitmap onlinePhoto;

    public static MathTestSystem getInstance(Context context) {
        if (myInstance == null) {
            myInstance = new MathTestSystem();
            myInstance.load(context);
        }
        return myInstance;
    }

    public MathTestSystem() {
        dbModel = new MathTestDBModel();
        studentList = new ArrayList<>();
        studentId = 1;
    }

    public void load(Context context) {
        dbModel.load(context);
        studentList = dbModel.getStudents();

        // Load all the student's test by referring to their id
        for (Student student : studentList) {
            student.addTestList(dbModel.getStudentTestList(student));
            studentId++;
        }
    }

    // ----- Array sizes -----

    public int studentSize() {
        return studentList.size();
    }

    // ----- Getters -----

    public Student getStudent(int i) {
        return studentList.get(i);
    }

    public Student getStudentDoingTest() {
        return studentDoingTest;
    }

    public Student getStudentToEdit() {
        return studentToEdit;
    }

    public Student getStudentToViewTest() {
        return studentToViewTest;
    }

    public Test getCurrentTest() {
        return currentTest;
    }

    public Bitmap getOnlinePhoto() {
        return onlinePhoto;
    }

    // ----- Setters -----

    public void setStudentDoingTest(Student studentDoingTest) {
        this.studentDoingTest = studentDoingTest;
    }

    public void setStudentToEdit(Student studentToEdit) {
        this.studentToEdit = studentToEdit;
    }

    public void setStudentToViewTest(Student studentToViewTest) {
        this.studentToViewTest = studentToViewTest;
    }

    public void setCurrentTest(Test currentTest) {
        this.currentTest = currentTest;
    }

    public void setOnlinePhoto(Bitmap onlinePhoto) {
        this.onlinePhoto = onlinePhoto;
    }

    // ----- Add Data to DB -----

    public void addStudent(Student student) {
        student.setID(studentId);
        studentList.add(student);
        dbModel.addStudent(student);
        studentId++;
    }

    public void addTest(Student student, Test test) {
        dbModel.addTest(student, test);
    }

    // ----- Edit Data from DB -----

    public void editStudent(Student student) {
        dbModel.editStudent(student);
    }

    // ----- Delete Data from DB -----

    public void deleteStudent(Student student) {
        studentList.remove(student);
        dbModel.deleteStudent(student);
    }

    // ----- Validations -----

    /** Check if a Student has a test or not */
    public boolean hasTest(Student student) {
        return student.hasTest();
    }

}
