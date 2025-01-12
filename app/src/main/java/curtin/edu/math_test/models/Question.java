package curtin.edu.math_test.models;

import java.util.ArrayList;

public class Question {

    private String question;
    private int result;
    private int timeToSolve;
    private ArrayList<Integer> options;

    public Question(String question, int result, int timeToSolve) {
        this.question = question;
        this.result = result;
        this.timeToSolve = timeToSolve;
        options = new ArrayList<>();
    }

    // ----- Getters -----

    public String getQuestion() {
        return question;
    }

    public int getResult() {
        return result;
    }

    public int getTimeToSolve() {
        return timeToSolve;
    }

    public int numOptions() {
        return options.size();
    }

    public ArrayList<Integer> getOptions() {
        return options;
    }

    public int getOption(int i) {
        return options.get(i);
    }

    // ----- Setters -----

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public void setTimeToSolve(int timeToSolve) {
        this.timeToSolve = timeToSolve;
    }

    public void addOption(int option) {
        options.add(option);
    }

    // ----- Validations -----

    public boolean isCorrect(int answer) {
        return answer == result;
    }

}
