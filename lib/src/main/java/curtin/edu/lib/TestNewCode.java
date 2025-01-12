package curtin.edu.lib;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class TestNewCode {

    private static class TestDate {

        private final String[] months = {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        };

        private final String date;
        private final String time;

        public TestDate() {
            GregorianCalendar gregCal = new GregorianCalendar();
            date = String.format(Locale.getDefault(), "%s %d %d",
                months[gregCal.get(Calendar.MONTH)],
                gregCal.get(Calendar.DATE),
                gregCal.get(Calendar.YEAR)
            );
            time = String.format(Locale.getDefault(), "%d:%d:%d %s",
                gregCal.get(Calendar.HOUR),
                gregCal.get(Calendar.MINUTE),
                gregCal.get(Calendar.SECOND),
                Calendar.AM == Calendar.AM_PM ? "am" : "pm"
            );
        }

        public String getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }

    }  // END OF TestDate

    public static void main(String[] args) {
        TestDate testDate = new TestDate();
        String date = testDate.getDate();
        String time = testDate.getTime();
        System.out.println("Date: " + date + "\nTime: " + time);
    }

}
