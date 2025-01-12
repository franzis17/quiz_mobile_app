package curtin.edu.math_test.activities;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import curtin.edu.math_test.R;
import curtin.edu.math_test.models.MathTestSystem;
import curtin.edu.math_test.models.Student;
import curtin.edu.math_test.models.Test;

public class ViewTestResults extends AppCompatActivity {

    private MathTestSystem mathTestSystem;

    private ArrayList<Test> testList;
    private TestResultsAdapter testResultsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_test_results);

        mathTestSystem = MathTestSystem.getInstance(ViewTestResults.this);
        Student studentToViewTest = mathTestSystem.getStudentToViewTest();
        testList = studentToViewTest.getTestList();

        RecyclerView testResultRecyclerView = findViewById(R.id.testResultRecyclerView);
        testResultRecyclerView.setLayoutManager(new LinearLayoutManager(ViewTestResults.this));
        testResultRecyclerView.setAdapter(testResultsAdapter = new TestResultsAdapter());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mathTestSystem.setStudentToViewTest(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sort_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_highToLow) {
            // sort high to low
            testList.sort(Test.TestMarksHighToLowComparator);
            testResultsAdapter.notifyDataSetChanged();
            return true;
        } else if (item.getItemId() == R.id.menu_lowToHigh) {
            // sort low to high
            testList.sort(Test.TestMarksLowToHighComparator);
            testResultsAdapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     *  ViewHolder for Test Results
     */
    public class TestResultsViewHolder extends RecyclerView.ViewHolder {

        private final TextView startDateText;
        private final TextView startTimeText;
        private final TextView totalMarksText;
        private final TextView timeSpentText;

        public TestResultsViewHolder(@NonNull View itemView) {
            super(itemView);
            startDateText = itemView.findViewById(R.id.startDateText);
            startTimeText = itemView.findViewById(R.id.startTimeText);
            totalMarksText = itemView.findViewById(R.id.totalMarksText);
            timeSpentText = itemView.findViewById(R.id.timeSpentText);
        }

        public void bind(Test test) {
            startDateText.setText(test.getStartDate());
            startTimeText.setText(test.getStartTime());
            totalMarksText.setText(String.valueOf(test.getTotalMarks()));
            timeSpentText.setText(
                String.format(Locale.getDefault(), "%d min %d sec",
                    test.getTimeMinute(), test.getTimeSecond())
            );
        }

    }  // END OF TestResultsViewHolder

    /**
     *  Adapter for Test Results
     */
    public class TestResultsAdapter extends RecyclerView.Adapter<TestResultsViewHolder> {

        @NonNull
        @Override
        public TestResultsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int itemType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View itemView = inflater.inflate(R.layout.list_tests, parent, false);
            return new TestResultsViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull TestResultsViewHolder vh, int position) {
            vh.bind(testList.get(position));
        }

        @Override
        public int getItemCount() {
            return testList.size();
        }

    }  // END OF TestResultsAdapter

}