package curtin.edu.math_test.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

public class RegisterStudent extends AppCompatActivity {

    private final Activity THIS_ACTIVITY = RegisterStudent.this;

    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_TAKE_PHOTO = 2;
    private static final int REQUEST_READ_CONTACT_PERMISSION = 3;
    private static final int REQUEST_ACCESS_PHOTO = 4;

    private MathTestSystem mathTestSystem;

    private int contactId;
    private Student studentToRegister;

    private File photoFile;

    private ImageView studentImage;
    private EditText firstNameInput;
    private EditText lastNameInput;
    private EditText phoneNumInput;
    private EditText emailInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_student);

        mathTestSystem = MathTestSystem.getInstance(THIS_ACTIVITY);

        studentImage = findViewById(R.id.studentImage);
        firstNameInput = findViewById(R.id.firstNameInput);
        lastNameInput = findViewById(R.id.lastNameInput);
        phoneNumInput = findViewById(R.id.phoneNumInput);
        emailInput = findViewById(R.id.emailInput);

        Button addPhoneButton = findViewById(R.id.addPhoneButton);
        Button addEmailButton = findViewById(R.id.addEmailButton);
        Button addFromContactsButton = findViewById(R.id.addFromContactsButton);
        ImageButton takePhotoButton = findViewById(R.id.takePhotoButton);
        ImageButton searchGalleryButton = findViewById(R.id.searchGalleryButton);
        ImageButton searchOnlineButton = findViewById(R.id.searchOnlineButton);
        Button registerButton = findViewById(R.id.registerButton);
        Button viewStudentsButton = findViewById(R.id.viewStudentsButton);

        // Pre-construct student to add phone numbers and emails
        studentToRegister = new Student();

        addFromContactsButton.setOnClickListener(view -> {
            if (notGrantedPermission()) {
                createNewRequestPermission();
            } else {
                emptyContactInfoFields();
                addFromContacts();
            }
        });

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
                    studentToRegister.addPhoneNumber(Integer.parseInt(phoneNum));
                    UserInterface.toastNotifyUser(THIS_ACTIVITY, "Phone number added");
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
                    studentToRegister.addEmail(email);
                    UserInterface.toastNotifyUser(THIS_ACTIVITY, "Email added");
                }
            } catch (IllegalArgumentException e) {
                UserInterface.toastNotifyUser(THIS_ACTIVITY, e.getMessage());
            }
        });
        registerButton.setOnClickListener(view -> {
            registerStudent();
        });
        viewStudentsButton.setOnClickListener(view -> {
            Intent intent = new Intent(THIS_ACTIVITY, ViewStudentsActivity.class);
            intent.putExtra("viewType", ViewStudentsActivity.EDITABLE);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Photo picked online
        if (mathTestSystem.getOnlinePhoto() != null) {
            studentImage.setImageBitmap(mathTestSystem.getOnlinePhoto());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (studentImage.getDrawable() != null) {
            mathTestSystem.setOnlinePhoto(
                ((BitmapDrawable)studentImage.getDrawable()).getBitmap());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CONTACT && resultCode == RESULT_OK) {
            getContactId(data);
            getContactName();
            getContactEmail();
            getContactPhoneNum();
        }
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Bitmap photo = BitmapFactory.decodeFile(photoFile.toString());
            studentImage.setImageBitmap(photo);
        }
        if (requestCode == REQUEST_ACCESS_PHOTO && resultCode == RESULT_OK) {
            Uri targetUri = data.getData();
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(
                    getContentResolver().openInputStream(targetUri)
                );
                studentImage.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /* ----- Button Functions ----- */

    private void addFromContacts() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setData(ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, REQUEST_CONTACT);
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
            photoIntent, PackageManager.MATCH_DEFAULT_ONLY)) {

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

    private void registerStudent() {
        try {
            // Set the imageView to the student's image by converting it to bytes
            if (studentImage.getDrawable() != null) {
                studentToRegister.setPhoto(
                    Conversion.bitmapToBytes(
                        ((BitmapDrawable)studentImage.getDrawable()).getBitmap())
                );
            }

            studentToRegister.setFirstName(firstNameInput.getText().toString());
            studentToRegister.setLastName(lastNameInput.getText().toString());

            // Phone and email can be empty but not to be stored
            if (!ValidateData.isEmpty(phoneNumInput.getText().toString())) {
                studentToRegister.addPhoneNumber(
                    Integer.parseInt(phoneNumInput.getText().toString()));
            }
            if (!ValidateData.isEmpty(emailInput.getText().toString())) {
                studentToRegister.addEmail(emailInput.getText().toString());
            }

            mathTestSystem.addStudent(studentToRegister);
            UserInterface.toastNotifyUser(THIS_ACTIVITY,
                firstNameInput.getText().toString() + " has been added");
        } catch (IllegalArgumentException e) {
            UserInterface.toastNotifyUser(THIS_ACTIVITY, e.getMessage());
        }
    }

    /* ----- Contact Info ---- */

    private void getContactId(Intent data) {
        Uri contactUri = data.getData();

        String[] queryFields = new String[]{ContactsContract.Contacts._ID};

        try (Cursor cursor = getContentResolver().query(contactUri, queryFields, null, null, null)) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                this.contactId = cursor.getInt(0);
            }
        } catch (CursorIndexOutOfBoundsException e) {
            Log.e("CursorIndexOutOfBoundsException", e.getMessage()
                + " when getting contact id");
        }
    }

    private void getContactName() {
        Uri dataUri = ContactsContract.Data.CONTENT_URI;

        String[] queryFields = new String[]{
            StructuredName.GIVEN_NAME,
            StructuredName.FAMILY_NAME,
            StructuredName.MIMETYPE
        };

        String whereClause = StructuredName.CONTACT_ID + "=? and " + StructuredName.MIMETYPE + "=?";

        String[] whereValues = new String[]{
            String.valueOf(this.contactId),
            StructuredName.CONTENT_ITEM_TYPE
        };

        try (Cursor c = getContentResolver().query(
                dataUri, queryFields, whereClause, whereValues, null)
        ) {
            c.moveToFirst();
            String firstName = c.getString(0);
            String lastName = c.getString(1);
            firstNameInput.setText(firstName);
            lastNameInput.setText(lastName);
        } catch (CursorIndexOutOfBoundsException e) {
            Log.e("CursorIndexOutOfBoundsException", e.getMessage());
        }
    }

    private void getContactEmail() {
        Uri emailUri = Email.CONTENT_URI;

        String[] emailQueryFields = new String[]{Email.ADDRESS};
        String emailWhereClause = Email.CONTACT_ID + "=?";
        String[] whereValues = new String[]{String.valueOf(this.contactId)};

        try (Cursor emailCursor = getContentResolver().query(
            emailUri, emailQueryFields, emailWhereClause, whereValues, null)) {
            emailCursor.moveToFirst();
            String email = emailCursor.getString(0);
            emailInput.setText(email);
        } catch (CursorIndexOutOfBoundsException e) {
            Log.e("CursorIndexOutOfBoundsException", e.getMessage());
        }
    }

    private void getContactPhoneNum() {
        Uri phoneUri = Phone.CONTENT_URI;

        String[] phoneQueryFields = new String[]{Phone.NUMBER};
        String phoneWhereClause = Phone.CONTACT_ID + "=?";
        String[] whereValues = new String[]{String.valueOf(this.contactId)};

        try (Cursor phoneCursor = getContentResolver().query(
            phoneUri, phoneQueryFields, phoneWhereClause, whereValues, null)) {
            phoneCursor.moveToFirst();
            String phoneNum = phoneCursor.getString(0);
            phoneNumInput.setText(phoneNum);
        } catch (CursorIndexOutOfBoundsException e) {
            Log.e("CursorIndexOutOfBoundsException", e.getMessage());
        }
    }

    /* ----- Permissions ----- */

    /** Check to see if user is already permitted to read contacts otherwise create new request */
    private boolean notGrantedPermission() {
        return (ContextCompat.checkSelfPermission(
            THIS_ACTIVITY, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED);
    }

    /** Request to read contacts */
    private void createNewRequestPermission() {
        ActivityCompat.requestPermissions(
            THIS_ACTIVITY,
            new String[]{Manifest.permission.READ_CONTACTS},
            REQUEST_READ_CONTACT_PERMISSION);
    }

    /* ----- UI ----- */

    private void emptyContactInfoFields() {
        firstNameInput.setText("");
        lastNameInput.setText("");
        phoneNumInput.setText("");
        emailInput.setText("");
    }

}