package com.kiminonawa.mydiary.entries.diary;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.kiminonawa.mydiary.R;
import com.kiminonawa.mydiary.db.DBManager;
import com.kiminonawa.mydiary.entries.BaseDiaryFragment;
import com.kiminonawa.mydiary.entries.DiaryActivity;
import com.kiminonawa.mydiary.shared.SPFManager;
import com.kiminonawa.mydiary.shared.TimeTools;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;


/**
 * This page doesn't be used in the movie.
 * I define this page for write diary.
 */

public class DiaryFragment extends BaseDiaryFragment implements View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener, LocationListener {


    /**
     * UI
     */
    private LinearLayout LL_diary_time_information;
    private SwipeRefreshLayout SRL_diary_content;
    private TextView TV_diary_month, TV_diary_date, TV_diary_day, TV_diary_time, TV_diary_location;
    private Spinner SP_diary_weather, SP_diary_mood;
    private EditText EDT_diary_title, EDT_diary_content;
    private ImageView IV_diary_menu, IV_diary_location, IV_diary_photo, IV_diary_delete, IV_diary_clear, IV_diary_save;

    /**
     * Location
     */
    private String noLocation;
    private Location diaryLocations = null;
    private LocationManager locationManager;
    private String locationProvider;
    private DiaryHandler diaryHandler = new DiaryHandler(this);
    private boolean isLocation;
    /**
     * Permission
     */
    private static final int REQUEST_ACCESS_FINE_LOCATION_PERMISSION = 1;

    /**
     * Time
     */
    private Calendar calendar;
    private Date nowDate;
    private TimeTools timeTools;
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        calendar = Calendar.getInstance();
        timeTools = TimeTools.getInstance(getActivity().getApplicationContext());
        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        locationProvider = LocationManager.NETWORK_PROVIDER;
        isLocation = SPFManager.getDiaryLocation(getActivity());
        noLocation = getActivity().getString(R.string.diary_no_location);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_diary, container, false);

        LL_diary_time_information = (LinearLayout) rootView.findViewById(R.id.LL_diary_time_information);
        SRL_diary_content = (SwipeRefreshLayout) rootView.findViewById(R.id.SRL_diary_content);
        SRL_diary_content.setOnRefreshListener(this);

        TV_diary_month = (TextView) rootView.findViewById(R.id.TV_diary_month);
        TV_diary_date = (TextView) rootView.findViewById(R.id.TV_diary_date);
        TV_diary_day = (TextView) rootView.findViewById(R.id.TV_diary_day);
        TV_diary_time = (TextView) rootView.findViewById(R.id.TV_diary_time);
        TV_diary_location = (TextView) rootView.findViewById(R.id.TV_diary_location);

        SP_diary_weather = (Spinner) rootView.findViewById(R.id.SP_diary_weather);
        SP_diary_weather.setVisibility(View.VISIBLE);
        SP_diary_mood = (Spinner) rootView.findViewById(R.id.SP_diary_mood);
        SP_diary_mood.setVisibility(View.VISIBLE);

        EDT_diary_title = (EditText) rootView.findViewById(R.id.EDT_diary_title);
        EDT_diary_content = (EditText) rootView.findViewById(R.id.EDT_diary_content);


        IV_diary_menu = (ImageView) rootView.findViewById(R.id.IV_diary_menu);
        IV_diary_location = (ImageView) rootView.findViewById(R.id.IV_diary_location);
        IV_diary_location.setOnClickListener(this);
        IV_diary_photo = (ImageView) rootView.findViewById(R.id.IV_diary_photo);
        IV_diary_delete = (ImageView) rootView.findViewById(R.id.IV_diary_delete);
        IV_diary_clear = (ImageView) rootView.findViewById(R.id.IV_diary_clear);
        IV_diary_clear.setOnClickListener(this);
        IV_diary_save = (ImageView) rootView.findViewById(R.id.IV_diary_save);
        IV_diary_save.setOnClickListener(this);

        IV_diary_delete.setVisibility(View.GONE);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initLocationIcon();
        initWeatherSpinner();
        initMoodSpinner();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(this);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        diaryHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ACCESS_FINE_LOCATION_PERMISSION:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initDiaryInfo(true);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.diary_location_permission_title))
                            .setMessage(getString(R.string.diary_location_permission_content))
                            .setPositiveButton(getString(R.string.dialog_button_ok), null);
                    builder.show();
                    SRL_diary_content.setRefreshing(false);
                    setCurrentTime();
                    TV_diary_location.setText(noLocation);
                }
                break;
        }
    }


    private boolean checkPermission(final int requestCode) {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        requestCode);
                return false;
            } else { // No explanation needed, we can request the permission.
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        requestCode);
                return false;
            }
        }
        return true;
    }


    private void initDiaryInfo(boolean showRefreshing) {
        if (isLocation && locationManager.isProviderEnabled(locationProvider)) {
            if (showRefreshing) {
                diaryHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        SRL_diary_content.setRefreshing(true);
                    }
                });
            }
            if (checkPermission(REQUEST_ACCESS_FINE_LOCATION_PERMISSION)) {
                if (locationManager.isProviderEnabled(locationProvider)) {
                    locationManager.requestLocationUpdates(locationProvider, 3000, 0, this);
                }
                //Waiting gps max timeout is 20s
                diaryHandler.sendEmptyMessageDelayed(0, 20000);
            }
        } else {
            setCurrentTime();
            TV_diary_location.setText(noLocation);
            SRL_diary_content.setRefreshing(false);
        }
    }


    private void setCurrentTime() {
        nowDate = new Date();
        calendar.setTime(nowDate);
        TV_diary_month.setText(timeTools.getMonthsFullName()[calendar.get(Calendar.MONTH)]);
        TV_diary_date.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
        TV_diary_day.setText(timeTools.getDaysFullName()[calendar.get(Calendar.DAY_OF_WEEK) - 1]);
        TV_diary_time.setText(sdf.format(calendar.getTime()));
    }


    private void initLocationIcon() {
        if (isLocation) {
            IV_diary_location.setImageResource(R.drawable.ic_location_on_white_24dp);
            initDiaryInfo(true);
        } else {
            SRL_diary_content.setRefreshing(false);
            diaryHandler.removeCallbacksAndMessages(null);
            IV_diary_location.setImageResource(R.drawable.ic_location_off_white_24dp);
            setCurrentTime();
            TV_diary_location.setText(noLocation);
        }
    }

    private void initWeatherSpinner() {
        ImageArrayAdapter weatherArrayAdapter = new ImageArrayAdapter(getActivity(), DiaryInfo.getWeatherArray());
        SP_diary_weather.setAdapter(weatherArrayAdapter);
    }

    private void initMoodSpinner() {
        ImageArrayAdapter moodArrayAdapter = new ImageArrayAdapter(getActivity(), DiaryInfo.getMoodArray());
        SP_diary_mood.setAdapter(moodArrayAdapter);
    }

    private void clearDiary() {
        EDT_diary_title.setText("");
        EDT_diary_content.setText("");
    }

    private void saveDiary() {
        String locationName = TV_diary_location.getText().toString();
        if (noLocation.equals(locationName)) {
            locationName = "";
        }
        DBManager dbManager = new DBManager(getActivity());
        dbManager.opeDB();
        dbManager.insetDiary(calendar.getTimeInMillis(),
                EDT_diary_title.getText().toString(), EDT_diary_content.getText().toString(),
                SP_diary_mood.getSelectedItemPosition(), SP_diary_weather.getSelectedItemPosition(),
                false, getTopicId(), locationName);
        dbManager.closeDB();
        clearDiary();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.IV_diary_location:
                isLocation = !isLocation;
                SPFManager.setDiaryLocation(getActivity(), isLocation);
                initLocationIcon();
                break;
            case R.id.IV_diary_clear:
                clearDiary();
                break;
            case R.id.IV_diary_save:
                if (EDT_diary_title.length() > 0 && EDT_diary_content.length() > 0) {
                    saveDiary();
                    ((DiaryActivity) getActivity()).gotoPage(0);
                } else if (SRL_diary_content.isRefreshing()) {
                    Toast.makeText(getActivity(), getString(R.string.toast_refashioning), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.toast_diary_empty), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onRefresh() {
        initDiaryInfo(false);
    }


    @Override
    public void onLocationChanged(Location location) {
//        Log.d("test", "onLocationCganged");
        diaryLocations = new Location(location);
        diaryHandler.removeCallbacksAndMessages(null);
        diaryHandler.sendEmptyMessage(0);
        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private static class DiaryHandler extends Handler {

        private WeakReference<DiaryFragment> mFrag;

        DiaryHandler(DiaryFragment aFragment) {
            mFrag = new WeakReference<>(aFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            DiaryFragment theFrag = mFrag.get();
            if (theFrag != null) {
                theFrag.setCurrentTime();
                theFrag.TV_diary_location.setText(getLocationName(theFrag));
                theFrag.SRL_diary_content.setRefreshing(false);
            }
        }

        private String getLocationName(DiaryFragment theFrag) {
            String returnLocation = theFrag.noLocation;
            if (theFrag.diaryLocations == null) {
                try {
                    theFrag.diaryLocations = theFrag.locationManager.getLastKnownLocation(theFrag.locationProvider);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
            List<String> providerList = theFrag.locationManager.getAllProviders();
            try {
                if (null != theFrag.diaryLocations && null != providerList && providerList.size() > 0) {
                    double longitude = theFrag.diaryLocations.getLongitude();
                    double latitude = theFrag.diaryLocations.getLatitude();
                    Geocoder geocoder = new Geocoder(theFrag.getActivity().getApplicationContext(), Locale.getDefault());
                    List<Address> listAddresses = geocoder.getFromLocation(latitude, longitude, 1);
                    if (null != listAddresses && listAddresses.size() > 0) {
                        returnLocation = listAddresses.get(0).getAddressLine(0);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                theFrag.diaryLocations = null;
            }
            return returnLocation;
        }
    }
}
