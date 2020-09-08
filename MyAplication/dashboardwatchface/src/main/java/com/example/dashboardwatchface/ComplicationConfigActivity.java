/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.dashboardwatchface;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.wearable.complications.ComplicationHelperActivity;
import android.support.wearable.complications.ComplicationProviderInfo;
import android.support.wearable.complications.ProviderChooserIntent;
import android.support.wearable.complications.ProviderInfoRetriever;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.util.concurrent.Executors;

/**
 * The watch-side config activity for {@linkComplicationWatchFaceService}, which allows for setting
 * the left and right complications of watch face.
 */
public class ComplicationConfigActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "ConfigActivity";

    static final int COMPLICATION_CONFIG_REQUEST_CODE = 1001;

    /**
     * Used by associated watch face ({@linkComplicationWatchFaceService}) to let this
     * configuration Activity know which complication locations are supported, their ids, and
     * supported complication data types.
     */
    // TODO: Step 3, intro 1
    public enum ComplicationLocation {
        LEFT,
        RIGHT
    }

    private int mLeftComplicationId;
    private int mRightComplicationId;

    // Selected complication id by user.
    private int mSelectedComplicationId;

    // ComponentName used to identify a specific service that renders the watch face.
    private ComponentName mWatchFaceComponentName;

    // Required to retrieve complication data from watch face for preview.
    private ProviderInfoRetriever mProviderInfoRetriever;

    private ImageView mLeftComplicationBackground;
    private ImageView mRightComplicationBackground;

    private ImageButton mLeftComplication;
    private ImageButton mRightComplication;

    private Drawable mDefaultAddComplicationDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_config);

        mDefaultAddComplicationDrawable = getDrawable(R.drawable.add_complication);

        // TODO: Step 3, initialize 1
        mSelectedComplicationId = -1;

        mLeftComplicationId =
                MyWatchFace.getComplicationId(ComplicationLocation.LEFT);
        mRightComplicationId =
                MyWatchFace.getComplicationId(ComplicationLocation.RIGHT);

        mWatchFaceComponentName =
                new ComponentName(getApplicationContext(), MyWatchFace.class);

        // Sets up left complication preview.
        mLeftComplicationBackground = findViewById(R.id.left_complication_background);
        mLeftComplication = findViewById(R.id.left_complication);
        mLeftComplication.setOnClickListener(this);

        // Sets default as "Add Complication" icon.
        mLeftComplication.setImageDrawable(mDefaultAddComplicationDrawable);
        mLeftComplicationBackground.setVisibility(View.INVISIBLE);

        // Sets up right complication preview.
        mRightComplicationBackground = findViewById(R.id.right_complication_background);
        mRightComplication = findViewById(R.id.right_complication);
        mRightComplication.setOnClickListener(this);

        // Sets default as "Add Complication" icon.
        mRightComplication.setImageDrawable(mDefaultAddComplicationDrawable);
        mRightComplicationBackground.setVisibility(View.INVISIBLE);

        // TODO: Step 3, initialize 2
        mProviderInfoRetriever =
                new ProviderInfoRetriever(getApplicationContext(), Executors.newCachedThreadPool());
        mProviderInfoRetriever.init();

        retrieveInitialComplicationsData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // TODO: Step 3, release
        mProviderInfoRetriever.release();
    }

    // TODO: Step 3, retrieve complication data
    public void retrieveInitialComplicationsData() {
        final int[] complicationIds = MyWatchFace.getComplicationIds();

        mProviderInfoRetriever.retrieveProviderInfo(
                new ProviderInfoRetriever.OnProviderInfoReceivedCallback() {
                    @Override
                    public void onProviderInfoReceived(
                            int watchFaceComplicationId,
                            @Nullable ComplicationProviderInfo complicationProviderInfo) {

                        Log.d(TAG, "\n\nonProviderInfoReceived: " + complicationProviderInfo);

                        updateComplicationViews(watchFaceComplicationId, complicationProviderInfo);
                    }
                },
                mWatchFaceComponentName,
                complicationIds);
    }

    @Override
    public void onClick(View view) {
        if (view.equals(mLeftComplication)) {
            Log.d(TAG, "Left Complication click()");
            launchComplicationHelperActivity(ComplicationLocation.LEFT);

        } else if (view.equals(mRightComplication)) {
            Log.d(TAG, "Right Complication click()");
            launchComplicationHelperActivity(ComplicationLocation.RIGHT);
        }
    }

    // Verifies the watch face supports the complication location, then launches the helper
    // class, so user can choose their complication data provider.
    // TODO: Step 3, launch data selector
    private void launchComplicationHelperActivity(ComplicationLocation complicationLocation) {
        mSelectedComplicationId =
                MyWatchFace.getComplicationId(complicationLocation);

        if (mSelectedComplicationId >= 0) {

            int[] supportedTypes =
                    MyWatchFace.getSupportedComplicationTypes(
                            complicationLocation);

            startActivityForResult(
                    ComplicationHelperActivity.createProviderChooserHelperIntent(
                            getApplicationContext(),
                            mWatchFaceComponentName,
                            mSelectedComplicationId,
                            supportedTypes),
                    ComplicationConfigActivity.COMPLICATION_CONFIG_REQUEST_CODE);

        } else {
            Log.d(TAG, "Complication not supported by watch face.");
        }
    }

    public void updateComplicationViews(
            int watchFaceComplicationId, ComplicationProviderInfo complicationProviderInfo) {
        Log.d(TAG, "updateComplicationViews(): id: " + watchFaceComplicationId);
        Log.d(TAG, "\tinfo: " + complicationProviderInfo);

        if (watchFaceComplicationId == mLeftComplicationId) {
            if (complicationProviderInfo != null) {
                mLeftComplication.setImageIcon(complicationProviderInfo.providerIcon);
                mLeftComplicationBackground.setVisibility(View.VISIBLE);

            } else {
                mLeftComplication.setImageDrawable(mDefaultAddComplicationDrawable);
                mLeftComplicationBackground.setVisibility(View.INVISIBLE);
            }

        } else if (watchFaceComplicationId == mRightComplicationId) {
            if (complicationProviderInfo != null) {
                mRightComplication.setImageIcon(complicationProviderInfo.providerIcon);
                mRightComplicationBackground.setVisibility(View.VISIBLE);

            } else {
                mRightComplication.setImageDrawable(mDefaultAddComplicationDrawable);
                mRightComplicationBackground.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // TODO: Step 3, update views
        if (requestCode == COMPLICATION_CONFIG_REQUEST_CODE && resultCode == RESULT_OK) {

            // Retrieves information for selected Complication provider.
            ComplicationProviderInfo complicationProviderInfo =
                    data.getParcelableExtra(ProviderChooserIntent.EXTRA_PROVIDER_INFO);
            Log.d(TAG, "Provider: " + complicationProviderInfo);

            if (mSelectedComplicationId >= 0) {
                updateComplicationViews(mSelectedComplicationId, complicationProviderInfo);
            }
        }
    }
}
