package curtin.edu.math_test.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * UI related interactions that are used by almost every activities
 */
public class UserInterface {

    public static void toastNotifyUser(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

}
