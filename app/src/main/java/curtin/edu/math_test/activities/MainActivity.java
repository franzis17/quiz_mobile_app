package curtin.edu.math_test.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import curtin.edu.math_test.R;
import curtin.edu.math_test.models.MathTestSystem;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MathTestSystem mathTestSystem = MathTestSystem.getInstance(MainActivity.this);

        Button registerStudentButton = findViewById(R.id.registerStudentButton);
        Button viewStudentsButton = findViewById(R.id.viewStudentsButton);

        registerStudentButton.setOnClickListener(view -> {
            mathTestSystem.setOnlinePhoto(null);
            Intent intent = new Intent(MainActivity.this, RegisterStudent.class);
            startActivity(intent);
        });

        viewStudentsButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ViewStudentsActivity.class);
            intent.putExtra("viewType", ViewStudentsActivity.SELECTABLE);
            startActivity(intent);
        });
    }

}