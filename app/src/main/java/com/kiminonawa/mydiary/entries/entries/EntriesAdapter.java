package com.kiminonawa.mydiary.entries.entries;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kiminonawa.mydiary.R;
import com.kiminonawa.mydiary.entries.diary.DiaryInfo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by daxia on 2016/10/17.
 */

public class EntriesAdapter extends RecyclerView.Adapter<EntriesAdapter.EntriesViewHolder> {


    private List<EntriesEntity> entriesList;
    private Fragment mFragment;
    private DateFormat dateFormat = new SimpleDateFormat("HH:mm");
    private String[] daysSimpleName;
    private DiaryViewerDialogFragment.DiaryViewerCallback mDiaryViewerCallback;

    public EntriesAdapter(Fragment fragment, List<EntriesEntity> topicList, DiaryViewerDialogFragment.DiaryViewerCallback callback) {
        this.mFragment = fragment;
        this.entriesList = topicList;
        this.mDiaryViewerCallback = callback;
        daysSimpleName = mFragment.getResources().getStringArray(R.array.days_simple_name);
    }


    @Override
    public EntriesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_entries_item, parent, false);
        return new EntriesViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return entriesList.size();
    }

    @Override
    public void onBindViewHolder(EntriesViewHolder holder, final int position) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(entriesList.get(position).getCreateDate());

        if (showHeader(position)) {
            holder.getHeader().setVisibility(View.VISIBLE);
            holder.getHeader().setText(String.valueOf(calendar.get(Calendar.MONTH) + 1));
        } else {
            holder.getHeader().setVisibility(View.GONE);
        }

        holder.getTVDate().setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
        holder.getTVDay().setText(daysSimpleName[calendar.get(Calendar.DAY_OF_WEEK) - 1]);
        holder.getTVTime().setText(String.valueOf(dateFormat.format(calendar.getTime())));
        holder.getTVTitle().setText(entriesList.get(position).getTitle());
        holder.getTVSummary().setText(entriesList.get(position).getSummary());

        holder.getIVWeather().setImageResource(DiaryInfo.getWeathetrResourceId(entriesList.get(position).getWeatherId()));
        holder.getIVMood().setImageResource(DiaryInfo.getMoodResourceId(entriesList.get(position).getMoodId()));

        if (entriesList.get(position).hasAttachment()) {
            holder.getIVAttachment().setVisibility(View.VISIBLE);
        } else {
            holder.getIVAttachment().setVisibility(View.GONE);
        }
        holder.getRootView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DiaryViewerDialogFragment diaryViewerDialog =
                        DiaryViewerDialogFragment.newInstance(entriesList.get(position).getId());
                diaryViewerDialog.setCallBack(mDiaryViewerCallback);
                diaryViewerDialog.show(mFragment.getFragmentManager(), "diaryViewerDialog");
            }
        });
    }

    private boolean showHeader(final int position) {
        if (position == 0) {
            return true;
        } else {
            Calendar previousCalendar = new GregorianCalendar();
            previousCalendar.setTime(entriesList.get(position - 1).getCreateDate());
            Calendar currentCalendar = new GregorianCalendar();
            currentCalendar.setTime(entriesList.get(position).getCreateDate());
            if (previousCalendar.get(Calendar.MONTH) != currentCalendar.get(Calendar.MONTH)) {
                return true;
            } else {
                return false;
            }
        }
    }

    protected class EntriesViewHolder extends RecyclerView.ViewHolder {

        private TextView TV_entries_item_header;
        private TextView TV_entries_item_date, TV_entries_item_day, TV_entries_item_time,
                TV_entries_item_title, TV_entries_item_summary;
        private View rootView;

        private ImageView IV_entries_item_weather, IV_entries_item_mood, IV_entries_item_bookmark, IV_entries_item_attachment;

        protected EntriesViewHolder(View view) {
            super(view);
            this.rootView = view;
            this.TV_entries_item_header = (TextView) rootView.findViewById(R.id.TV_entries_item_header);

            this.TV_entries_item_date = (TextView) rootView.findViewById(R.id.TV_entries_item_date);
            this.TV_entries_item_day = (TextView) rootView.findViewById(R.id.TV_entries_item_day);
            this.TV_entries_item_time = (TextView) rootView.findViewById(R.id.TV_entries_item_time);
            this.TV_entries_item_title = (TextView) rootView.findViewById(R.id.TV_entries_item_title);
            this.TV_entries_item_summary = (TextView) rootView.findViewById(R.id.TV_entries_item_summary);
            this.IV_entries_item_weather = (ImageView) rootView.findViewById(R.id.IV_entries_item_weather);
            this.IV_entries_item_mood = (ImageView) rootView.findViewById(R.id.IV_entries_item_mood);
            this.IV_entries_item_bookmark = (ImageView) rootView.findViewById(R.id.IV_entries_item_bookmark);
            this.IV_entries_item_attachment = (ImageView) rootView.findViewById(R.id.IV_entries_item_attachment);
        }

        public ImageView getIVWeather() {
            return IV_entries_item_weather;
        }

        public ImageView getIVMood() {
            return IV_entries_item_mood;
        }

        public ImageView getIVBookmark() {
            return IV_entries_item_bookmark;
        }

        public TextView getHeader() {
            return TV_entries_item_header;
        }

        public TextView getTVDate() {
            return TV_entries_item_date;
        }

        public TextView getTVDay() {
            return TV_entries_item_day;
        }

        public TextView getTVTime() {
            return TV_entries_item_time;
        }

        public TextView getTVTitle() {
            return TV_entries_item_title;
        }

        public TextView getTVSummary() {
            return TV_entries_item_summary;
        }

        public ImageView getIVAttachment() {
            return IV_entries_item_attachment;
        }

        public View getRootView() {
            return rootView;
        }
    }
}
