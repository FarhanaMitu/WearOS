package com.example.weatherdemo.watchface;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.example.weatherdemo.R;
import com.example.weatherdemo.Utility;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import retrofit.ApiClient;
import retrofit.ApiInterface;
import retrofit.Data;
import retrofit.Today;
import retrofit.WeatherData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherWatchFace extends CanvasWatchFaceService {
    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
    private static final Typeface MONO_TYPEFACE =
            Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);

    /**
     * Update rate in milliseconds for interactive mode. Defaults to one second
     * because the watch face needs to update seconds in interactive mode.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;
    private static final int WEATHER_UPDATE_TIME = 1000;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<WeatherWatchFace.Engine> mWeakReference;

        public EngineHandler(WeatherWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            WeatherWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                    case WEATHER_UPDATE_TIME:
                        engine.handleUpdateWeather();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine {

        private final Handler mUpdateTimeHandler = new EngineHandler(this);
        private Calendar mCalendar;
        private final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };
        private boolean mRegisteredTimeZoneReceiver = false;
        private Paint mBackgroundPaint;
        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        private boolean mLowBitAmbient;
        private boolean mBurnInProtection;
        private boolean mAmbient;

        private Paint mTimePaint, mTextPaint, mTextValuePaint, mAMPaint;

        private Bitmap mWeatherIcon;
        private int mIconLeft, mIconRight;
        private boolean isRound;

        private Today today;
        private WeatherData mWeatherData;
        private WeatherData[] mWeather;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(WeatherWatchFace.this)
                    .setAcceptsTapEvents(true)
                    .build());

            mCalendar = Calendar.getInstance();

            Resources resources = WeatherWatchFace.this.getResources();

            today = new Today();
            today.setTemp("30");
            mWeatherData = new WeatherData();
            mWeatherData.setDescription("sunny");
            mWeatherData.setMain("Sunny");
            mWeather = new WeatherData[1];
            mWeather[0] = mWeatherData;
            today.setWeatherData(mWeather);

            // Initializes Watch Face.
            initialize();

            getWeather();
        }

        private void initialize() {
            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(Color.BLACK);
//            mBackgroundPaint.setColor(getResources().getColor(R.color.bgColor));

            mWeatherIcon = BitmapFactory.decodeResource(getResources(), R.drawable.haze_s);
            mIconLeft = 40;
            mIconRight = 120;

            mTimePaint = new Paint();
            mTimePaint.setColor(Color.WHITE);
            mTimePaint.setTextSize(57);
            mTimePaint.setAntiAlias(true);
            mTimePaint.setTypeface(MONO_TYPEFACE);
            mTimePaint.setStrokeWidth(4f);
            mTimePaint.setTextAlign(Paint.Align.CENTER);

            mTextPaint = new Paint();
            mTextPaint.setColor(Color.WHITE);
            mTextPaint.setTextSize(44);
            mTextPaint.setAntiAlias(true);
            mTextPaint.setTypeface(MONO_TYPEFACE);
            mTextPaint.setStrokeWidth(4f);
            mTextPaint.setTextAlign(Paint.Align.CENTER);

            mTextValuePaint = new Paint();
            mTextValuePaint.setColor(Color.WHITE);
            mTextValuePaint.setTextSize(18);
            mTextValuePaint.setAntiAlias(true);
            mTextValuePaint.setTypeface(NORMAL_TYPEFACE);
            mTextValuePaint.setStrokeWidth(4f);
            mTextValuePaint.setTextAlign(Paint.Align.CENTER);

            mAMPaint = new Paint();
            mAMPaint.setColor(Color.WHITE);
            mAMPaint.setTextSize(20);
            mAMPaint.setAntiAlias(true);
            mAMPaint.setTypeface(NORMAL_TYPEFACE);
            mAMPaint.setStrokeWidth(4f);
            mAMPaint.setTextAlign(Paint.Align.CENTER);
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            WeatherWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            WeatherWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = WeatherWatchFace.this.getResources();
            isRound = insets.isRound();
//            mXOffset = resources.getDimension(isRound
//                    ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
//            float textSize = resources.getDimension(isRound
//                    ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);
//
//            mTextPaint.setTextSize(textSize);
//            mIconLeft = (int) resources.getDimension(isRound ? 60 : 40);
//            mIconRight = (int) resources.getDimension(isRound ? 140 : 120);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);

            mAmbient = inAmbientMode;
            if (mLowBitAmbient) {
                mTextPaint.setAntiAlias(!inAmbientMode);
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        /**
         * Captures tap event (and tap type) and toggles the background color if the user finishes
         * a tap.
         */
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            switch (tapType) {
                case TAP_TYPE_TOUCH:
                    // The user has started touching the screen.
                    break;
                case TAP_TYPE_TOUCH_CANCEL:
                    // The user has started a different gesture or otherwise cancelled the tap.
                    break;
                case TAP_TYPE_TAP:
                    // The user has completed the tap gesture.
                    // TODO: Add code to handle the tap gesture.
//                    Toast.makeText(getApplicationContext(), R.string.message, Toast.LENGTH_SHORT)
//                            .show();
                    break;
            }
            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            // Draw the background.
            if (isInAmbientMode()) {
                canvas.drawColor(Color.BLACK);
            } else {
                canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
            }

            // Draw H:MM in ambient mode or H:MM:SS in interactive mode.
            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);

            int xPos = (canvas.getWidth() / 2);
            int yPos = (int) ((canvas.getHeight() / 2) - ((mTimePaint.descent() + mTimePaint.ascent()) / 2)) ;

            boolean is24Hour = DateFormat.is24HourFormat(WeatherWatchFace.this);
            int hour;

            if(is24Hour){
                hour = mCalendar.get(Calendar.HOUR);
            } else {
                hour = mCalendar.get(Calendar.HOUR);
                if(hour == 0) {
                    hour = 12;
                }
            }

            String text = mAmbient
                    ? String.format("%02d:%02d", hour,
                    mCalendar.get(Calendar.MINUTE))
                    : String.format("%02d:%02d", hour,
                    mCalendar.get(Calendar.MINUTE));
            String date = new SimpleDateFormat("EEEE, MMMM yy").format(mCalendar.getTime());
            String temp = today.getTemp() + "°c";
            WeatherData[] weather = today.getWeatherData();
//            Log.d("TAG", "mitu des: " + weather.length);
            String des = weather[0].getDescription();
            Log.d("TAG", "String Length: " + des +"-");
//            String des = "haze";
//            String des = "sunny";
//            String des = "cloudy";
//            String des = "rain";
//            String des = "snow";

            mWeatherIcon = BitmapFactory.decodeResource(getResources(), Utility.getIconResourceForWeather(des));

            Paint paint = new Paint();

            canvas.drawText(text, xPos, yPos-100, mTimePaint);
            canvas.drawText(date, xPos, yPos-70, mTextValuePaint);
            canvas.drawText(temp, xPos+20, yPos+20, mTextPaint);
            if(isRound){
                canvas.drawBitmap(mWeatherIcon, 60, 140, paint);
            } else {
                canvas.drawBitmap(mWeatherIcon, 36, 120, paint);
            }
            canvas.drawText(des, xPos, yPos+55, mTextValuePaint);
        }

        private void getWeather(){
            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<Data> call = apiInterface.getWeatherData();
            Log.d("TAG", "getWeatherData: "+call.request().url());
            call.enqueue(new Callback<Data>() {

                @Override
                public void onResponse(Call<Data> call, Response<Data> response) {
                    Log.d("TAG", "onResponse: Success");
                    Log.d("TAG", "onResponse: "+response);

                    if(response.body()==null) {
                        Log.d("TAG", "onResponse: Response is Null.");
                    } else {
                        Log.d("TAG", "mitu");
                        today = response.body().getTodayData();
                    }
                }

                @Override
                public void onFailure(Call<Data> call, Throwable t) {
                    Log.d("TAG", "onFailure: couldn't fetch Data");
                }
            });
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            mUpdateTimeHandler.removeMessages(WEATHER_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
                mUpdateTimeHandler.sendEmptyMessage(WEATHER_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }

        private void handleUpdateWeather() {
            getWeather();
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessageDelayed(WEATHER_UPDATE_TIME, WEATHER_UPDATE_TIME*60*30);
            }
        }
    }
}
