package curtin.edu.math_test.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Conversion {

    public static byte[] bitmapToBytes(Bitmap imgBitmap) {
        byte[] imgBytes = null;

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            imgBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            imgBytes = outputStream.toByteArray();
            outputStream.close();
        } catch (IOException e) {
            Log.e("IOException - bitmapToBytes()", e.getMessage());
            e.printStackTrace();
        }

        return imgBytes;
    }

    public static Bitmap bytesToBitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

}
