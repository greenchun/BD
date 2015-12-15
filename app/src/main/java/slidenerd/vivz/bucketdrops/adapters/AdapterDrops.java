package slidenerd.vivz.bucketdrops.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmResults;
import slidenerd.vivz.bucketdrops.R;
import slidenerd.vivz.bucketdrops.beans.Drop;
import slidenerd.vivz.bucketdrops.extras.Util;
import slidenerd.vivz.bucketdrops.home.BucketDropsApp;

/**
 * Created by vivz on 18/07/15.
 */
public class AdapterDrops extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnSwipeListener {
    public static final int ITEM = 1;
    public static final int NO_ITEM = 2;
    public static final int FOOTER = 3;
    private Context mContext;
    private LayoutInflater mInflater;
    private FooterClickListener mFooterClickListener;
    private MarkListener mMarkListener;
    private Realm mRealm;
    private RealmResults<Drop> mResults;
    private int mSort;

    public AdapterDrops(Context context, Realm realm, RealmResults<Drop> results) {
        mRealm = realm;
        mContext = context;
        mInflater = LayoutInflater.from(context);
        updateResults(results);
    }

    public void updateResults(RealmResults<Drop> results) {
        mResults = results;
        mSort = BucketDropsApp.loadSortOption();
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        if (mResults == null) {
            return RecyclerView.NO_ID;
        } else {
            return mResults.get(position).getAdded();
        }
    }

    public void setDropClickListener(MarkListener listener) {
        mMarkListener = listener;
    }

    public void setOnFooterClickListener(FooterClickListener listener) {
        mFooterClickListener = listener;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof DropHolder) {
            DropHolder dropHolder = (DropHolder) viewHolder;
            Drop drop = mResults.get(position);
            dropHolder.setWhat(drop.getWhat());
            dropHolder.setWhen(drop.getWhen());
            dropHolder.setBackground(drop.isCompleted());
        }
    }

    /**
     * Returns the total number of items in the data set hold by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        if (mResults == null) {
            return 0;
        } else if (mResults.isEmpty()) {
            if (mSort == SortOptions.SORT_DEFAULT) {
                return 0;
            } else {
                return 2;
            }
        } else {
            return mResults.size() + 1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mResults == null) {
            return ITEM;
        } else if (mResults.isEmpty()) {
            if (mSort == SortOptions.SORT_DEFAULT) {
                return ITEM;
            } else {
                if (position == 0) {
                    return NO_ITEM;
                } else {
                    return FOOTER;
                }
            }
        } else {
            if (position < mResults.size()) {
                return ITEM;
            } else {
                return FOOTER;
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == FOOTER) {
            View root = mInflater.inflate(R.layout.footer, parent, false);
            FooterHolder viewHolder = new FooterHolder(root);
            return viewHolder;

        } else if (viewType == NO_ITEM) {
            View root = mInflater.inflate(R.layout.no_item, parent, false);
            NoneHolder viewHolder = new NoneHolder(root);
            return viewHolder;
        } else {
            View root = mInflater.inflate(R.layout.item, parent, false);
            DropHolder dropHolder = new DropHolder(root);
            return dropHolder;
        }
    }

    public void markComplete(int position) {
        if (position < mResults.size()) {
            Drop drop = mResults.get(position);
            mRealm.beginTransaction();
            drop.setCompleted(true);
            notifyItemChanged(position);
            mRealm.commitTransaction();
        }
    }

    /**
     * @param position the position of the item that was swiped within the RecyclerView
     */
    @Override
    public void onSwipe(int position) {
        if (position < mResults.size()) {
            mRealm.beginTransaction();
            mResults.get(position).removeFromRealm();
            mRealm.commitTransaction();
            notifyItemRemoved(position);
        }
    }

    public void add(Drop drop) {
        mRealm.beginTransaction();
        mRealm.copyToRealmOrUpdate(drop);
        mRealm.commitTransaction();
        notifyDataSetChanged();
    }

    /**
     * An interface that notifies your class when the footer is clicked inside the RecyclerView through its onClickFooter method
     */
    public interface FooterClickListener {
        void onClickFooter();
    }

    /**
     * An interface that notifies your class when any item is clicked from your RecyclerView
     */
    public interface MarkListener {
        /**
         * @param position is the position of the item that was clicked by the user inside the RecylerView
         */
        void onMark(int position);
    }

    public class NoneHolder extends RecyclerView.ViewHolder {

        public NoneHolder(View itemView) {
            super(itemView);
        }
    }

    public class DropHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTextWhat;
        private TextView mTextWhen;
        private View mRoot;

        public DropHolder(View itemView) {
            super(itemView);
            mRoot = itemView;
            mTextWhat = (TextView) mRoot.findViewById(R.id.text_what);
            mTextWhen = (TextView) mRoot.findViewById(R.id.text_when);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            if (mMarkListener != null) {
                //Notify interested classes about the item that was clicked at the current position
                mMarkListener.onMark(getAdapterPosition());
            }
        }

        public void setWhat(String text) {
            mTextWhat.setText(text);
        }

        public void setWhen(long when) {
            mTextWhen.setText(Util.getFormattedDate(when));
        }

        @SuppressLint("NewApi")
        public void setBackground(boolean isCompleted) {
            //For items whose status is marked as complete, we would like to highlight them with a different background
            Drawable background = null;
            if (isCompleted) {
                background = new ColorDrawable(Color.argb(237, 142, 121, 187));
            } else {
                background = mContext.getResources().getDrawable(R.drawable.bg_bucket_item);
            }
            if (Util.isJellyBeanOrMore()) {
                mRoot.setBackgroundDrawable(background);
            } else {
                mRoot.setBackground(background);
            }
        }
    }

    public class FooterHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Button mBtnAddDrop;

        public FooterHolder(View itemView) {
            super(itemView);
            mBtnAddDrop = (Button) itemView.findViewById(R.id.btn_add_drop);
            mBtnAddDrop.setTypeface(Util.loadRalewayThin(mContext));
            mBtnAddDrop.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            if (mFooterClickListener != null) {
                //Notify interested classes about the footer that was clicked at the specified position.
                mFooterClickListener.onClickFooter();
            }
        }
    }
}
