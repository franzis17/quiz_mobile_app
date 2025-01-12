package curtin.edu.math_test.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import curtin.edu.math_test.R;
import curtin.edu.math_test.models.MathTestSystem;
import curtin.edu.math_test.models.Student;

public class ViewStudentsActivity extends AppCompatActivity {

    private final Activity THIS_ACTIVITY = ViewStudentsActivity.this;

    /** Shows edit button and delete button on the List of Students */
    public static final int EDITABLE = 1;
    /** Students will only be selectable on the list */
    public static final int SELECTABLE = 2;

    /** Either EDITABLE or VIEWABLE */
    private int viewType;

    private MathTestSystem mathTestSystem;

    private StudentAdapter studentAdapter;
    private int adapterPosition;
    private boolean onChangeItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_students);

        mathTestSystem = MathTestSystem.getInstance(THIS_ACTIVITY);

        viewType = getIntent().getIntExtra("viewType", 2);

        TextView headerText = findViewById(R.id.headerText);

        // Header text of ViewStudentActivity depends on the activity to be done
        if (viewType == SELECTABLE) {
            headerText.setText("Start Test");
        } else if (viewType == EDITABLE) {
            headerText.setText("Student List");
        }

        RecyclerView viewStudentsRecyclerView = findViewById(R.id.viewStudentsRecyclerView);
        viewStudentsRecyclerView.setLayoutManager(new LinearLayoutManager(THIS_ACTIVITY));
        viewStudentsRecyclerView.setAdapter(studentAdapter = new StudentAdapter());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (onChangeItem) {
            studentAdapter.notifyItemChanged(adapterPosition);
            onChangeItem = false;
        }
    }

    /**
     *   ViewHolder for Students
     */
    public class StudentViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout studentConstraintLayout;
        private ImageView studentImage;
        private TextView firstNameText;
        private TextView lastNameText;
        private Button editButton;
        private Button deleteButton;
        private Button selectButton;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            studentConstraintLayout = itemView.findViewById(R.id.studentConstraintLayout);
            studentImage = itemView.findViewById(R.id.studentImage);
            firstNameText = itemView.findViewById(R.id.firstNameText);
            lastNameText = itemView.findViewById(R.id.lastNameText);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            selectButton = itemView.findViewById(R.id.selectButton);
        }

        public void bind(Student student) {
            byte[] photo = student.getPhoto();
            Bitmap photoBitmap = BitmapFactory.decodeByteArray(photo, 0, photo.length);
            studentImage.setImageBitmap(photoBitmap);

            firstNameText.setText(student.getFirstName());
            lastNameText.setText(student.getLastName());

            studentConstraintLayout.setOnClickListener(view -> {
                mathTestSystem.setStudentToViewTest(student);
                startActivity(new Intent(THIS_ACTIVITY, ViewTestResults.class));
            });

            editButton.setOnClickListener(view -> {
                mathTestSystem.setStudentToEdit(student);
                adapterPosition = getAdapterPosition();
                onChangeItem = true;
                startActivity(new Intent(THIS_ACTIVITY, EditStudentInfo.class));
            });

            deleteButton.setOnClickListener(view -> {
                mathTestSystem.deleteStudent(student);
                studentAdapter.notifyItemRemoved(getAdapterPosition());
            });

            selectButton.setOnClickListener(view -> {
                mathTestSystem.setStudentDoingTest(student);
                startActivity(new Intent(THIS_ACTIVITY, TestActivity.class));
            });

            if (viewType == SELECTABLE) {
                editButton.setVisibility(View.INVISIBLE);
                deleteButton.setVisibility(View.INVISIBLE);
                selectButton.setVisibility(View.VISIBLE);
            }
            if (viewType == EDITABLE) {
                editButton.setVisibility(View.VISIBLE);
                deleteButton.setVisibility(View.VISIBLE);
                selectButton.setVisibility(View.INVISIBLE);
            }
        }

    }  // END OF StudentViewHolder

    /**
     *   Adapter for Students
     */
    public class StudentAdapter extends RecyclerView.Adapter<StudentViewHolder> {

        @NonNull
        @Override
        public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View itemView = inflater.inflate(R.layout.list_students, parent, false);
            return new StudentViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull StudentViewHolder viewHolder, int position) {
            viewHolder.bind(mathTestSystem.getStudent(position));
        }

        @Override
        public int getItemCount() {
            return mathTestSystem.studentSize();
        }

    }  // END OF StudentAdapter

}