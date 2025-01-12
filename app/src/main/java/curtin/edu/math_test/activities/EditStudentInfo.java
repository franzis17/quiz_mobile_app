package curtin.edu.math_test.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;

import curtin.edu.math_test.BuildConfig;
import curtin.edu.math_test.R;
import curtin.edu.math_test.models.MathTestSystem;
import curtin.edu.math_test.models.Student;
import curtin.edu.math_test.utils.Conversion;
import curtin.edu.math_test.utils.UserInterface;
import curtin.edu.math_test.utils.ValidateData;

public class EditStudentInfo extends AppCompatActivity {

    private final Activity THIS_ACTIVITY = EditStudentInfo.this;

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int REQUEST_ACCESS_PHOTO = 2;

    private MathTestSystem mathTestSystem;
    private Student studentToEdit;

    private File photoFile;

    /* Views */
    private ImageView studentImage;
    private EditText firstNameInput;
    private EditText lastNameInput;
    private EditText phoneNumInput;
    private EditText emailInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_student_info);

        mathTestSystem = MathTestSystem.getInstance(EditStudentInfo.this);
        studentToEdit = mathTestSystem.getStudentToEdit();

        studentImage = findViewById(R.id.studentImage);
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        phoneNumInput = findViewById(R.id.phoneNumInput);
        emailInput = findViewById(R.id.emailInput);
        ImageButton takePhotoButton = findViewById(R.id.takePhotoButton);
        ImageButton searchGalleryButton = findViewById(R.id.searchGalleryButton);
        ImageButton searchOnlineButton = findViewById(R.id.searchOnlineButton);
        Button addPhoneButton = findViewById(R.id.addPhoneButton);
        Button addEmailButton = findViewById(R.id.addEmailButton);
        Button editStudentButton = findViewById(R.id.editStudentButton);

        // Display existing student info to be edited
        studentImage.setImageBitmap(Conversion.bytesToBitmap(studentToEdit.getPhoto()));
        firstNameInput.setText(studentToEdit.getFirstName());
        lastNameInput.setText(studentToEdit.getLastName());

        /* Photo Buttons */
        takePhotoButton.setOnClickListener(view -> {
            takePhoto();
        });
        searchGalleryButton.setOnClickListener(view -> {
            searchGallery();
        });
        searchOnlineButton.setOnClickListener(view -> {
            startActivity(new Intent(THIS_ACTIVITY, BrowsePhotosOnline.class));
        });

        /* Student Info Buttons */
        addPhoneButton.setOnClickListener(view -> {
            try {
                String phoneNum = phoneNumInput.getText().toString();
                // Phone can be empty but not stored
                if (!ValidateData.isEmpty(phoneNum)) {
                    studentToEdit.addPhoneNumber(Integer.parseInt(phoneNum));
                }
            } catch (IllegalArgumentException e) {
                UserInterface.toastNotifyUser(THIS_ACTIVITY, e.getMessage());
            }
        });
        addEmailButton.setOnClickListener(view -> {
            try {
                String email = emailInput.getText().toString();
                // Email can be empty but not stored
                if (!ValidateData.isEmpty(email)) {
                    studentToEdit.addEmail(email);
                }
            } catch (IllegalArgumentException e) {
                UserInterface.toastNotifyUser(THIS_ACTIVITY, e.getMessage());
            }
        });
        editStudentButton.setOnClickListener(view -> {
            editStudent();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Bitmap photo = BitmapFactory.decodeFile(photoFile.toString());
            studentImage.setImageBitmap(photo);
        }
        if (requestCode == REQUEST_ACCESS_PHOTO && resultCode == RESULT_OK) {
            Uri targetUri = data.getData();
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(
                    getContentResolver().openInputStream(targetUri));
                studentImage.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void takePhoto() {
        // Empty online photo first or else it'll be set on onResume() after the photo is taken
        mathTestSystem.setOnlinePhoto(null);

        photoFile = new File(getFilesDir(), "photo.jpg");
        Uri photoUri = FileProvider.getUriForFile(
            getApplicationContext(), BuildConfig.APPLICATION_ID + ".fileprovider", photoFile
        );

        Intent photoIntent = new Intent();
        photoIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

        PackageManager pkgMgr = getPackageManager();
        for (ResolveInfo resInfo : pkgMgr.queryIntentActivities(
                photoIntent, PackageManager.MATCH_DEFAULT_ONLY)
        ) {
            grantUriPermission(
                resInfo.activityInfo.packageName, photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            );
        }

        startActivityForResult(photoIntent, REQUEST_TAKE_PHOTO);
    }

    private void searchGallery() {
        // Empty online photo first or else it'll be set on onResume() after picking the photo
        mathTestSystem.setOnlinePhoto(null);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_ACCESS_PHOTO);
    }

    private void editStudent() {
        // Set the imageView to the student's image by converting it to bytes
        if (studentImage.getDrawable() != null) {
            BitmapDrawable drawable = (BitmapDrawable) studentImage.getDrawable();
            Bitmap imgBitmap = drawable.getBitmap();
            byte[] imgBytes = Conversion.bitmapToBytes(imgBitmap);
            studentToEdit.setPhoto(imgBytes);
        }

        String firstName = firstNameInput.getText().toString();
        String lastName = lastNameInput.getText().toString();
        String phoneNum = phoneNumInput.getText().toString();
        String email = emailInput.getText().toString();

        studentToEdit.setFirstName(firstName);
        studentToEdit.setLastName(lastName);
        // Phone and email can be empty but not to be stored
        if (!ValidateData.isEmpty(phoneNum)) {
            studentToEdit.addPhoneNumber(Integer.parseInt(phoneNum));
        }
        if (!ValidateData.isEmpty(email)) {
            studentToEdit.addEmail(email);
        }

        mathTestSystem.editStudent(studentToEdit);
        UserInterface.toastNotifyUser(THIS_ACTIVITY, firstName+" has been edited");
    }

}