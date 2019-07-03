package com.bytedance.clockapplication.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.strictmode.ContentUriWithoutPermissionViolation;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Calendar;
import java.util.Locale;

public class Clock extends View {

    private final static String TAG = Clock.class.getSimpleName();

    private static final int FULL_ANGLE = 360;

    private static final int CUSTOM_ALPHA = 140;
    private static final int FULL_ALPHA = 255;

    private static final int DEFAULT_PRIMARY_COLOR = Color.WHITE;
    private static final int DEFAULT_SECONDARY_COLOR = Color.LTGRAY;

    private static final float DEFAULT_DEGREE_STROKE_WIDTH = 0.010f;

    public final static int AM = 0;

    private static final int RIGHT_ANGLE = 90;

    private int mWidth, mCenterX, mCenterY, mRadius;


    /**
     * properties
     */
    private int centerInnerColor;
    private int centerOuterColor;

    private int secondsNeedleColor;
    private int hoursNeedleColor;
    private int minutesNeedleColor;

    private int degreesColor;

    private int hoursValuesColor;

    private int numbersColor;

    private boolean mShowAnalog = true;

    public Clock(Context context) {
        super(context);
        init(context, null);
    }

    public Clock(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public Clock(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size;
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int widthWithoutPadding = width - getPaddingLeft() - getPaddingRight();
        int heightWithoutPadding = height - getPaddingTop() - getPaddingBottom();


        if (widthWithoutPadding > heightWithoutPadding) {
            size = heightWithoutPadding;
        } else {
            size = widthWithoutPadding;
        }

        setMeasuredDimension(size + getPaddingLeft() + getPaddingRight(), size + getPaddingTop() + getPaddingBottom());
        mhandler.sendEmptyMessage(0x23);
    }

    private void init(Context context, AttributeSet attrs) {

        this.centerInnerColor = Color.LTGRAY;
        this.centerOuterColor = DEFAULT_PRIMARY_COLOR;

        this.secondsNeedleColor = DEFAULT_SECONDARY_COLOR;
        this.hoursNeedleColor = DEFAULT_PRIMARY_COLOR;
        this.minutesNeedleColor = DEFAULT_PRIMARY_COLOR;

        this.degreesColor = DEFAULT_PRIMARY_COLOR;

        this.hoursValuesColor = DEFAULT_PRIMARY_COLOR;

        numbersColor = Color.WHITE;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        mWidth = getHeight() > getWidth() ? getWidth() : getHeight();

        int halfWidth = mWidth / 2;
        mCenterX = halfWidth;
        mCenterY = halfWidth;
        mRadius = halfWidth;

        if (mShowAnalog) {
            drawDegrees(canvas);
            drawHoursValues(canvas);
            drawNeedles(canvas);
            drawCenter(canvas);
        } else {
            drawNumbers(canvas);
        }

    }

    private void drawDegrees(Canvas canvas) {  //刻度

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(mWidth * DEFAULT_DEGREE_STROKE_WIDTH);
        paint.setColor(degreesColor);

        int rPadded = mCenterX - (int) (mWidth * 0.01f);
        int rEnd = mCenterX - (int) (mWidth * 0.05f);

        for (int i = 0; i < FULL_ANGLE; i += 6 /* Step */) {

            if ((i % RIGHT_ANGLE) != 0 && (i % 15) != 0)
                paint.setAlpha(CUSTOM_ALPHA);
            else {
                paint.setAlpha(FULL_ALPHA);
            }

            int startX = (int) (mCenterX + rPadded * Math.cos(Math.toRadians(i)));
            int startY = (int) (mCenterX - rPadded * Math.sin(Math.toRadians(i)));

            int stopX = (int) (mCenterX + rEnd * Math.cos(Math.toRadians(i)));
            int stopY = (int) (mCenterX - rEnd * Math.sin(Math.toRadians(i)));

            canvas.drawLine(startX, startY, stopX, stopY, paint);

        }
    }

    /**
     * @param canvas
     */
    private void drawNumbers(Canvas canvas) {

        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(mWidth * 0.2f);
        textPaint.setColor(numbersColor);
        textPaint.setColor(numbersColor);
        textPaint.setAntiAlias(true);

        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int amPm = calendar.get(Calendar.AM_PM);

        String time = String.format("%s:%s:%s%s",
                String.format(Locale.getDefault(), "%02d", hour),
                String.format(Locale.getDefault(), "%02d", minute),
                String.format(Locale.getDefault(), "%02d", second),
                amPm == AM ? "AM" : "PM");

        SpannableStringBuilder spannableString = new SpannableStringBuilder(time);
        spannableString.setSpan(new RelativeSizeSpan(0.3f), spannableString.toString().length() - 2, spannableString.toString().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // se superscript percent

        StaticLayout layout = new StaticLayout(spannableString, textPaint, canvas.getWidth(), Layout.Alignment.ALIGN_CENTER, 1, 1, true);
        canvas.translate(mCenterX - layout.getWidth() / 2f, mCenterY - layout.getHeight() / 2f);
        layout.draw(canvas);
    }

    /**
     * Draw Hour Text Values, such as 1 2 3 ...
     *
     * @param canvas
     */


    private void drawHoursValues(Canvas canvas) {
        // Default Color:
        // - hoursValuesColor
        Paint hoursPaint = new Paint();
        hoursPaint.setTextSize(60);
        hoursPaint.setStyle(Paint.Style.FILL);
        hoursPaint.setColor(hoursValuesColor);
        hoursPaint.setTextAlign(Paint.Align.CENTER);

        Paint.FontMetrics fontMetrics = hoursPaint.getFontMetrics();
        float top = fontMetrics.top;
        float bottom = fontMetrics.bottom;
        //int baseLineY = (int) (rect.centerY()-top/2-bottom/2);

        int R = mCenterX - (int) (mWidth * 0.1f);
        int baseLineX;
        int baselineY;

        for (int i = 0; i<FULL_ANGLE; i+=30){
            int centerX = (int) (mCenterX + R * Math.cos(Math.toRadians(i)));
            int centerY = (int) (mCenterX - R * Math.sin(Math.toRadians(i)));
            baselineY=(int) (centerY-top/2-bottom/2);
            baseLineX=centerX;
            int num;
            if (i/30<3){
                num=3-i/30;
            }
            else{
                num=15-i/30;
            }
            canvas.drawText(""+num,baseLineX,baselineY,hoursPaint);
        }






    }

    private float[] calculatepoint(float angle, float length){
        float[] points = new float[4];
        float default_point_back_length = 25;
        if(angle <= 90f){
            points[0] = (float) Math.sin(angle*Math.PI/180) * default_point_back_length+mCenterX;  //startX
            points[1] = -(float) Math.cos(angle*Math.PI/180) * default_point_back_length+mCenterY;  //startY
            points[2] = (float) Math.sin(angle*Math.PI/180) * length+mCenterX;  //stopX
            points[3] = -(float) Math.cos(angle*Math.PI/180) * length+mCenterY;  //stopY
        }else if(angle <= 180f){
            points[0] = (float) Math.cos((angle-90)*Math.PI/180) * default_point_back_length+mCenterX;
            points[1] = (float) Math.sin((angle-90)*Math.PI/180) * default_point_back_length+mCenterY;
            points[2] = (float) Math.cos((angle-90)*Math.PI/180) * length+mCenterX;
            points[3] = (float) Math.sin((angle-90)*Math.PI/180) * length+mCenterY;
        }else if(angle <= 270f){
            points[0] = -(float) Math.sin((angle-180)*Math.PI/180) * default_point_back_length+mCenterX;
            points[1] = (float) Math.cos((angle-180)*Math.PI/180) * default_point_back_length+mCenterY;
            points[2] = -(float) Math.sin((angle-180)*Math.PI/180) * length+mCenterX;
            points[3] = (float) Math.cos((angle-180)*Math.PI/180) * length+mCenterY;
        }else if(angle <= 360f){
            points[0] = -(float) Math.cos((angle-270)*Math.PI/180) * default_point_back_length+mCenterX;
            points[1] = -(float) Math.sin((angle-270)*Math.PI/180) * default_point_back_length+mCenterY;
            points[2] = -(float) Math.cos((angle-270)*Math.PI/180) * length+mCenterX;
            points[3] = -(float) Math.sin((angle-270)*Math.PI/180) * length+mCenterY;
        }
        return points;
    }
    /**
     * Draw hours, minutes needles
     * Draw progress that indicates hours needle disposition. 指针   math.sin cos
     *
     * @param canvas
     */
    private void drawNeedles(final Canvas canvas) {
        // Default Color:
        // - secondsNeedleColor
        // - hoursNeedleColor
        // - minutesNeedleColor
        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        Paint HourPaint = new Paint();
        Paint MinutePaint = new Paint();
        Paint SecondPaint = new Paint();

        HourPaint.setColor(hoursNeedleColor);
        HourPaint.setStyle(Paint.Style.FILL);
        HourPaint.setStrokeWidth(20);
        HourPaint.setAntiAlias(true);

        MinutePaint.setColor(minutesNeedleColor);
        MinutePaint.setStyle(Paint.Style.FILL);
        MinutePaint.setStrokeWidth(15);
        MinutePaint.setAntiAlias(true);

        SecondPaint.setColor(secondsNeedleColor);
        SecondPaint.setStyle(Paint.Style.FILL);
        SecondPaint.setStrokeWidth(10);
        SecondPaint.setAntiAlias(true);

        float[] hourpoints = calculatepoint((float)(hour+minute/60f+second/3600f)%12/12f*360, 0.2f*mWidth);
        canvas.drawLine(hourpoints[0], hourpoints[1], hourpoints[2], hourpoints[3], HourPaint);
        Log.v("draw lines","hour");
        float[] minutepoints = calculatepoint((float)(minute+second/60f)/60f*360, 0.3f*mWidth);
        canvas.drawLine(minutepoints[0], minutepoints[1], minutepoints[2], minutepoints[3], MinutePaint);
        Log.v("draw lines","minute");
        float[] secondpoints = calculatepoint((float)second/60f*360,0.4f*mWidth);
        canvas.drawLine(secondpoints[0], secondpoints[1], secondpoints[2], secondpoints[3], SecondPaint);
        Log.v("draw lines","second");

    }




    /**
     * Draw Center Dot  中心圆点
     *
     * @param canvas
     */
    private void drawCenter(Canvas canvas) {
        // Default Color:
        // - centerInnerColor
        // - centerOuterColor
        Paint innerdotPaint = new Paint();
        Paint outerdotPaint = new Paint();
        innerdotPaint.setColor(centerInnerColor);
        //innerdotPaint.setStrokeWidth(20);
        outerdotPaint.setColor(centerOuterColor);
        outerdotPaint.setStrokeWidth(10);

        innerdotPaint.setStyle(Paint.Style.FILL);
        outerdotPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle((float)mCenterX,(float)mCenterY,15,innerdotPaint);
        canvas.drawCircle((float)mCenterX,(float)mCenterY,20,outerdotPaint);

    }


    public void setShowAnalog(boolean showAnalog) {
            mShowAnalog = showAnalog;
            invalidate();
        }

    private Handler mhandler=new Handler(){
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x23:
                    Calendar mcalendar=Calendar.getInstance();
                    invalidate();
                    mhandler.sendEmptyMessageDelayed(0x23, 1000);
                    break;

                default:
                    break;
            }
        };
    };


    public boolean isShowAnalog() {
        return mShowAnalog;
    }

}

//每秒更新
//触发UI重新绘制