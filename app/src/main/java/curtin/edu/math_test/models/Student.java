package curtin.edu.math_test.models;

import java.util.ArrayList;

import curtin.edu.math_test.utils.ValidateData;

/**
 * Contains all Student information including all their finished Test
 */
public class Student {

    public static final int MAX_PHONES = 10;
    public static final int MAX_EMAILS = 10;

    // Student information
    private byte[] photo;
    private int id;
    private String firstName;
    private String lastName;
    private ArrayList<Integer> phoneNumbers;
    private ArrayList<String> emails;

    private ArrayList<Test> testList;

    public Student() {
        photo = new byte[4096];
        id = -1;
        firstName = null;
        lastName = null;
        phoneNumbers = new ArrayList<>();
        emails = new ArrayList<>();

        testList = new ArrayList<>();
    }

    public Student(byte[] photo, int id, String firstName, String lastName)
        throws IllegalArgumentException {

        if (ValidateData.isEmpty(firstName)) {
            throw new IllegalArgumentException("Student's first name is empty");
        }
        if (ValidateData.isEmpty(lastName)) {
            throw new IllegalArgumentException("Student's last name is empty");
        }

        this.photo = photo;
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        phoneNumbers = new ArrayList<>();
        emails = new ArrayList<>();

        testList = new ArrayList<>();
    }

    // ----- Getters -----

    public int getID() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public int getFirstPhoneNum() {
        return phoneNumbers.get(0);
    }

    public String getFirstEmail() {
        return emails.get(0);
    }

    public ArrayList<Integer> getPhoneNumbers() {
        return phoneNumbers;
    }

    public ArrayList<String> getEmails() {
        return emails;
    }

    public int getNumTest() {
        return testList.size();
    }

    public ArrayList<Test> getTestList() {
        return testList;
    }

    public Test getTest(int i) {
        return testList.get(i);
    }

    // ----- Setters -----

    public void setID(int id) {
        this.id = id;
    }

    public void setFirstName(String firstName) throws IllegalArgumentException {
        if (ValidateData.isEmpty(firstName)) {
            throw new IllegalArgumentException("Student's first name is empty");
        } else {
            this.firstName = firstName;
        }
    }

    public void setLastName(String lastName) throws IllegalArgumentException {
        if (ValidateData.isEmpty(lastName)) {
            throw new IllegalArgumentException("Student's last name is empty");
        } else {
            this.lastName = lastName;
        }
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public void addPhoneNumber(int phoneNum) throws IllegalArgumentException {
        if (phoneNumbers.size() == MAX_PHONES) {
            throw new IllegalArgumentException(
                "Phone number cannot be added. Reached max limit of "+MAX_PHONES+".");
        } else {
            phoneNumbers.add(phoneNum);
        }
    }

    public void addEmail(String email) throws IllegalArgumentException {
        if (invalidEmail(email)) {
            throw new IllegalArgumentException("Student's email is invalid");
        } else if (emails.size() == MAX_EMAILS) {
            throw new IllegalArgumentException(
                "Email cannot be added. Reached max limit of "+MAX_EMAILS+".");
        } else {
            emails.add(email);
        }
    }

    /** Used when loading the database and retrieving Student's Test History */
    public void addTestList(ArrayList<Test> newTestList) {
        testList.addAll(newTestList);
    }

    // ----- Methods -----

    /** Called when the test has finished */
    public void addToTestList(Test test) {
        testList.add(test);
    }

    // ----- Validations -----

    public boolean hasTest() {
        return testList.size() != 0;
    }

    /**
     * Email is invalid when the input is not empty and does not contain "@" and ".com"
     *
     * @param     email    checked if it is invalid
     * @return    true     if email is not empty and does not contain both "@" and ".com"
     */
    private boolean invalidEmail(String email) {
        return (!ValidateData.isEmpty(email) && (!email.contains("@") || !email.contains(".com")));
    }

}
