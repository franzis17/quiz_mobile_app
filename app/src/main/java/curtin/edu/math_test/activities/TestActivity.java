package curtin.edu.math_test.activities;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.LinkedList;

import javax.net.ssl.HttpsURLConnection;

import curtin.edu.math_test.R;
import curtin.edu.math_test.models.MathTestSystem;
import curtin.edu.math_test.models.Question;
import curtin.edu.math_test.models.Student;
import curtin.edu.math_test.models.Test;
import curtin.edu.math_test.utils.DownloadUtils;
import curtin.edu.math_test.utils.ServerConnections;
import curtin.edu.math_test.utils.UserInterface;
import curtin.edu.math_test.utils.ValidateData;

/**
 *  During the test, Students should be able to input their answers if the number of options are
 *  less than 2 and more than 1 will display the button layout depending on how many number of
 *  options. Depending on the number of options, the number of buttons displayed should only be
 *  either 4, 3 or 2 and the different layouts can be navigated using previous and next buttons.
 */
public class TestActivity extends AppCompatActivity {

    private final Activity THIS_ACTIVITY = TestActivity.this;

    private static final String API_KEY = "01189998819991197253";

    /* DB and memory save/load */
    private MathTestSystem mathTestSystem;

    /* Data needed for the test */
    private Student studentDoingTest;
    private Test test;

    /* Views */
    private ProgressBar timer;
    private ProgressBar progressBar;
    private ConstraintLayout timerConstraintLayout;
    private ConstraintLayout scoreAndTimeConstraintLayout;
    private LinearLayout answerInputLinearLayout;
    private LinearLayout optionsLinearLayout;
    private TextView questionText;
    private TextView totalMarksText;
    private TextView totalTimeText;
    private EditText answerInput;
    private Button enterButton;
    private Button prevButton;
    private Button nextButton;
    private Button endButton;
    private Button passButton;

    private LayoutInflater layoutInflater;

    /* Loading options */
    private static int i_option;  // For navigating between Button View Layouts
    private static int i_button;  // Used by the Button ViewHolders to bind options to buttons
    private ArrayList<View> optionViews;

    /* Progress Bar Countdown */
    private int time;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mathTestSystem = MathTestSystem.getInstance(THIS_ACTIVITY);
        this.studentDoingTest = mathTestSystem.getStudentDoingTest();
        mathTestSystem.setCurrentTest(this.test = new Test());

        timer = findViewById(R.id.timer);
        progressBar = findViewById(R.id.progressBar);
        timerConstraintLayout = findViewById(R.id.timerConstraintLayout);
        scoreAndTimeConstraintLayout = findViewById(R.id.scoreAndTimeConstraintLayout);
        answerInputLinearLayout = findViewById(R.id.answerInputLinearLayout);
        optionsLinearLayout = findViewById(R.id.optionsLinearLayout);
        questionText = findViewById(R.id.questionText);
        totalMarksText = findViewById(R.id.totalMarksText);
        totalTimeText = findViewById(R.id.totalTimeText);
        answerInput = findViewById(R.id.answerInput);
        enterButton = findViewById(R.id.enterButton);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        endButton = findViewById(R.id.endButton);
        passButton = findViewById(R.id.passButton);

        layoutInflater = this.getLayoutInflater();                   // Inflates the view of a button layout

        timerConstraintLayout.setVisibility(View.INVISIBLE);         // Visible on startTest()
        scoreAndTimeConstraintLayout.setVisibility(View.INVISIBLE);  // Visible on startTest()
        answerInputLinearLayout.setVisibility(View.INVISIBLE);       // Visible when there are no options
        prevButton.setVisibility(View.INVISIBLE);                    // Visible when there are previous options
        nextButton.setVisibility(View.INVISIBLE);                    // Visible when there are next options
        endButton.setVisibility(View.INVISIBLE);                     // Visible on startTest()
        passButton.setVisibility(View.INVISIBLE);                    // Visible on startTest()

        enterButton.setOnClickListener(view -> {
            String answer = answerInput.getText().toString();
            if (ValidateData.isEmpty(answer)) {
                UserInterface.toastNotifyUser(THIS_ACTIVITY, "Cannot have a blank answer");
            } else {
                answerQuestion(Integer.parseInt(answer));
            }
        });

        endButton.setOnClickListener(view -> {
            studentDoingTest.addToTestList(test);
            mathTestSystem.addTest(studentDoingTest, test);
            finish();
        });

        passButton.setOnClickListener(view -> {
            passQuestion();
        });

        // Retrieve the questions from the server and set begin date and time
        new RetrieveQuestionTask().execute();
        test.setStartDateAndTime();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    /**
     *  Retrieves the data of a question from the QuestionBankServer
     */
    public class RetrieveQuestionTask extends AsyncTask<Void, Void, Question> {

        @Override
        protected Question doInBackground(Void... voids) {
            Question question = null;

            try {
                String siteUrl = buildRemoteURL();
                HttpsURLConnection connection = ServerConnections.openConnection(siteUrl);
                DownloadUtils.addCertificate(THIS_ACTIVITY, connection);

                if (connection == null) {
                    UserInterface.toastNotifyUser(THIS_ACTIVITY, "No connection");
                } else if (ServerConnections.noGoodConnection(connection)) {
                    UserInterface.toastNotifyUser(THIS_ACTIVITY, "Problems with downloading");
                } else {
                    String rawData = downloadDataFromInputStream(connection);
                    question = parseJSONData(rawData);
                    connection.disconnect();
                }
            } catch (IOException e) {
                Log.e("IOException", e.getMessage());
                e.printStackTrace();
            } catch (GeneralSecurityException e) {
                Log.e("GeneralSecurityException", e.getMessage());
                e.printStackTrace();
            }

            return question;
        }

        @Override
        protected void onPostExecute(Question question) {
            if (question != null) {
                progressBar.setVisibility(View.GONE);
                startTest(question);
            } else {
                UserInterface.toastNotifyUser(THIS_ACTIVITY, "Unable to retrieve question");
                finish();    // Go back to previous activity
            }
        }

        private String buildRemoteURL() {
            Uri.Builder url = Uri.parse("https://192.168.1.105:8000/random/question").buildUpon();
            url.appendQueryParameter("method", "thedata.getit");
            url.appendQueryParameter("api_key", API_KEY);
            url.appendQueryParameter("format", "json");

            String siteUrl = url.build().toString();
            Log.d("URL", siteUrl);

            return siteUrl;
        }

        /**
         *  Write the data from the inputStream to ByteArrayOutputStream
         *  Then decode the outputStream to String
         *
         *  @param  connection     used to connect to the server
         *  @return String         data retrieved from the server
         *  @throws IOException    when error occurs during the download
         */
        private String downloadDataFromInputStream(HttpsURLConnection connection) throws IOException {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            InputStream inputStream = connection.getInputStream();

            byte[] buffer = new byte[1024];
            int bytesRead = inputStream.read(buffer);

            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bytesRead);
                bytesRead = inputStream.read(buffer);
            }

            inputStream.close();
            outputStream.close();
            return outputStream.toString();
        }

        /**
         *  Obtain the question, result, time limit and all the choices/options from the rawData
         *
         *  @param  rawData    contains the data that will need to be parsed and create a Test
         *                     object with
         *  @return Test       used for displaying the data of the Test on the screen and stored
         *                     as a Student's data
         */
        private Question parseJSONData(String rawData) {
            Question question = null;

            try {
                JSONObject jBase = new JSONObject(rawData);
                String questionStr = jBase.getString("question");
                int result = jBase.getInt("result");
                int timeToSolve = jBase.getInt("timetosolve");
                question = new Question(questionStr, result, timeToSolve);

                JSONArray jOptionsArr = jBase.getJSONArray("options");
                for (int i = 0; i < jOptionsArr.length(); i++) {
                    int option = jOptionsArr.getInt(i);
                    question.addOption(option);
                }
                Log.d("Number of options", String.format("%d", question.numOptions()));
            } catch (JSONException e) {
                Log.e("JSONException", e.getMessage());
                e.printStackTrace();
            }

            return question;
        }

    }  // END OF RetrieveQuestionTask

    private void startTest(Question question) {
        test.setQuestion(question);

        questionText.setText(question.getQuestion());
        answerInput.setText("");
        timerConstraintLayout.setVisibility(View.VISIBLE);
        scoreAndTimeConstraintLayout.setVisibility(View.VISIBLE);
        answerInputLinearLayout.setVisibility(View.INVISIBLE);
        prevButton.setVisibility(View.INVISIBLE);
        nextButton.setVisibility(View.INVISIBLE);
        endButton.setVisibility(View.VISIBLE);
        passButton.setVisibility(View.VISIBLE);

        setTimer(test.getQuestion().getTimeToSolve());
        startTimer();

        if (test.numOptions() < 2) {
            answerInputLinearLayout.setVisibility(View.VISIBLE);
        } else {
            initialise();
            prevButton.setOnClickListener(view -> prevOption());
            nextButton.setOnClickListener(view -> nextOption());
        }
    }

    /** Find out how many layouts to view and bind the options to each buttons of the layouts */
    public void initialise() {
        i_option = 0;
        i_button = 0;
        optionViews = new ArrayList<>();

        Log.d("Option index", String.format("%d", i_option));
        if (test.numOptions() > 4) {
            nextButton.setVisibility(View.VISIBLE);
        }

        /*
            Creating the Button Views
            1. Divide the number of total options to either 4, 3 or 2 to add up to the total options.
            2. Go through the divided numbers and create the View and the ViewHolder for that layout
               and bind all the options for that layout.
        */
        LinkedList<Integer> numLayouts = divideNumOptions(test.numOptions());
        int i_bind_option = 0;    // Index of which option, in the test, to bind to a button
        for (int layout : numLayouts) {
            if (layout == 2) {
                View twoButtonView = layoutInflater.inflate(R.layout.options_two_button, null);
                TwoButtonViewHolder viewHolder = new TwoButtonViewHolder(twoButtonView);
                for (int i = 0; i < 2; i++) {
                    viewHolder.bind(test.getOption(i_bind_option));
                    i_bind_option++;
                }
                optionViews.add(twoButtonView);
            } else if (layout == 3) {
                View threeButtonView = layoutInflater.inflate(R.layout.options_three_button, null);
                ThreeButtonViewHolder viewHolder = new ThreeButtonViewHolder(threeButtonView);
                for (int i = 0; i < 3; i++) {
                    viewHolder.bind(test.getOption(i_bind_option));
                    i_bind_option++;
                }
                optionViews.add(threeButtonView);
            } else if (layout == 4) {
                View fourButtonView = layoutInflater.inflate(R.layout.options_four_button, null);
                FourButtonViewHolder viewHolder = new FourButtonViewHolder(fourButtonView);
                for (int i = 0; i < 4; i++) {
                    viewHolder.bind(test.getOption(i_bind_option));
                    i_bind_option++;
                }
                optionViews.add(fourButtonView);
            }
        }

        // Start from the 1st view
        optionsLinearLayout.addView(optionViews.get(0));
    }

    /**
     *  Divides the total number of options to either 4, 3, or 2 to create the button layouts
     *
     *  @param  numOptions    number of options that is divided
     *  @return numbers       LinkedList of Integer that contains either numbers 4, 3, or 2
     */
    public LinkedList<Integer> divideNumOptions(int numOptions) {
        LinkedList<Integer> numbers = new LinkedList<>();

        for (int i = 0; i < numOptions; i++) {
            int number = (i % 4) + 1;

            // Everytime it hits 4, or just when the loop ends, store the number
            if ((i + 1) % 4 == 0 || i == numOptions - 1) {
                // Can't have 1 as layout so subtract 1 from last number and add 1 to new number
                if (number == 1 && numbers.size() != 0) {
                    int changeNumber = numbers.removeLast();
                    numbers.addLast(changeNumber - 1);
                    number = number + 1;
                }
                numbers.addLast(number);
            }
        }

        return numbers;
    }

    /**
     *  Called by previous button to go back to the previous view
     *  Can only be called when the index is not at the beginning
     */
    public void prevOption() {
        if (i_option > 0) {
            optionsLinearLayout.removeView(optionViews.get(i_option));
            i_option--;
            optionsLinearLayout.addView(optionViews.get(i_option));

            if (i_option == 0) {
                prevButton.setVisibility(View.INVISIBLE);
            }
            if (nextButton.getVisibility() == View.INVISIBLE) {
                nextButton.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     *  Called by next button to go to the next view
     *  Can only be called when the index is not at the end
     */
    public void nextOption() {
        if (i_option < optionViews.size()) {
            optionsLinearLayout.removeView(optionViews.get(i_option));
            i_option++;
            optionsLinearLayout.addView(optionViews.get(i_option));

            if (i_option == (optionViews.size() - 1)) {
                nextButton.setVisibility(View.INVISIBLE);
            }
            if (prevButton.getVisibility() == View.INVISIBLE) {
                prevButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void answerQuestion(int optionAnswer) {
        Log.d("Answer", String.format("%d", optionAnswer));
        test.answerQuestion(optionAnswer, time);
        retrieveNewQuestion();
    }

    private void passQuestion() {
        test.addTime(time);
        retrieveNewQuestion();
    }

    private void retrieveNewQuestion() {
        countDownTimer.cancel();
        updateScoreAndTime();
        optionsLinearLayout.removeAllViewsInLayout();
        new RetrieveQuestionTask().execute();
    }

    private void startTimer() {
        int timeToSolve = test.getQuestion().getTimeToSolve();
        Log.d("Time Limit", String.valueOf(timeToSolve));

        time = 0;
        timeToSolve = timeToSolve * 1000;  // Convert to seconds

        // Countdown every 1 second from timeToSolve
        countDownTimer = new CountDownTimer(timeToSolve, 1000) {
            @Override
            public void onTick(long msUntilFinished) {
                updateTimer(time);
                time++;
            }
            @Override
            public void onFinish() {
                UserInterface.toastNotifyUser(THIS_ACTIVITY, "Ran out of time");
                retrieveNewQuestion();
            }
        };
        countDownTimer.start();
    }

    /** Sets the max amount the progressBar has to progress through */
    private void setTimer(int max) {
        timer.setMin(0);
        timer.setMax(max);
    }

    /** Called every 1 second to update the progressBar */
    private void updateTimer(int value) {
        timer.setVisibility(View.VISIBLE);
        timer.setProgress(value);
    }

    private void updateScoreAndTime() {
        totalMarksText.setText(String.valueOf(test.getTotalMarks()));
        totalTimeText.setText(test.getTimeMinute() + " min " + test.getTimeSecond() + " sec");
    }

    /*
        ----- Button ViewHolders -----
        The optional answers will be bound to the buttons depending on how many buttons there
        are for a layout.
    */

    public class TwoButtonViewHolder extends RecyclerView.ViewHolder {

        private Button option1Button;
        private Button option2Button;
        private Button[] optionButtons;

        public TwoButtonViewHolder(@NonNull View itemView) {
            super(itemView);
            option1Button = itemView.findViewById(R.id.option1Button);
            option2Button = itemView.findViewById(R.id.option2Button);
            optionButtons = new Button[]{
                option1Button, option2Button
            };
        }

        public void bind(int option) {
            optionButtons[i_button].setText(String.valueOf(option));

            optionButtons[i_button].setOnClickListener(view -> answerQuestion(option));

            i_button++;
            // Can only bind up to 2 buttons
            if (i_button == 2) {
                i_button = 0;
            }
        }

    }  // END OF TwoButtonViewHolder

    public class ThreeButtonViewHolder extends RecyclerView.ViewHolder {

        private Button option1Button;
        private Button option2Button;
        private Button option3Button;
        private Button[] optionButtons;

        public ThreeButtonViewHolder(@NonNull View itemView) {
            super(itemView);
            option1Button = itemView.findViewById(R.id.option1Button);
            option2Button = itemView.findViewById(R.id.option2Button);
            option3Button = itemView.findViewById(R.id.option3Button);
            optionButtons = new Button[]{
                option1Button, option2Button, option3Button
            };
        }

        public void bind(int option) {
            optionButtons[i_button].setText(String.valueOf(option));

            optionButtons[i_button].setOnClickListener(view -> answerQuestion(option));

            i_button++;
            // Can only bind up to 3 buttons
            if (i_button == 3) {
                i_button = 0;
            }
        }

    }  // END OF ThreeButtonViewHolder

    public class FourButtonViewHolder extends RecyclerView.ViewHolder {

        private Button option1Button;
        private Button option2Button;
        private Button option3Button;
        private Button option4Button;
        private Button[] optionButtons;

        public FourButtonViewHolder(@NonNull View itemView) {
            super(itemView);
            option1Button = itemView.findViewById(R.id.option1Button);
            option2Button = itemView.findViewById(R.id.option2Button);
            option3Button = itemView.findViewById(R.id.option3Button);
            option4Button = itemView.findViewById(R.id.option4Button);
            optionButtons = new Button[]{
                option1Button, option2Button, option3Button, option4Button
            };
        }

        public void bind(int option) {
            optionButtons[i_button].setText(String.valueOf(option));

            optionButtons[i_button].setOnClickListener(view -> answerQuestion(option));

            i_button++;
            // Can only bind up to 4 buttons
            if (i_button == 4) {
                i_button = 0;
            }
        }

    }  // END OF FourButtonViewHolder

}
