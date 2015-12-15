package slidenerd.vivz.bucketdrops.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import slidenerd.vivz.bucketdrops.R;
import slidenerd.vivz.bucketdrops.extras.Util;


/**
 * Created by vivz on 26/10/15.
 */
public class CustomDatePicker extends LinearLayout implements View.OnTouchListener {

    public static final int TOP = 1;
    public static final int BOTTOM = 3;

    public static final int DAY = 0;
    public static final int MONTH = 1;
    public static final int YEAR = 2;

    private SimpleDateFormat mFormatter;
    private Calendar mCalendar;

    private String[] mMonthNames;
    private Context mContext;
    private TextView mTextMonth;
    private TextView mTextDay;
    private TextView mTextYear;
    private Typeface mTypeface;
    private int[] point = new int[2];
    private Drawable[] mCompoundDrawables = new Drawable[4];

    private String mCurrentDate;
    private int mCurrentYear;
    private int mCurrentDay;
    private int mCurrentMonth;

    public CustomDatePicker(Context context) {
        super(context);
        init(context);
    }

    public CustomDatePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomDatePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomDatePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void init(Context context) {
        mContext = context;
        mMonthNames = getResources().getStringArray(R.array.months);
        mTypeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/raleway_thin.ttf");
        mFormatter = new SimpleDateFormat("yyyy-MM-dd");
        mCalendar = Calendar.getInstance();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.custom_date_picker, this);
        mTextDay = (TextView) view.findViewById(R.id.date_display);
        mTextMonth = (TextView) view.findViewById(R.id.month_display);
        mTextYear = (TextView) view.findViewById(R.id.year_display);

        mTextDay.setTypeface(mTypeface);
        mTextMonth.setTypeface(mTypeface);
        mTextYear.setTypeface(mTypeface);

        try {
            mCurrentYear = mCalendar.get(Calendar.YEAR);
            mCurrentMonth = mCalendar.get(Calendar.MONTH);
            mCurrentDay = mCalendar.get(Calendar.DATE);

            Log.d("VIVZ", mCurrentMonth + "");
            mCurrentDate = mCurrentYear + "-" + (mCurrentMonth + 1) + "-" + mCurrentDay;
            mCalendar.setTime(mFormatter.parse(mCurrentDate));
            Log.d("VIVZ", mCalendar.getTime().toString());
            mTextYear.setText(mCurrentYear + "");
            mTextMonth.setText(mMonthNames[mCurrentMonth]);
            mTextDay.setText(mCurrentDay + "");

            mTextDay.setOnTouchListener(this);
            mTextMonth.setOnTouchListener(this);
            mTextYear.setOnTouchListener(this);
        } catch (ParseException e) {
            Log.d("VIVZ", "parsing error");
        }


    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.date_display) {
            processEventsFor(mTextDay, event, DAY);
        }
        if (v.getId() == R.id.month_display) {
            processEventsFor(mTextMonth, event, MONTH);
        }
        if (v.getId() == R.id.year_display) {
            processEventsFor(mTextYear, event, YEAR);
        }
        return true;
    }

    public void processEventsFor(TextView textView, MotionEvent event, int quantity) {
        textView.getLocationInWindow(point);
        mCompoundDrawables = textView.getCompoundDrawables();
        Rect topBounds = mCompoundDrawables[TOP].getBounds();
        Rect bottomBounds = mCompoundDrawables[BOTTOM].getBounds();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (event.getRawY() < point[1] + topBounds.height()) {
                    // your action for drawable click event
                    toggleDrawable(textView, TOP, true);
                }
                if (event.getRawY() > point[1] + textView.getHeight() - bottomBounds.height()) {
                    toggleDrawable(textView, BOTTOM, true);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (event.getRawY() < point[1] + topBounds.height()) {
                    // your action for drawable click event
                    toggleDrawable(textView, TOP, false);
                    increment(quantity);
                }
                if (event.getRawY() > point[1] + textView.getHeight() - bottomBounds.height()) {
                    toggleDrawable(textView, BOTTOM, false);
                    decrement(quantity);
                }
                break;
        }
    }

    private void increment(int quantity) {
        switch (quantity) {
            case DAY:
                mCalendar.add(Calendar.DATE, 1);
                break;
            case MONTH:
                mCalendar.add(Calendar.MONTH, 1);
                break;
            case YEAR:
                mCalendar.add(Calendar.YEAR, 1);
                break;
        }
        mCurrentDay = mCalendar.get(Calendar.DATE);
        mCurrentMonth = mCalendar.get(Calendar.MONTH);
        mCurrentYear = mCalendar.get(Calendar.YEAR);
        mTextDay.setText(mCurrentDay + "");
        mTextMonth.setText(mMonthNames[mCurrentMonth]);
        mTextYear.setText(mCurrentYear + "");
    }

    private void decrement(int quantity) {
        switch (quantity) {
            case DAY:
                mCalendar.add(Calendar.DATE, -1);
                break;
            case MONTH:
                mCalendar.add(Calendar.MONTH, -1);
                break;
            case YEAR:
                mCalendar.add(Calendar.YEAR, -1);
                break;
        }
        mCurrentDay = mCalendar.get(Calendar.DATE);
        mCurrentMonth = mCalendar.get(Calendar.MONTH);
        mCurrentYear = mCalendar.get(Calendar.YEAR);
        mTextDay.setText(mCurrentDay + "");
        mTextMonth.setText(mMonthNames[mCurrentMonth]);
        mTextYear.setText(mCurrentYear + "");
    }


    private void toggleDrawable(TextView textView, int index, boolean pressed) {
        Drawable[] drawables = textView.getCompoundDrawables();
        Resources resources = getResources();
        Resources.Theme theme = mContext.getTheme();
        Drawable drawable;
        if (index == TOP) {
            if (Util.isLollipopOrMore()) {
                drawable = resources.getDrawable(pressed ? R.drawable.drawable_top_pressed : R.drawable.drawable_top_normal, theme);
            } else {
                drawable = resources.getDrawable(pressed ? R.drawable.drawable_top_pressed : R.drawable.drawable_top_normal);
            }
            textView.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawable, drawables[2], drawables[3]);
        } else if (index == BOTTOM) {
            if (Util.isLollipopOrMore()) {
                drawable = resources.getDrawable(pressed ? R.drawable.drawable_bottom_pressed : R.drawable.drawable_bottom_normal, theme);
            } else {
                drawable = resources.getDrawable(pressed ? R.drawable.drawable_bottom_pressed : R.drawable.drawable_bottom_normal);
            }
            textView.setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], drawables[2], drawable);
        }
    }

    public int getDay() {
        return mCurrentDay;
    }

    public int getMonth() {
        return mCurrentMonth;
    }

    public int getYear() {
        return mCurrentYear;
    }

}