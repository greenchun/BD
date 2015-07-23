package slidenerd.vivz.bucketdrops.home;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;

import java.util.Calendar;
import java.util.Locale;

import slidenerd.vivz.bucketdrops.R;
import slidenerd.vivz.bucketdrops.adapters.OnAddDropListener;
import slidenerd.vivz.bucketdrops.beans.Drop;
import slidenerd.vivz.bucketdrops.extras.Util;


public class DialogAdd extends DialogFragment implements View.OnClickListener, TextView.OnEditorActionListener {

    private Activity mContext;
    //Title of the dialog
    private TextView mTextTitle;
    //The close button for this dialog
    private ImageButton mBtnClose;
    //The area where the user can type his/her goal
    private EditText mInputWhat;
    //The control with which user can select the date for his/her goal by which they feel they wanna accomplish their goal
    private DatePicker mInputWhen;
    //The button clicking which the goal and date will be added to the database
    private Button mBtnAddDrop;
    //The object which will be notified when the user hits the "Add Drop" button
    private OnAddDropListener mOnAddDropListener;

    public void setAddDropListener(OnAddDropListener OnAddDropListener) {
        mOnAddDropListener = OnAddDropListener;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //init UI elements except the date picker
        initViews(view);
        //init the date picker
        initDatePicker(view);
        //monitor the user clicking buttons such as DONE on the virtual keyboard
        mInputWhat.setOnEditorActionListener(this);
        mBtnClose.setOnClickListener(this);
        mBtnAddDrop.setOnClickListener(this);

        //load custom fonts wherever appropriate
        initCustomFont();
    }

    private void initViews(View view) {
        mTextTitle = (TextView) view.findViewById(R.id.text_dialog_title);
        mInputWhat = (EditText) view.findViewById(R.id.input_task);
        mBtnClose = (ImageButton) view.findViewById(R.id.btn_dialog_close);
        mBtnAddDrop = (Button) view.findViewById(R.id.btn_add_drop);

    }

    private void initDatePicker(View view) {
        mInputWhen = (DatePicker) view.findViewById(R.id.input_time);
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
    }

    private void initCustomFont() {
        mTextTitle.setTypeface(Util.loadRalewayRegular(mContext));
        mInputWhat.setTypeface(Util.loadRalewayThin(mContext));
        mBtnAddDrop.setTypeface(Util.loadRalewayThin(mContext));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_drop:
                addAction();
                break;
        }
        dismiss();
    }

    private void addAction() {
        if (mOnAddDropListener != null) {
            //Load the taskname, convert the user entered date to a specific value of 0 hours 0 minutes and 0 seconds, 12 am precisely on the day they want things to be done
            String taskName = mInputWhat.getText().toString();
            int dayOfMonth = mInputWhen.getDayOfMonth();
            int month = mInputWhen.getMonth();
            int year = mInputWhen.getYear();
            LocalDateTime localDateTime = new LocalDateTime(year, month + 1, dayOfMonth, 0, 0, 0);
            DateTimeZone timeZone = DateTimeZone.getDefault();
            long currentTime = System.currentTimeMillis();
            long when = localDateTime.toDateTime(timeZone).getMillis();
            if (when < currentTime) {
                Toast.makeText(getActivity(), "Right Today? Are You Serious!", Toast.LENGTH_LONG).show();
            } else {
                Drop drop = new Drop(taskName, System.currentTimeMillis(), when, false);
                mOnAddDropListener.onClickAddDrop(drop);
            }
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            //Hide the keyboard when the user presses done on it
            mInputWhen.requestFocus();
            InputMethodManager imm = (InputMethodManager) mContext.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mInputWhat.getWindowToken(), 0);
            return true;
        }
        return false;
    }
}
