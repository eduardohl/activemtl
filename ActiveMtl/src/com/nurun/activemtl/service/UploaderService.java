package com.nurun.activemtl.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.app.IntentService;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.nurun.activemtl.ActiveMtlApplication;
import com.nurun.activemtl.R;
import com.nurun.activemtl.controller.EventController;
import com.nurun.activemtl.model.EventType;
import com.nurun.activemtl.util.BitmapUtil;

public class UploaderService extends IntentService {

    private static final String EXTRA_IMAGE_URI = "EXTRA_IMAGE_URI";
    private static final String EXTRA_NAME = "EXTRA_NAME";
    private static final String EXTRA_DESCRIPTION = "EXTRA_DESCRIPTION";
    private static final String EXTRA_LATLONG = "EXTRA_LATLONG";
    private static final String EXTRA_EVENT_TYPE = "EXTRA_EVENT_TYPE";
    private static final String EXTRA_CATEGORY = "EXTRA_CATEGORY";
    private EventController courtController;

    public static final String INTENT_ACTION_SUCCESS = "UPLOADER_SERVICE_SUCCESS";
    public static final String INTENT_ACTION_FAILURE = "UPLOADER_SERVICE_FAILURE";

    private static final int ID = 13072013;

    private static final int PROGRESS_MAX = 100;
    private static final int PROGRESS_MIN = 0;

    public UploaderService() {
        super(UploaderService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        courtController = (EventController) getApplicationContext().getSystemService(ActiveMtlApplication.EVENT_CONTROLLER);
    }

    public static Intent newIntent(Context context, String imageUri, String name, String description, Location lastLocation, EventType eventType, String category) {
        double[] latLong = { lastLocation.getLatitude(), lastLocation.getLongitude() };
        return new Intent(context, UploaderService.class).putExtra(EXTRA_IMAGE_URI, imageUri).putExtra(EXTRA_NAME, name)
                .putExtra(EXTRA_DESCRIPTION, description).putExtra(EXTRA_LATLONG, latLong).putExtra(EXTRA_EVENT_TYPE, eventType).putExtra(EXTRA_CATEGORY, category);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String fileUri = intent.getStringExtra(EXTRA_IMAGE_URI);
        String name = intent.getStringExtra(EXTRA_NAME);
        String description = intent.getStringExtra(EXTRA_DESCRIPTION);
        double[] latLong = intent.getDoubleArrayExtra(EXTRA_LATLONG);
        EventType eventType = (EventType) intent.getSerializableExtra(EXTRA_EVENT_TYPE);
        String category = intent.getStringExtra(EXTRA_CATEGORY);
        InputStream inputStream = getInputStream(fileUri);
        if (inputStream != null) {
            try {
                updateNotification(PROGRESS_MIN);
                
                courtController.addSuggestedEvent(name, description, fileUri, latLong, eventType, category);
                updateNotification(PROGRESS_MAX);
            } catch (RuntimeException e) {
                Log.e(getClass().getSimpleName(), e.getMessage(), e);
                updateNotification(-1);
            }
        }
    }

    private InputStream getInputStream(String fileUri) {
        Bitmap resizedBitmap = BitmapUtil.getResizedBitmap(Uri.parse(fileUri));
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        resizedBitmap.compress(CompressFormat.PNG, 0, stream);
        return new ByteArrayInputStream(stream.toByteArray());
    }

    private void updateNotification(final int progress) {
        final String title = getString(R.string.court_suggestion_title);

        final Context context = getApplicationContext();
        Builder builder = new Notification.Builder(context).setSmallIcon(R.drawable.icon).setContentTitle(title).setDefaults(Notification.DEFAULT_ALL);

        switch (progress) {
        case PROGRESS_MAX:
            builder.setContentText(getString(R.string.court_suggestion_success)).setProgress(0, 0, false);
            break;
        case -1:
            builder.setContentText(getString(R.string.court_suggestion_failure)).setProgress(0, 0, false);
            break;
        default:
            builder.setContentText(getString(R.string.court_suggestion_progress)).setProgress(PROGRESS_MAX, progress, true);
            break;
        }
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(ID, builder.build());
    }
}
