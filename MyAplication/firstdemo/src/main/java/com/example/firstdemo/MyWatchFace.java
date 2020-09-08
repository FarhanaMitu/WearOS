package com.example.firstdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;

import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Analog watch face with a ticking second hand. In ambient mode, the second hand isn't
 * shown. On devices with low-bit ambient mode, the hands are drawn without anti-aliasing in ambient
 * mode. The watch face is drawn with less contrast in mute mode.
 * <p>
 * Important Note: Because watch face apps do not have a default Activity in
 * their project, you will need to set your Configurations to
 * "Do not launch Activity" for both the Wear and/or Application modules. If you
 * are unsure how to do this, please review the "Run Starter project" section
 * in the Google Watch Face Code Lab:
 * https://codelabs.developers.google.com/codelabs/watchface/index.html#0
 */
public class MyWatchFace extends CanvasWatchFaceService {

    /*
     * Updates rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        /* Handler to update the time once a second in interactive mode. */
        private final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if (R.id.message_update == message.what) {
                    invalidate();
                    if (shouldTimerBeRunning()) {
                        long timeMs = System.currentTimeMillis();
                        long delayMs = INTERACTIVE_UPDATE_RATE_MS
                                - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                        mUpdateTimeHandler.sendEmptyMessageDelayed(R.id.message_update, delayMs);
                    }
                }
            }
        };

        private final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };

        private boolean mRegisteredTimeZoneReceiver = false;

        private static final float STROKE_WIDTH = 3f;

        private static final float HAND_END_CAP_RADIUS = 4f;
        private static final float SHADOW_RADIUS = 6f;

        private Calendar mCalendar;

        private Paint mBackgroundPaint;
        private Paint mHandPaint;

        private boolean mAmbient;

        private float mHourHandLength;
        private float mMinuteHandLength;
        private float mSecondHandLength;

        private int mWidth;
        private int mHeight;
        private float mCenterX;
        private float mCenterY;
        private float mScale = 1;

        private int mWatchHandColor;
        private int mWatchHandShadowColor;

        private Bitmap mBackgroundBitmap;
        private Bitmap mGrayBackgroundBitmap;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(MyWatchFace.this).build());

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(Color.BLACK);

            mHandPaint = new Paint();
            mHandPaint.setColor(Color.WHITE);
            mHandPaint.setStrokeWidth(STROKE_WIDTH);
            mHandPaint.setAntiAlias(true);
            mHandPaint.setStrokeCap(Paint.Cap.ROUND);
            mHandPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, Color.BLACK);// Change watch hand
            mHandPaint.setStyle(Paint.Style.STROKE);

            mCalendar = Calendar.getInstance();

            mBackgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.custom_background2);

//            Palette.from(mBackgroundBitmap).generate(new Palette.PaletteAsyncListener() {
//                @Override
//                public void onGenerated(@Nullable Palette palette) {
//                    if (palette != null) {
//                        Log.d("onGenerated", palette.toString());
//                        mWatchHandColor = palette.getVibrantColor(Color.WHITE);
//                        mWatchHandShadowColor =
//                                palette.getDarkMutedColor(Color.BLACK);
//
//                        setWatchHandColor();
//                    }
//                }
//            });
        }

        private void setWatchHandColor(){
            if (mAmbient){
                mHandPaint.setColor(Color.WHITE);
                mHandPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, Color.BLACK);
            } else {
                mHandPaint.setColor(mWatchHandColor);
                mHandPaint.setShadowLayer(SHADOW_RADIUS, 0, 0, mWatchHandShadowColor);
            }
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(R.id.message_update);
            super.onDestroy();
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

            if (inAmbientMode) {
                mHandPaint.setAntiAlias(false);
            } else {
                mHandPaint.setAntiAlias(true);
            }

//            setWatchHandColor();
            invalidate();

            /* Check and trigger whether or not timer should be running (only in active mode). */
            updateTimer();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            mWidth = width;
            mHeight = height;
            /*
             * Find the coordinates of the center point on the screen.
             * Ignore the window insets so that, on round watches
             * with a "chin", the watch face is centered on the entire screen,
             * not just the usable portion.
             */
            mCenterX = mWidth / 2f;
            mCenterY = mHeight / 2f;
            /*
             * Calculate the lengths of the watch hands and store them in member variables.
             */
//            mHourHandLength = mCenterX - 80;
            mHourHandLength = 0.5f * width / 2;
//            mMinuteHandLength = mCenterX - 40;
            mMinuteHandLength = 0.7f * width / 2;
//            mSecondHandLength = mCenterX - 20;
            mSecondHandLength = 0.9f * width / 2;

            mScale = ((float)width) / (float) mBackgroundBitmap.getWidth();
            mBackgroundBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap, (int)(mBackgroundBitmap.getWidth()*mScale),
                    (int)(mBackgroundBitmap.getHeight()*mScale), true);
            initGrayBackgroundBitmap();
        }

        private void initGrayBackgroundBitmap() {
            mGrayBackgroundBitmap = Bitmap.createBitmap(
                    mBackgroundBitmap.getWidth(),
                    mBackgroundBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mGrayBackgroundBitmap);
            Paint grayPaint = new Paint();
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0);
            ColorMatrixColorFilter filter = new
                    ColorMatrixColorFilter(colorMatrix);
            grayPaint.setColorFilter(filter);
            canvas.drawBitmap(mBackgroundBitmap, 0, 0, grayPaint);
        }

        /**
         * Captures tap event (and tap type). The {@link WatchFaceService#TAP_TYPE_TAP} case can be
         * used for implementing specific logic to handle the gesture.
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
                    Toast.makeText(getApplicationContext(), R.string.message, Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            long now = System.currentTimeMillis();
            mCalendar.setTimeInMillis(now);

            // Draw the background.
//            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mBackgroundPaint);
            if (mAmbient) {
                canvas.drawBitmap(mGrayBackgroundBitmap, 0, 0, mBackgroundPaint);
            } else {
                canvas.drawBitmap(mBackgroundBitmap, 0, 0, mBackgroundPaint);
            }


            /*
             * These calculations reflect the rotation in degrees per unit of time, e.g.,
             * 360 / 60 = 6 and 360 / 12 = 30.
             */
            final float seconds =
                    (mCalendar.get(Calendar.SECOND) + mCalendar.get(Calendar.MILLISECOND) / 1000f);
            final float secondsRotation = seconds * 6f;

            final float minutesRotation = mCalendar.get(Calendar.MINUTE) * 6f;

            final float hourHandOffset = mCalendar.get(Calendar.MINUTE) / 2f;
            final float hoursRotation = (mCalendar.get(Calendar.HOUR) * 30) + hourHandOffset;

            // save the canvas state before we begin to rotate it
            canvas.save();

            canvas.rotate(hoursRotation, mCenterX, mCenterY);
            drawHand(canvas, mHourHandLength);

            canvas.rotate(minutesRotation - hoursRotation, mCenterX, mCenterY);
            drawHand(canvas, mMinuteHandLength);

            canvas.rotate(secondsRotation - minutesRotation, mCenterX, mCenterY);
            canvas.drawLine(mCenterX, mCenterY - HAND_END_CAP_RADIUS, mCenterX,
                    mCenterY - mSecondHandLength, mHandPaint);

            canvas.drawCircle(mCenterX, mCenterY, HAND_END_CAP_RADIUS,
                    mHandPaint);
//            canvas.rotate(hoursRotation, mCenterX, mCenterY);
//            canvas.drawLine(mCenterX, mCenterY, mCenterX, mCenterY - mHourHandLength, mHandPaint);
//
//            canvas.rotate(minutesRotation - hoursRotation, mCenterX, mCenterY);
//            canvas.drawLine(mCenterX, mCenterY, mCenterX, mCenterY - mMinuteHandLength, mHandPaint);
//
//            if (!mAmbient) {
//                canvas.rotate(secondsRotation - minutesRotation, mCenterX, mCenterY);
//                canvas.drawLine(mCenterX, mCenterY, mCenterX, mCenterY - mSecondHandLength,
//                        mHandPaint);
//            }
            // restore the canvas' original orientation.
            canvas.restore();
        }

        private void drawHand(Canvas canvas, float handLength) {
            canvas.drawRoundRect(mCenterX - HAND_END_CAP_RADIUS,
                    mCenterY - handLength, mCenterX + HAND_END_CAP_RADIUS,
                    mCenterY + HAND_END_CAP_RADIUS, HAND_END_CAP_RADIUS,
                    HAND_END_CAP_RADIUS, mHandPaint);
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

            /*
             * Whether the timer should be running depends on whether we're visible
             * (as well as whether we're in ambient mode),
             * so we may need to start or stop the timer.
             */
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            MyWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            MyWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        /**
         * Starts/stops the {@link #mUpdateTimeHandler} timer based on the state of the watch face.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(R.id.message_update);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(R.id.message_update);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer
         * should only run in active mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }
    }
}
