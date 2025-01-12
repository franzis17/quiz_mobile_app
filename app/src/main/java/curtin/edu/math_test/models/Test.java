package curtin.edu.math_test.models;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 *  Model of the question retrieved from the QuestionServer
 */
public class Test {

    private String startDate;
    private String startTime;
    private int totalMarks;
    private int totalTime;
    private Question question;

    public Test() {
        totalMarks = 0;
        totalTime = 0;
    }

    public Test(String startDate, String startTime, int totalMarks, int totalTime) {
        this.startDate = startDate;
        this.startTime = startTime;
        this.totalMarks = totalMarks;
        this.totalTime = totalTime;
    }

    // ----- Comparators -----

    public static Comparator<Test> TestMarksHighToLowComparator = new Comparator<Test>() {
        @Override
        public int compare(Test t1, Test t2) {
            return t2.getTotalMarks() - t1.getTotalMarks();
        }
    };

    public static Comparator<Test> TestMarksLowToHighComparator = new Comparator<Test>() {
        @Override
        public int compare(Test t1, Test t2) {
            return t1.getTotalMarks() - t2.getTotalMarks();
        }
    };

    // ----- Getters -----

    public String getStartDate() {
        return startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public int getTimeMinute() {
        return totalTime / 60;
    }

    public int getTimeSecond() {
        return totalTime % 60;
    }

    public Question getQuestion() {
        return question;
    }

    public ArrayList<Integer> getOptions() {
        return question.getOptions();
    }

    public int numOptions() {
        return question.numOptions();
    }

    public int getOption(int i) {
        return question.getOption(i);
    }

    // ----- Setters -----

    /** Called once the test has begun to set begin date and time of test */
    public void setStartDateAndTime() {
        String[] months = {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        };
        GregorianCalendar gregCal = new GregorianCalendar();
        startDate = String.format(Locale.getDefault(), "%s %d %d",
            months[gregCal.get(Calendar.MONTH)],
            gregCal.get(Calendar.DATE),
            gregCal.get(Calendar.YEAR)
        );
        startTime = String.format(Locale.getDefault(), "%d:%d:%d %s",
            gregCal.get(Calendar.HOUR),
            gregCal.get(Calendar.MINUTE),
            gregCal.get(Calendar.SECOND),
            Calendar.AM == Calendar.AM_PM ? "am" : "pm"  // Ternary Operator
        );
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    // ----- Methods -----

    /** Called once the question has been answered */
    public void answerQuestion(int answer, int addedTime) {
        markAnswer(answer);
        addTime(addedTime);
    }

    /**
     *  Called when the student answers a question, determines if the answer is right/wrong
     *  then add/deduct marks depending if answer is right or wrong
     *
     *  @param  answer  student's answer to be marked and stored on the student
     */
    public void markAnswer(int answer) {
        if (question.isCorrect(answer)) {
            totalMarks += 10;
        } else {
            // Wrong answer deducts 5 marks
            totalMarks -= 5;
            // Cannot have marks below 0
            if (totalMarks < 0) {
                totalMarks = 0;
            }
        }
    }

    /** Called after answering or passing a question */
    public void addTime(int addedTime) {
        totalTime += addedTime;
    }

}
