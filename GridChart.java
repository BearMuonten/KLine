package com.treasure.dreamstock.weight.bigkline;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/**
 * 坐标轴使用的View
 * 
 * @author 熊峰
 * 
 */
public class GridChart extends View {

	// ////////////默认值////////////////
	/** 默认背景色 */
	public static final int DEFAULT_BACKGROUD = Color.WHITE;

	/** 默认XY轴字体大小 **/
	public static final int DEFAULT_AXIS_TITLE_SIZE = 22;

	/** 默认XY坐标轴颜色 */
	private static final int DEFAULT_AXIS_COLOR = Color.RED;

	/** 默认经纬线颜色 */
	private static final int DEFAULT_LONGI_LAITUDE_COLOR = Color.MAGENTA;

	/** 默认上表纬线数 */
	public static int DEFAULT_UPER_LATITUDE_NUM = 3;

	/** 默认下表纬线数 */
	private static int DEFAULT_LOWER_LATITUDE_NUM = 1;

	/** 默认经线数 */
	public static int DEFAULT_LOGITUDE_NUM = 3;

	/** 默认边框的颜色 */
	public static final int DEFAULT_BORDER_COLOR = Color.RED;

	/** 默认虚线效果 */
	private static final PathEffect DEFAULT_DASH_EFFECT = new DashPathEffect(new float[] { 3, 3, 3,
			3 }, 1);

	/** 下表的顶部 */
	public static float LOWER_CHART_TOP;

	/** 上表的底部 */
	public static float UPER_CHART_BOTTOM;

	// /////////////属性////////////////
	/** 背景色 */
	private int mBackGround;

	/** 坐标轴XY颜色 */
	private int mAxisColor;

	/** 经纬线颜色 */
	private int mLongiLatitudeColor;

	/** 虚线效果 */
	private PathEffect mDashEffect;

	/** 边线色 */
	private int mBorderColor;

	/** 上表高度 */
	private float mUperChartHeight;

	/** 是否显示下表Tabs */
	private boolean showLowerChartTabs;

	/** 是否显示顶部Titles */
	private boolean showTopTitles;

	/** 顶部Titles高度 */
	private float topTitleHeight;

	/** 下表TabTitles */
	private String[] mLowerChartTabTitles;

	/** 下表Tab宽度 */
	private float mTabWidth;

	/** 下表Tab高度 */
	private float mTabHight;

	/** 下表TabIndex */
	private int mTabIndex;

	/** 下表高度 */
	private float mLowerChartHeight;

	private float longitudeSpacing;
	private float latitudeSpacing;
	
	private float textSpace=20.0f;//编写旁边数字的距离

	private OnTabClickListener mOnTabClickListener;

	public GridChart(Context context) {
		super(context);
		init();
	}

	public GridChart(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public GridChart(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mBackGround = DEFAULT_BACKGROUD;
		mAxisColor = DEFAULT_AXIS_COLOR;
		mLongiLatitudeColor = DEFAULT_LONGI_LAITUDE_COLOR;
		mDashEffect = DEFAULT_DASH_EFFECT;
		mBorderColor = DEFAULT_BORDER_COLOR;
		showLowerChartTabs = true;
		showTopTitles = true;
		topTitleHeight = 0;
		mTabIndex = 0;
		mOnTabClickListener = null;

		mTabWidth = 0;
		mTabHight = 0;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		setBackgroundColor(mBackGround);
		int viewHeight = getHeight();
		int viewWidth = getWidth();
		mLowerChartHeight = viewHeight - 2 - LOWER_CHART_TOP;
		if (showLowerChartTabs) {
			mTabHight = viewHeight / 16.0f;
		}
		if (showTopTitles) {
			topTitleHeight = DEFAULT_AXIS_TITLE_SIZE + 2;
		} else {
			topTitleHeight = 0;
		}

		longitudeSpacing = (viewWidth - 2*textSpace) / (DEFAULT_LOGITUDE_NUM + 1);

		latitudeSpacing = (viewHeight - 4 - DEFAULT_AXIS_TITLE_SIZE - topTitleHeight - mTabHight)
				/ (DEFAULT_UPER_LATITUDE_NUM + DEFAULT_LOWER_LATITUDE_NUM + 2);
		mUperChartHeight = latitudeSpacing * (DEFAULT_UPER_LATITUDE_NUM + 1);
		LOWER_CHART_TOP = viewHeight - 1 - latitudeSpacing * (DEFAULT_LOWER_LATITUDE_NUM + 1);
		UPER_CHART_BOTTOM = 1 + topTitleHeight + latitudeSpacing * (DEFAULT_UPER_LATITUDE_NUM + 1);

		// 绘制边框
		drawBorders(canvas, viewHeight, viewWidth);

		// 绘制经线
		drawLongitudes(canvas, viewHeight, longitudeSpacing);

		// 绘制纬线
		drawLatitudes(canvas, viewHeight, viewWidth, latitudeSpacing);

		// 绘制X线及LowerChartTitles
		drawRegions(canvas, viewHeight, viewWidth);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		Rect rect = new Rect();
		getGlobalVisibleRect(rect);
		float x = event.getRawX();
		float y = event.getRawY();

		if (y <= LOWER_CHART_TOP + rect.top + 2
				&& y >= UPER_CHART_BOTTOM + DEFAULT_AXIS_TITLE_SIZE + rect.top) {
			if (mTabWidth <= 0) {
				return true;
			}
			int indext = (int) (x / mTabWidth);

			if (mTabIndex != indext) {
				mTabIndex = indext;
				mOnTabClickListener.onTabClick(mTabIndex);
			}
			return true;
		}

		return false;
	}

	public void setOnTabClickListener(OnTabClickListener onTabClickListener) {
		mOnTabClickListener = onTabClickListener;
	}

	public interface OnTabClickListener {
		void onTabClick(int indext);
	}

	/**
	 * 绘制边框
	 * 
	 * @param canvas
	 */
	private void drawBorders(Canvas canvas, int viewHeight, int viewWidth) {
		Paint paint = new Paint();
		paint.setColor(mBorderColor);
		paint.setStrokeWidth(2);
		canvas.drawLine(textSpace, 1, viewWidth - textSpace, 1, paint);
		canvas.drawLine(textSpace, 1, textSpace, viewHeight - 1, paint);
		canvas.drawLine(viewWidth - textSpace, viewHeight - 1, viewWidth - textSpace, 1, paint);
		canvas.drawLine(viewWidth - textSpace, viewHeight - 1, textSpace, viewHeight - 1, paint);
	}

	/**
	 * 绘制经线
	 * 
	 * @param canvas
	 * @param viewHeight
	 * @param viewWidth
	 */
	private void drawLongitudes(Canvas canvas, int viewHeight, float longitudeSpacing) {
		Paint paint = new Paint();
		paint.setColor(mLongiLatitudeColor);
		paint.setPathEffect(mDashEffect);
		for (int i = 1; i <= DEFAULT_LOGITUDE_NUM; i++) {
			canvas.drawLine(textSpace + longitudeSpacing * i, topTitleHeight + 2, textSpace + longitudeSpacing * i,
					UPER_CHART_BOTTOM, paint);
			canvas.drawLine(textSpace + longitudeSpacing * i, LOWER_CHART_TOP, textSpace + longitudeSpacing * i,
					viewHeight - 1, paint);
		}

	}

	/**
	 * 绘制纬线
	 * 
	 * @param canvas
	 * @param viewHeight
	 * @param viewWidth
	 */
	private void drawLatitudes(Canvas canvas, int viewHeight, int viewWidth, float latitudeSpacing) {
		Paint paint = new Paint();
		paint.setColor(mLongiLatitudeColor);
		paint.setPathEffect(mDashEffect);
		for (int i = 1; i <= DEFAULT_UPER_LATITUDE_NUM; i++) {
			canvas.drawLine(textSpace, topTitleHeight + 1 + latitudeSpacing * i, viewWidth - textSpace,
					topTitleHeight + 1 + latitudeSpacing * i, paint);
		}
		for (int i = 1; i <= DEFAULT_LOWER_LATITUDE_NUM; i++) {
			canvas.drawLine(textSpace, viewHeight - 1 - latitudeSpacing, viewWidth - textSpace, viewHeight - 1
					- latitudeSpacing, paint);
		}

	}

	private void drawRegions(Canvas canvas, int viewHeight, int viewWidth) {
		Paint paint = new Paint();
		paint.setColor(mAxisColor);
		paint.setAlpha(150);
		if (showTopTitles) {
			canvas.drawLine(textSpace, 1 + DEFAULT_AXIS_TITLE_SIZE + 2, viewWidth - textSpace,
					1 + DEFAULT_AXIS_TITLE_SIZE + 2, paint);
		}
		canvas.drawLine(textSpace, UPER_CHART_BOTTOM, viewWidth - textSpace, UPER_CHART_BOTTOM, paint);
		canvas.drawLine(textSpace, LOWER_CHART_TOP, viewWidth - textSpace, LOWER_CHART_TOP, paint);
		if (showLowerChartTabs) {
			canvas.drawLine(textSpace, UPER_CHART_BOTTOM + DEFAULT_AXIS_TITLE_SIZE + 2, viewWidth - textSpace,
					UPER_CHART_BOTTOM + DEFAULT_AXIS_TITLE_SIZE + 2, paint);
			if (mLowerChartTabTitles == null || mLowerChartTabTitles.length <= 0) {
				return;
			}
			mTabWidth = (viewWidth - 2*textSpace) / 10.0f * 10.0f / mLowerChartTabTitles.length;
			if (mTabWidth < DEFAULT_AXIS_TITLE_SIZE * 2.5f + 2) {
				mTabWidth = DEFAULT_AXIS_TITLE_SIZE * 2.5f + 2;
			}

			Paint textPaint = new Paint();
			textPaint.setColor(Color.WHITE);
			textPaint.setTextSize(DEFAULT_AXIS_TITLE_SIZE);
			for (int i = 0; i < mLowerChartTabTitles.length && mTabWidth * (i + 1) <= viewWidth - 2; i++) {
				if (i == mTabIndex) {
					Paint bgPaint = new Paint();
					bgPaint.setColor(Color.MAGENTA);
					canvas.drawRect(mTabWidth * i + textSpace, LOWER_CHART_TOP, mTabWidth * (i + 1) + textSpace,
							UPER_CHART_BOTTOM + DEFAULT_AXIS_TITLE_SIZE + 2, bgPaint);
				}
				canvas.drawLine(mTabWidth * i + textSpace, LOWER_CHART_TOP, mTabWidth * i + textSpace,
						UPER_CHART_BOTTOM + DEFAULT_AXIS_TITLE_SIZE + 2, paint);
				canvas.drawText(mLowerChartTabTitles[i], mTabWidth * i + mTabWidth / 2.0f
						- mLowerChartTabTitles[i].length() / 3.0f * DEFAULT_AXIS_TITLE_SIZE,
						LOWER_CHART_TOP - mTabHight / 2.0f + DEFAULT_AXIS_TITLE_SIZE / 2.0f,
						textPaint);
			}
		}
	}

	public int getBackGround() {
		return mBackGround;
	}

	public void setBackGround(int BackGround) {
		this.mBackGround = BackGround;
	}

	public int getAxisColor() {
		return mAxisColor;
	}

	public void setAxisColor(int AxisColor) {
		this.mAxisColor = AxisColor;
	}

	public int getLongiLatitudeColor() {
		return mLongiLatitudeColor;
	}

	/**
	 * 设置经纬线颜色
	 * @param LongiLatitudeColor
	 */
	public void setLongiLatitudeColor(int LongiLatitudeColor) {
		this.mLongiLatitudeColor = LongiLatitudeColor;
	}

	public PathEffect getDashEffect() {
		return mDashEffect;
	}

	public void setDashEffect(PathEffect DashEffect) {
		this.mDashEffect = DashEffect;
	}

	public int getBorderColor() {
		return mBorderColor;
	}
	
	/**
	 * 设置表格外框颜色
	 * @param BorderColor
	 */
	public void setBorderColor(int BorderColor) {
		this.mBorderColor = BorderColor;
	}
	
	/**
	 * 获取上表高度
	 * @return
	 */
	public float getUperChartHeight() {
		return mUperChartHeight;
	}

	public void setUperChartHeight(float UperChartHeight) {
		this.mUperChartHeight = UperChartHeight;
	}

	public boolean isShowLowerChartTabs() {
		return showLowerChartTabs;
	}

	public void setShowLowerChartTabs(boolean showLowerChartTabs) {
		this.showLowerChartTabs = showLowerChartTabs;
	}

	/**
	 * 获取下表高度
	 * @return
	 */
	public float getLowerChartHeight() {
		return mLowerChartHeight;
	}

	public void setLowerChartHeight(float LowerChartHeight) {
		this.mLowerChartHeight = LowerChartHeight;
	}

	public String[] getLowerChartTabTitles() {
		return mLowerChartTabTitles;
	}

	public void setLowerChartTabTitles(String[] LowerChartTabTitles) {
		this.mLowerChartTabTitles = LowerChartTabTitles;
	}

	public float getLongitudeSpacing() {
		return longitudeSpacing;
	}

	public void setLongitudeSpacing(float longitudeSpacing) {
		this.longitudeSpacing = longitudeSpacing;
	}

	public float getLatitudeSpacing() {
		return latitudeSpacing;
	}

	public void setLatitudeSpacing(float latitudeSpacing) {
		this.latitudeSpacing = latitudeSpacing;
	}

	public void setShowTopTitles(boolean showTopTitles) {
		this.showTopTitles = showTopTitles;
	}

	public float getTopTitleHeight() {
		return topTitleHeight;
	}
	
	/**
	 *设置上表纬线个数 
	 * @param num
	 */
	public void setDefaultLineUp(int num){
		this.DEFAULT_UPER_LATITUDE_NUM=num;
	}
	
	public void setDefaultLineDown(int num){
		this.DEFAULT_LOWER_LATITUDE_NUM=num;
	}
	
	public void setLogitudeNum(int num){
		this.DEFAULT_LOGITUDE_NUM=num;
	}
	
	/**
	 * 设置和获取四周字体所占距离
	 */
	public void setTextSpace(float textSpace){
		this.textSpace=textSpace;
	}
	
	public float getTextSpace(){
		return textSpace;
	}
	

}
