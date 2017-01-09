package com.treasure.dreamstock.weight.bigkline;

import java.security.acl.LastOwnerException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;

import com.treasure.dreamstock.R;
import com.treasure.dreamstock.weight.kline.DayKModel;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.text.TextUtils;
import android.text.method.MovementMethod;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.widget.Toast;

/**
 * 日k
 * 
 * @author xiongfeng
 * 
 */
public class MyDayKView extends GridChart {

	/**
	 * 是否data的size比默认显示的少
	 */
	private boolean isSmall = false;

	private Runnable mLongPressRunnable;
	private boolean isMoved;
	// 是否释放了
	private boolean isReleased;
	// 计数器，防止多次点击导致最后一次形成longpress的时间变短
	private int mCounter = 1;

	private int MIN_VALUE = 0, MAX_VALUE = 1;

	/** 触摸模式 */
	private static int TOUCH_MODE;
	private final static int NONE = 0;
	private final static int DOWN = 1;
	private final static int MOVE = 2;
	private final static int ZOOM = 3;
	private boolean flag = true;
	private Paint linePaint;
	private Paint pointPaint, areaPaint;
	private Paint mPaint, textPaint, timePaint;
	private float downHeight;// 向下偏移量
	private float downHeight2;// 向下偏移量
	private DecimalFormat decimalFormat;
	private float lowerHeight;// 下表高度

	/** 默认Y轴字体颜 **/
	private static final int DEFAULT_AXIS_Y_TITLE_COLOR = Color.YELLOW;

	/** 默认X轴字体颜色 **/
	private static final int DEFAULT_AXIS_X_TITLE_COLOR = Color.RED;

	/** 显示的最小Candle个数 */
	private final static int MIN_CANDLE_NUM = 30;

	/** 默认显示的Candle个数 */
	private final static int DEFAULT_CANDLE_NUM = 120;

	/** 最小可识别的移动距离 */
	private final static int MIN_MOVE_DISTANCE = 15;

	/** 触摸点 */
	private float mStartX;
	private float mStartY;

	private float startX;
	private float startY;
	private float stopX;
	private float stopY;
	private boolean flagSwitch = true;

	private float offset;

	private int width;
	private int height;

	private float maxValue, minValue;

	private float max = 0;;

	private float evenWidth;
	private int touchNum = 0;

	/**
	 * 日k数据
	 */
	private List<DayKModel> dataList;

	/** 显示的日k数据起始位置 */
	private int mDataStartIndext;

	/** 显示的日k数据个数 */
	private int mShowDataNum;

	/** 是否显示蜡烛详情 */
	private boolean showDetails;

	/** 当前数据的最大最小价格 */
	private static int num = 0;
	private float wight = 1.0f;
	private float maxVolume;
	private MyDataListener rfreshListener;
	private DayMoveListener moveListener;

	public MyDayKView(Context context) {
		super(context);
		init();
	}

	public MyDayKView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MyDayKView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	/**
	 * 设置刷新监听
	 * 
	 * @param listener
	 */
	public void setRefreshListener(MyDataListener listener) {
		this.rfreshListener = listener;
	}

	/**
	 * 设置移动监听
	 * 
	 * @param listener
	 */
	public void setMoveListener(DayMoveListener listener) {
		this.moveListener = listener;
	}

	private void init() {
		super.setShowLowerChartTabs(false);
		super.setLongiLatitudeColor(getResources().getColor(R.color.k_bg_line));
		super.setBorderColor(getResources().getColor(R.color.k_bg_line));
		super.setAxisColor(getResources().getColor(R.color.k_bg_line));
		super.setTextSpace(DEFAULT_AXIS_TITLE_SIZE * 4);

		downHeight = 1.2f * DEFAULT_AXIS_TITLE_SIZE;
		downHeight2 = 1.1f * DEFAULT_AXIS_TITLE_SIZE;

		mShowDataNum = DEFAULT_CANDLE_NUM;
		lowerHeight = 0;
		mDataStartIndext = 0;
		showDetails = false;
		decimalFormat = new DecimalFormat("0.00");

		linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		timePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		areaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

		mLongPressRunnable = new Runnable() {

			@Override
			public void run() {
				// mCounter--;
				// 计数器大于0，说明当前执行的Runnable不是最后一次down产生的。
				if (isReleased || isMoved) {
					return;
				}
				showDetails = true;
				postInvalidate();
			}
		};
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		super.setLogitudeNum(0);

		width = (int) (getWidth() - 2 * getTextSpace());
		height = getHeight();

		evenWidth = (float) ((width - 4) / 10.0 * 10.0 / mShowDataNum);
		lowerHeight = getLowerChartHeight() - 2;

		if (!isSmall) {
			// 画蜡烛图
			drawTimeLine(canvas);
			drawCand(canvas);
			drawLine(canvas, 0);
			drawLine(canvas, 1);
			drawLine(canvas, 2);
			drawColum(canvas);
		} else {
			if (flagSwitch) {
				Collections.reverse(dataList);
				flagSwitch = false;
			}
			drawTimeLine(canvas);
			drawCand2(canvas);
			drawLine2(canvas, 0);
			drawLine2(canvas, 1);
			drawLine2(canvas, 2);
			drawColum2(canvas);
		}

		drawHDLine(canvas);

		drawXYText(canvas);
	}

	/**
	 * 绘制时间轴
	 * 
	 * @param canvas
	 */
	private void drawTimeLine(Canvas canvas) {
		if (dataList == null) {
			return;
		}
		timePaint.setStrokeWidth(1.0f);
		timePaint.setColor(getResources().getColor(R.color.k_bg_line));
		textPaint.setColor(getResources().getColor(R.color.k_big_text));
		textPaint.setTextSize(DEFAULT_AXIS_TITLE_SIZE);

		if (!isSmall) {
			float prePos = 0;
			String sub1 = dataList.get(mDataStartIndext).getStocktime()
					.substring(5, 6);
			String nSub = dataList.get(mDataStartIndext).getStocktime()
					.substring(3, 4);
			String whereM = "2";
			for (int i = 0; i < mShowDataNum
					&& mDataStartIndext + i < dataList.size(); i++) {
				float pos = (width - (evenWidth * i + evenWidth * (i + 1)) / 2 + getTextSpace());

				if (from == 1) {
					String stocktime = dataList.get(mDataStartIndext + i)
							.getStocktime();
					String sub2 = stocktime.substring(5, 6);
					if (!sub2.equals(sub1)) {
						if (prePos == 0) {
							prePos = pos;
						}
						sub1 = sub2;
						String stockTimexs = null;
						if (mDataStartIndext + i > 0) {
							stockTimexs = dataList
									.get(mDataStartIndext + i - 1)
									.getStocktime();
						} else {
							stockTimexs = stocktime;
						}

						if (pos + evenWidth < getWidth() - 2 * getTextSpace()
								&& ((Math.abs(pos - prePos) >= width / 9) || prePos == pos)) {
							canvas.drawLine(pos + evenWidth,
									DEFAULT_AXIS_TITLE_SIZE + 3, pos
											+ evenWidth, getUperChartHeight()
											+ DEFAULT_AXIS_TITLE_SIZE + 3,
									timePaint);

							canvas.drawLine(pos + evenWidth,
									getUperChartHeight()
											+ DEFAULT_AXIS_TITLE_SIZE + 3, pos
											+ evenWidth, getWidth(), timePaint);

							canvas.drawText(stockTimexs.substring(0, 4) + "-"
									+ stockTimexs.substring(4, 6), pos - 2
									* DEFAULT_AXIS_TITLE_SIZE + evenWidth,
									getUperChartHeight() + 2
											* DEFAULT_AXIS_TITLE_SIZE,
									textPaint);

							prePos = pos;

						}
					}
				} else if (from == 2) {
					String stocktime = dataList.get(mDataStartIndext + i)
							.getStocktime();
					String sub2 = stocktime.substring(4, 6);
					if ((sub2.equals("11") && (!whereM.equals(sub2)))
							|| (sub2.equals("05") && (!whereM.equals(sub2)))) {
						if (prePos == 0) {
							prePos = pos;
						}
						whereM = sub2;
						String stockTimexs = null;
						if (mDataStartIndext + i > 0) {
							stockTimexs = dataList
									.get(mDataStartIndext + i - 1)
									.getStocktime();
						} else {
							stockTimexs = stocktime;
						}

						if (pos + evenWidth < getWidth() - 2 * getTextSpace()
								&& ((Math.abs(pos - prePos) >= width / 9) || prePos == pos)) {
							canvas.drawLine(pos + evenWidth,
									DEFAULT_AXIS_TITLE_SIZE + 3, pos
											+ evenWidth, getUperChartHeight()
											+ DEFAULT_AXIS_TITLE_SIZE + 3,
									timePaint);

							canvas.drawLine(pos + evenWidth,
									getUperChartHeight()
											+ DEFAULT_AXIS_TITLE_SIZE + 3, pos
											+ evenWidth, getWidth(), timePaint);

							canvas.drawText(stockTimexs.substring(0, 4) + "-"
									+ stockTimexs.substring(4, 6), pos - 2
									* DEFAULT_AXIS_TITLE_SIZE + evenWidth,
									getUperChartHeight() + 2
											* DEFAULT_AXIS_TITLE_SIZE,
									textPaint);
							prePos = pos;
						}
					}
				} else if (from == 3) {
					String stocktime = dataList.get(mDataStartIndext + i)
							.getStocktime();
					String sub2 = stocktime.substring(3, 4);
					if (!sub2.equals(nSub)) {
						nSub = sub2;
						if (prePos == 0) {
							prePos = pos;
						}
						String stockTimexs = null;
						if (mDataStartIndext + i > 0) {
							stockTimexs = dataList
									.get(mDataStartIndext + i - 1)
									.getStocktime();
						} else {
							stockTimexs = stocktime;
						}
						if (pos + evenWidth < getWidth() - 2 * getTextSpace()
								&& ((Math.abs(pos - prePos) >= width / 9) || prePos == pos)) {
							canvas.drawLine(pos + evenWidth,
									DEFAULT_AXIS_TITLE_SIZE + 3, pos
											+ evenWidth, getUperChartHeight()
											+ DEFAULT_AXIS_TITLE_SIZE + 3,
									timePaint);

							canvas.drawLine(pos + evenWidth,
									getUperChartHeight()
											+ DEFAULT_AXIS_TITLE_SIZE + 3, pos
											+ evenWidth, getWidth(), timePaint);

							canvas.drawText(stockTimexs.substring(0, 4), pos
									+ evenWidth - DEFAULT_AXIS_TITLE_SIZE,
									getUperChartHeight() + 2
											* DEFAULT_AXIS_TITLE_SIZE,
									textPaint);
							prePos = pos;
						}
					}
				}
			}

		} else {
			float prePos = 0;
			// 小于默认显示个数
			String sub1 = dataList.get(mDataStartIndext).getStocktime()
					.substring(5, 6);
			String nSub = dataList.get(mDataStartIndext).getStocktime()
					.substring(3, 4);
			String whereM = "2";

			for (int i = 0; i < mShowDataNum
					&& mDataStartIndext + i < dataList.size(); i++) {
				float pos = ((evenWidth * i + evenWidth * (i + 1)) / 2 + getTextSpace());
				if (from == 1) {
					String stocktime = dataList.get(mDataStartIndext + i)
							.getStocktime();
					String sub2 = stocktime.substring(5, 6);
					if (!sub2.equals(sub1)) {
						if (prePos == 0) {
							prePos = pos;
						}
						sub1 = sub2;
						if (((Math.abs(pos - prePos) >= width / 9) || prePos == pos)) {
							canvas.drawLine(pos, DEFAULT_AXIS_TITLE_SIZE + 3,
									pos, getUperChartHeight()
											+ DEFAULT_AXIS_TITLE_SIZE + 3,
									timePaint);

							canvas.drawLine(pos, getUperChartHeight()
									+ DEFAULT_AXIS_TITLE_SIZE + 3, pos,
									getWidth(), timePaint);

							canvas.drawText(stocktime.substring(0, 4) + "-"
									+ stocktime.substring(4, 6), pos - 2
									* DEFAULT_AXIS_TITLE_SIZE,
									getUperChartHeight() + 2
											* DEFAULT_AXIS_TITLE_SIZE,
									textPaint);
							prePos = pos;
						}
					}

				} else if (from == 2) {
					String stocktime = dataList.get(mDataStartIndext + i)
							.getStocktime();
					String sub2 = stocktime.substring(4, 6);
					if ((sub2.equals("11") && (!whereM.equals(sub2)))
							|| (sub2.equals("05") && (!whereM.equals(sub2)))) {
						if (prePos == 0) {
							prePos = pos;
						}
						whereM = sub2;
						if (((Math.abs(pos - prePos) >= width / 9) || prePos == pos)) {
							canvas.drawLine(pos, DEFAULT_AXIS_TITLE_SIZE + 3,
									pos, getUperChartHeight()
											+ DEFAULT_AXIS_TITLE_SIZE + 3,
									timePaint);
							canvas.drawLine(pos, getUperChartHeight()
									+ DEFAULT_AXIS_TITLE_SIZE + 3, pos,
									getWidth(), timePaint);
							canvas.drawText(stocktime.substring(0, 4) + "-"
									+ stocktime.substring(4, 6), pos - 2
									* DEFAULT_AXIS_TITLE_SIZE,
									getUperChartHeight() + 2
											* DEFAULT_AXIS_TITLE_SIZE,
									textPaint);
							prePos = pos;
						}
					}
				} else if (from == 3) {
					String stocktime = dataList.get(mDataStartIndext + i)
							.getStocktime();
					String sub2 = stocktime.substring(3, 4);
					if (!sub2.equals(nSub)) {
						if (prePos == 0) {
							prePos = pos;
						}
						nSub = sub2;
						if (((Math.abs(pos - prePos) >= width / 9) || prePos == pos)) {
							canvas.drawLine(pos, DEFAULT_AXIS_TITLE_SIZE + 3,
									pos, getUperChartHeight()
											+ DEFAULT_AXIS_TITLE_SIZE + 3,
									timePaint);
							canvas.drawLine(pos, getUperChartHeight()
									+ DEFAULT_AXIS_TITLE_SIZE + 3, pos,
									getWidth(), timePaint);
							canvas.drawText(stocktime.substring(0, 4), pos
									- DEFAULT_AXIS_TITLE_SIZE,
									getUperChartHeight() + 2
											* DEFAULT_AXIS_TITLE_SIZE,
									textPaint);
						}
					}
				}

			}
		}
	}

	/**
	 * 画xy轴文字
	 * 
	 * @param canvas
	 */
	private void drawXYText(Canvas canvas) {
		textPaint.setColor(getResources().getColor(R.color.k_big_text));
		textPaint.setTextSize(DEFAULT_AXIS_TITLE_SIZE);
		canvas.drawText(decimalFormat.format(maxValue), 0,
				DEFAULT_AXIS_TITLE_SIZE, textPaint);
		canvas.drawText(decimalFormat.format(minValue), 0, getUperChartHeight()
				+ DEFAULT_AXIS_TITLE_SIZE, textPaint);
		canvas.drawText(decimalFormat.format((maxValue + minValue) / 2), 0,
				getUperChartHeight() / 2 + DEFAULT_AXIS_TITLE_SIZE, textPaint);

		if (maxVolume > 10E7) {
			canvas.drawText("亿手", 0, getHeight() - DEFAULT_AXIS_TITLE_SIZE / 2,
					textPaint);
			canvas.drawText(decimalFormat.format(maxVolume / 10E7), 0,
					getHeight() - lowerHeight + DEFAULT_AXIS_TITLE_SIZE / 2,
					textPaint);
		} else {
			canvas.drawText("万手", 0, getHeight() - DEFAULT_AXIS_TITLE_SIZE / 2,
					textPaint);
			canvas.drawText(decimalFormat.format(maxVolume / 10000), 0,
					getHeight() - lowerHeight + DEFAULT_AXIS_TITLE_SIZE / 2,
					textPaint);
		}
	}

	/**
	 * 画均线
	 * 
	 * @param canvas
	 * @param type
	 */
	private void drawLine(Canvas canvas, int type) {
		if (dataList != null && dataList.size() > 0) {
			float upHeight = getUperChartHeight() - 4;
			// float evenWidth = (float) width / mShowDataNum;
			linePaint.setStrokeWidth(1.5f);

			switch (type) {
			case 0:
				linePaint.setColor(getResources().getColor(R.color.k_lin5));
				for (int i = 0; i < mShowDataNum
						&& mDataStartIndext + i < dataList.size(); i++) {

					startX = (width - evenWidth * i + getTextSpace());
					if (TextUtils.isEmpty(dataList.get(mDataStartIndext + i)
							.getLine5())) {

					} else if (dataList.get(mDataStartIndext + i).getLine5()
							.equals("0")) {
						startY = (float) ((1 - (Double.parseDouble(dataList
								.get(mDataStartIndext + i).getLine5()) - minValue)
								/ (offset * wight))
								* upHeight + downHeight);
					} else {
						startY = (float) ((1 - (Double.parseDouble(dataList
								.get(mDataStartIndext + i).getLine5()) - minValue)
								/ (offset * wight))
								* upHeight + downHeight);

					}

					stopX = (width - evenWidth * (i + 1) + getTextSpace());
					if (mDataStartIndext + i + 1 == dataList.size()) {

					} else {
						if (dataList.get(mDataStartIndext + i + 1).getLine5()
								.equals("0")) {

						} else {
							stopY = (float) ((1 - (Double.parseDouble(dataList
									.get(mDataStartIndext + i + 1).getLine5()) - minValue)
									/ (offset * wight))
									* upHeight + downHeight);
						}
					}

					if (dataList.get(mDataStartIndext + i).getLine5()
							.equals("0")) {

					} else {
						canvas.drawLine(startX, startY, stopX, stopY, linePaint);
					}
				}

				break;
			case 1:
				linePaint.setColor(getResources().getColor(R.color.k_lin10));
				for (int i = 0; i < mShowDataNum
						&& mDataStartIndext + i < dataList.size(); i++) {

					if (mDataStartIndext + i + 1 == dataList.size()) {

					} else {
						startX = (width - evenWidth * i + getTextSpace());
						if (dataList.get(mDataStartIndext + i).getLine10()
								.equals("")) {
						} else {
							startY = (float) ((1 - (Double.parseDouble(dataList
									.get(mDataStartIndext + i).getLine10()) - minValue)
									/ (offset * wight))
									* upHeight + downHeight);
						}
						stopX = (width - evenWidth * (i + 1) + getTextSpace());
						if (dataList.get(mDataStartIndext + i + 1).getLine10()
								.equals("")) {

						} else {
							if (dataList.get(mDataStartIndext + i + 1)
									.getLine10().equals("0")) {

							} else {
								stopY = (float) ((1 - (Double
										.parseDouble(dataList.get(
												mDataStartIndext + i + 1)
												.getLine10()) - minValue)
										/ (offset * wight))
										* upHeight + downHeight);
							}
						}
					}
					if (dataList.get(mDataStartIndext + i).getLine10()
							.equals("0")) {

					} else {
						canvas.drawLine(startX, startY, stopX, stopY, linePaint);
					}
				}
				break;
			case 2:
				linePaint.setColor(getResources().getColor(R.color.k_lin20));
				for (int i = 0; i < mShowDataNum
						&& mDataStartIndext + i < dataList.size(); i++) {

					if (mDataStartIndext + i + 1 == dataList.size()) {

					} else {
						startX = (width - evenWidth * i + getTextSpace());
						if (dataList.get(mDataStartIndext + i).getLine20()
								.equals("")) {
						} else {
							startY = (float) ((1 - (Double.parseDouble(dataList
									.get(mDataStartIndext + i).getLine20()) - minValue)
									/ (offset * wight))
									* upHeight + downHeight);
						}
						stopX = (width - evenWidth * (i + 1) + getTextSpace());
						if (dataList.get(mDataStartIndext + i + 1).getLine20()
								.equals("")) {

						} else {
							if (dataList.get(mDataStartIndext + i + 1)
									.getLine20().equals("0")) {

							} else {
								stopY = (float) ((1 - (Double
										.parseDouble(dataList.get(
												mDataStartIndext + i + 1)
												.getLine20()) - minValue)
										/ (offset * wight))
										* upHeight + downHeight);
							}
						}
					}
					if (dataList.get(mDataStartIndext + i).getLine20()
							.equals("0")) {

					} else {
						canvas.drawLine(startX, startY, stopX, stopY, linePaint);
					}
				}

				break;
			default:
				break;
			}

		}
	}

	/**
	 * 画蜡烛
	 * 
	 * @param canvas
	 */
	private void drawCand(Canvas canvas) {
		canvas.translate(-1.0f, -1.0f);

		float rate = (getUperChartHeight() - 2) / (maxValue - minValue);
		float upHeight = getUperChartHeight() - 4;
		if (dataList != null && dataList.size() > 0) {

			// float evenHeight = (float) (upHeight / 5);
			for (int i = 0; i < mShowDataNum
					&& mDataStartIndext + i < dataList.size(); i++) {

				float high = (float) Double.parseDouble(dataList.get(
						mDataStartIndext + i).getHigh());
				float low = (float) Double.parseDouble(dataList.get(
						mDataStartIndext + i).getLow());
				float fopen = (float) Double.parseDouble(dataList.get(
						mDataStartIndext + i).getNewprice());
				float lastclose = (float) Double.parseDouble(dataList.get(
						mDataStartIndext + i).getLastclose());
				float pos = (float) (width
						- (evenWidth * i + evenWidth * (i + 1)) / 2 + getTextSpace());
				float newprice = (float) Double.parseDouble(dataList.get(
						mDataStartIndext + i).getFopen());
				// L.w("even width:"+evenWidth);
				if (fopen >= newprice) {
					linePaint
							.setColor(getResources().getColor(R.color.font_up));

					linePaint.setStrokeWidth(1);
					canvas.drawLine(pos, (1 - (low - minValue)
							/ (offset * wight))
							* upHeight + downHeight2, pos,
							(1 - (high - minValue) / (offset * wight))
									* upHeight + downHeight2, linePaint);
					linePaint.setStrokeWidth(12);

					if (Math.abs(newprice - fopen) <= 1) {
						if (newprice - fopen == 0) {
							float ly = ((1 - (low - minValue)
									/ (offset * wight))
									* upHeight
									+ downHeight2
									+ (1 - (high - minValue) / (offset * wight))
									* upHeight + downHeight2) / 2;
							if (fopen > lastclose) {
								linePaint.setColor(getResources().getColor(
										R.color.font_up));
								canvas.drawRect(pos - (evenWidth / 2) + 1,
										ly + 1, pos + (evenWidth / 2) - 1,
										ly + 3, linePaint);
							} else {
								linePaint.setColor(getResources().getColor(
										R.color.font_green));
								canvas.drawRect(pos - (evenWidth / 2) + 1,
										ly + 1, pos + (evenWidth / 2) - 1,
										ly + 3, linePaint);
							}
						} else {
							canvas.drawRect(pos - (evenWidth / 2) + 1,
									(1 - (fopen - minValue) / (offset * wight))
											* upHeight + downHeight2, pos
											+ (evenWidth / 2) - 1,
									(1 - (newprice - minValue)
											/ (offset * wight))
											* upHeight + downHeight2, linePaint);
						}
					} else {
						canvas.drawRect(pos - (evenWidth / 2) + 1,
								(1 - (fopen - minValue) / (offset * wight))
										* upHeight + downHeight2, pos
										+ (evenWidth / 2) - 1,
								(1 - (newprice - minValue) / (offset * wight))
										* upHeight + downHeight2, linePaint);
					}

				} else if (fopen < newprice) {
					linePaint.setColor(getResources().getColor(
							R.color.font_green));

					linePaint.setStrokeWidth(1);
					canvas.drawLine(pos, (1 - (low - minValue)
							/ (offset * wight))
							* upHeight + downHeight2, pos,
							(1 - (high - minValue) / (offset * wight))
									* upHeight + downHeight2, linePaint);
					linePaint.setStrokeWidth(12);
					if (Math.abs(newprice - fopen) <= 1) {

						canvas.drawRect(pos - (evenWidth / 2) + 1,
								(1 - (newprice - minValue) / (offset * wight))
										* upHeight + downHeight2, pos
										+ (evenWidth / 2) - 1,
								(1 - (fopen - minValue) / (offset * wight))
										* upHeight + downHeight2, linePaint);
					} else {
						canvas.drawRect(pos - (evenWidth / 2) + 1,
								(1 - (newprice - minValue) / (offset * wight))
										* upHeight + downHeight2, pos
										+ (evenWidth / 2) - 1,
								(1 - (fopen - minValue) / (offset * wight))
										* upHeight + downHeight2, linePaint);
					}
				} else {

					if (fopen >= lastclose) {
						linePaint.setColor(getResources().getColor(
								R.color.font_up));
						linePaint.setStrokeWidth(1);
						canvas.drawLine(pos, (1 - (low - minValue)
								/ (offset * wight))
								* upHeight + downHeight2, pos,
								(1 - (high - minValue) / (offset * wight))
										* upHeight + downHeight2, linePaint);
					} else {
						linePaint.setColor(getResources().getColor(
								R.color.font_green));
						linePaint.setStrokeWidth(1);
						canvas.drawLine(pos, (1 - (low - minValue)
								/ (offset * wight))
								* upHeight + downHeight2, pos,
								(1 - (high - minValue) / (offset * wight))
										* upHeight + downHeight2, linePaint);
					}
					linePaint.setStrokeWidth(12);
					canvas.drawRect(pos - (evenWidth / 2) + 1,
							(1 - (fopen - minValue) / (offset * wight))
									* upHeight + downHeight2, pos
									+ (evenWidth / 2) - 1,
							(1 - (fopen - minValue) / (offset * wight))
									* upHeight + downHeight2, linePaint);
				}
			}
		}
	}

	/**
	 * 画底部交易量
	 * 
	 * @param canvas
	 */
	private void drawColum(Canvas canvas) {
		pointPaint.setStrokeWidth(1.0f);
		pointPaint.setColor(getResources().getColor(R.color.font_up));
		pointPaint.setStyle(Style.FILL);
		timePaint.setStrokeWidth(1.0f);
		timePaint.setColor(getResources().getColor(R.color.k_bg_line));
		textPaint.setColor(getResources().getColor(R.color.k_big_text));
		textPaint.setTextSize(DEFAULT_AXIS_TITLE_SIZE);
		if (dataList != null && dataList.size() > 0) {
			// float evenWidth=width/mShowDataNum;
			float lowertop = LOWER_CHART_TOP + 1;
			float lowerHight = getHeight() - lowertop - 4;
			String sub1 = dataList.get(mDataStartIndext).getStocktime()
					.substring(5, 6);
			String nSub = dataList.get(mDataStartIndext).getStocktime()
					.substring(3, 4);
			String whereM = "2";
			int dn = 0;
			mPaint.setStrokeWidth(evenWidth - 2);
			for (int i = 0; i < mShowDataNum
					&& mDataStartIndext + i < dataList.size(); i++) {
				float pos = (width - (evenWidth * i + evenWidth * (i + 1)) / 2 + getTextSpace());
				String fopen = dataList.get(mDataStartIndext + i).getNewprice();
				String lastclose = dataList.get(mDataStartIndext + i)
						.getFopen();
				if (Double.parseDouble(fopen) >=Double.parseDouble(lastclose)) {
					mPaint.setColor(getResources().getColor(R.color.font_up));
				} else {
					mPaint.setColor(getResources().getColor(R.color.font_green));
				}
				num++;
				// 画赋权周期日的点
				if (from == 1) {
					String quanxi = dataList.get(mDataStartIndext + i)
							.getQuanxi();
					if (!TextUtils.isEmpty(quanxi) && "1".equals(quanxi)) {
						canvas.drawCircle(pos, UPER_CHART_BOTTOM
								- DEFAULT_AXIS_TITLE_SIZE / 2 - 2,
								DEFAULT_AXIS_TITLE_SIZE / 4, pointPaint);
					}

				}

				/**
				 * // 画时间线 if (from == 1) { String stocktime =
				 * dataList.get(mDataStartIndext + i) .getStocktime(); String
				 * sub2 = stocktime.substring(5, 6); if (!sub2.equals(sub1)) {
				 * sub1 = sub2; String stockTimexs = null; if (mDataStartIndext
				 * + i > 0) { stockTimexs = dataList .get(mDataStartIndext + i -
				 * 1) .getStocktime(); } else { stockTimexs = stocktime; }
				 * 
				 * if (pos + evenWidth < getWidth() - 2 * getTextSpace()) {
				 * canvas.drawLine(pos + evenWidth, DEFAULT_AXIS_TITLE_SIZE + 3,
				 * pos + evenWidth, getUperChartHeight() +
				 * DEFAULT_AXIS_TITLE_SIZE + 3, timePaint);
				 * 
				 * canvas.drawLine(pos + evenWidth, getUperChartHeight() +
				 * DEFAULT_AXIS_TITLE_SIZE + 3, pos + evenWidth, getWidth(),
				 * timePaint);
				 * 
				 * canvas.drawText(stockTimexs.substring(0, 4) + "-" +
				 * stockTimexs.substring(4, 6), pos - 2 DEFAULT_AXIS_TITLE_SIZE
				 * + evenWidth, getUperChartHeight() + 2
				 * DEFAULT_AXIS_TITLE_SIZE, textPaint);
				 * 
				 * } }
				 * 
				 * } else if (from == 2) { String stocktime =
				 * dataList.get(mDataStartIndext + i) .getStocktime(); String
				 * sub2 = stocktime.substring(4, 6); if ((sub2.equals("11") &&
				 * (!whereM.equals(sub2))) || (sub2.equals("05") &&
				 * (!whereM.equals(sub2)))) { whereM = sub2; String stockTimexs
				 * = null; if (mDataStartIndext + i > 0) { stockTimexs =
				 * dataList .get(mDataStartIndext + i - 1) .getStocktime(); }
				 * else { stockTimexs = stocktime; }
				 * 
				 * if (pos + evenWidth < getWidth() - 2 * getTextSpace()) {
				 * canvas.drawLine(pos + evenWidth, DEFAULT_AXIS_TITLE_SIZE + 3,
				 * pos + evenWidth, getUperChartHeight() +
				 * DEFAULT_AXIS_TITLE_SIZE + 3, timePaint);
				 * 
				 * canvas.drawLine(pos + evenWidth, getUperChartHeight() +
				 * DEFAULT_AXIS_TITLE_SIZE + 3, pos + evenWidth, getWidth(),
				 * timePaint);
				 * 
				 * canvas.drawText(stockTimexs.substring(0, 4) + "-" +
				 * stockTimexs.substring(4, 6), pos - 2 DEFAULT_AXIS_TITLE_SIZE
				 * + evenWidth, getUperChartHeight() + 2
				 * DEFAULT_AXIS_TITLE_SIZE, textPaint); } } } else if (from ==
				 * 3) { String stocktime = dataList.get(mDataStartIndext + i)
				 * .getStocktime(); String sub2 = stocktime.substring(3, 4); if
				 * (!sub2.equals(nSub)) { nSub = sub2; String stockTimexs =
				 * null; if (mDataStartIndext + i > 0) { stockTimexs = dataList
				 * .get(mDataStartIndext + i - 1) .getStocktime(); } else {
				 * stockTimexs = stocktime; } if (pos + evenWidth < getWidth() -
				 * 2 * getTextSpace()) { canvas.drawLine(pos + evenWidth,
				 * DEFAULT_AXIS_TITLE_SIZE + 3, pos + evenWidth,
				 * getUperChartHeight() + DEFAULT_AXIS_TITLE_SIZE + 3,
				 * timePaint);
				 * 
				 * canvas.drawLine(pos + evenWidth, getUperChartHeight() +
				 * DEFAULT_AXIS_TITLE_SIZE + 3, pos + evenWidth, getWidth(),
				 * timePaint);
				 * 
				 * canvas.drawText(stockTimexs.substring(0, 4), pos + evenWidth
				 * - DEFAULT_AXIS_TITLE_SIZE, getUperChartHeight() + 2
				 * DEFAULT_AXIS_TITLE_SIZE, textPaint); } } }
				 **/
				// ++++++++++++++++++++++++++++++++++++++
				canvas.drawRect(
						pos - (evenWidth / 2) + 1,
						(float) ((height - lowerHight) + ((1 - Double
								.parseDouble(dataList.get(mDataStartIndext + i)
										.getVolume())
								/ (maxVolume * wight)) * lowerHight)), pos
								+ (evenWidth / 2), height, mPaint);

			}

		}
	}

	/**
	 * +++++++++++++++++++++++++++++算法2+++++++++++++++++++++++
	 */
	private void drawLine2(Canvas canvas, int type) {
		if (dataList != null && dataList.size() > 0) {
			float upHeight = getUperChartHeight() - 4;
			// float evenWidth = (float) width / mShowDataNum;
			linePaint.setStrokeWidth(1.5f);

			switch (type) {
			case 0:
				linePaint.setColor(getResources().getColor(R.color.k_lin5));
				for (int i = 0; i < mShowDataNum
						&& mDataStartIndext + i < dataList.size(); i++) {

					startX = (evenWidth * i + getTextSpace());
					if (TextUtils.isEmpty(dataList.get(mDataStartIndext + i)
							.getLine5())) {

					} else if (dataList.get(mDataStartIndext + i).getLine5()
							.equals("0")) {
						startY = (float) ((1 - (Double.parseDouble(dataList
								.get(mDataStartIndext + i).getLine5()) - minValue)
								/ (offset * wight))
								* upHeight + downHeight);
					} else {
						startY = (float) ((1 - (Double.parseDouble(dataList
								.get(mDataStartIndext + i).getLine5()) - minValue)
								/ (offset * wight))
								* upHeight + downHeight);

					}

					stopX = (evenWidth * (i + 1) + getTextSpace());
					if (mDataStartIndext + i + 1 == dataList.size()) {

					} else {
						if (dataList.get(mDataStartIndext + i + 1).getLine5()
								.equals("0")) {

						} else {
							stopY = (float) ((1 - (Double.parseDouble(dataList
									.get(mDataStartIndext + i + 1).getLine5()) - minValue)
									/ (offset * wight))
									* upHeight + downHeight);
						}
					}

					if (dataList.get(mDataStartIndext + i).getLine5()
							.equals("0")) {

					} else {
						canvas.drawLine(startX, startY, stopX, stopY, linePaint);
					}
				}

				break;
			case 1:
				linePaint.setColor(getResources().getColor(R.color.k_lin10));
				for (int i = 0; i < mShowDataNum
						&& mDataStartIndext + i < dataList.size(); i++) {

					if (mDataStartIndext + i + 1 == dataList.size()) {

					} else {
						startX = (evenWidth * i + getTextSpace());
						if (dataList.get(mDataStartIndext + i).getLine10()
								.equals("")) {
						} else {
							startY = (float) ((1 - (Double.parseDouble(dataList
									.get(mDataStartIndext + i).getLine10()) - minValue)
									/ (offset * wight))
									* upHeight + downHeight);
						}
						stopX = (evenWidth * (i + 1) + getTextSpace());
						if (dataList.get(mDataStartIndext + i + 1).getLine10()
								.equals("")) {

						} else {
							if (dataList.get(mDataStartIndext + i + 1)
									.getLine10().equals("0")) {

							} else {
								stopY = (float) ((1 - (Double
										.parseDouble(dataList.get(
												mDataStartIndext + i + 1)
												.getLine10()) - minValue)
										/ (offset * wight))
										* upHeight + downHeight);
							}
						}
					}
					if (dataList.get(mDataStartIndext + i).getLine10()
							.equals("0")) {

					} else {
						canvas.drawLine(startX, startY, stopX, stopY, linePaint);
					}
				}
				break;
			case 2:
				linePaint.setColor(getResources().getColor(R.color.k_lin20));
				for (int i = 0; i < mShowDataNum
						&& mDataStartIndext + i < dataList.size(); i++) {

					if (mDataStartIndext + i + 1 == dataList.size()) {

					} else {
						startX = (evenWidth * i + getTextSpace());
						if (dataList.get(mDataStartIndext + i).getLine20()
								.equals("")) {
						} else {
							startY = (float) ((1 - (Double.parseDouble(dataList
									.get(mDataStartIndext + i).getLine20()) - minValue)
									/ (offset * wight))
									* upHeight + downHeight);
						}
						stopX = (evenWidth * (i + 1) + getTextSpace());
						if (dataList.get(mDataStartIndext + i + 1).getLine20()
								.equals("")) {

						} else {
							if (dataList.get(mDataStartIndext + i + 1)
									.getLine20().equals("0")) {

							} else {
								stopY = (float) ((1 - (Double
										.parseDouble(dataList.get(
												mDataStartIndext + i + 1)
												.getLine20()) - minValue)
										/ (offset * wight))
										* upHeight + downHeight);
							}
						}
					}
					if (dataList.get(mDataStartIndext + i).getLine20()
							.equals("0")) {

					} else {
						canvas.drawLine(startX, startY, stopX, stopY, linePaint);
					}
				}

				break;
			default:
				break;
			}

		}
	}

	/**
	 * 画蜡烛
	 * 
	 * @param canvas
	 */
	private void drawCand2(Canvas canvas) {
		canvas.translate(-1.0f, -1.0f);

		float rate = (getUperChartHeight() - 2) / (maxValue - minValue);
		float upHeight = getUperChartHeight() - 4;
		if (dataList != null && dataList.size() > 0) {

			// float evenHeight = (float) (upHeight / 5);
			for (int i = 0; i < mShowDataNum
					&& mDataStartIndext + i < dataList.size(); i++) {

				float high = (float) Double.parseDouble(dataList.get(
						mDataStartIndext + i).getHigh());
				float low = (float) Double.parseDouble(dataList.get(
						mDataStartIndext + i).getLow());
				float fopen = (float) Double.parseDouble(dataList.get(
						mDataStartIndext + i).getNewprice());
				float lastclose = (float) Double.parseDouble(dataList.get(
						mDataStartIndext + i).getLastclose());
				float pos = (float) ((evenWidth * i + evenWidth * (i + 1)) / 2 + getTextSpace());
				float newprice = (float) Double.parseDouble(dataList.get(
						mDataStartIndext + i).getFopen());
				// L.w("even width:"+evenWidth);
				if (fopen >= newprice) {
					linePaint
							.setColor(getResources().getColor(R.color.font_up));

					linePaint.setStrokeWidth(1);
					canvas.drawLine(pos, (1 - (low - minValue)
							/ (offset * wight))
							* upHeight + downHeight, pos,
							(1 - (high - minValue) / (offset * wight))
									* upHeight + downHeight, linePaint);
					linePaint.setStrokeWidth(12);

					if (Math.abs(newprice - fopen) <= 1) {
						if (newprice == fopen) {

							float ly = ((1 - (low - minValue)
									/ (offset * wight))
									* upHeight
									+ downHeight
									+ (1 - (high - minValue) / (offset * wight))
									* upHeight + downHeight) / 2;
							if (fopen > lastclose) {
								linePaint.setColor(getResources().getColor(
										R.color.font_up));
								canvas.drawRect(pos - (evenWidth / 2) + 1,
										ly + 1, pos + (evenWidth / 2) - 1,
										ly + 3, linePaint);
							} else {
								linePaint.setColor(getResources().getColor(
										R.color.font_green));
								canvas.drawRect(pos - (evenWidth / 2) + 1,
										ly + 1, pos + (evenWidth / 2) - 1,
										ly + 3, linePaint);
							}
						} else {

							canvas.drawRect(pos - (evenWidth / 2) + 1,
									(1 - (fopen - minValue) / (offset * wight))
											* upHeight + downHeight, pos
											+ (evenWidth / 2) - 1,
									(1 - (newprice - minValue)
											/ (offset * wight))
											* upHeight + downHeight, linePaint);
						}
					} else {
						canvas.drawRect(pos - (evenWidth / 2) + 1,
								(1 - (fopen - minValue) / (offset * wight))
										* upHeight + downHeight, pos
										+ (evenWidth / 2) - 1,
								(1 - (newprice - minValue) / (offset * wight))
										* upHeight + downHeight, linePaint);
					}

				} else if (fopen < newprice) {
					linePaint.setColor(getResources().getColor(
							R.color.font_green));

					linePaint.setStrokeWidth(1);
					canvas.drawLine(pos, (1 - (low - minValue)
							/ (offset * wight))
							* upHeight + downHeight, pos,
							(1 - (high - minValue) / (offset * wight))
									* upHeight + downHeight, linePaint);
					linePaint.setStrokeWidth(12);
					if (Math.abs(newprice - fopen) <= 1) {
						canvas.drawRect(pos - (evenWidth / 2) + 1,
								(1 - (newprice - minValue) / (offset * wight))
										* upHeight + downHeight, pos
										+ (evenWidth / 2) - 1,
								(1 - (fopen - minValue) / (offset * wight))
										* upHeight + downHeight, linePaint);
					} else {
						canvas.drawRect(pos - (evenWidth / 2) + 1,
								(1 - (newprice - minValue) / (offset * wight))
										* upHeight + downHeight, pos
										+ (evenWidth / 2) - 1,
								(1 - (fopen - minValue) / (offset * wight))
										* upHeight + downHeight, linePaint);
					}
				} else {

					if (fopen >= lastclose) {
						linePaint.setColor(getResources().getColor(
								R.color.font_up));
						linePaint.setStrokeWidth(1);
						canvas.drawLine(pos, (1 - (low - minValue)
								/ (offset * wight))
								* upHeight + downHeight, pos,
								(1 - (high - minValue) / (offset * wight))
										* upHeight + downHeight, linePaint);
					} else {
						linePaint.setColor(getResources().getColor(
								R.color.font_green));
						linePaint.setStrokeWidth(1);
						canvas.drawLine(pos, (1 - (low - minValue)
								/ (offset * wight))
								* upHeight + downHeight, pos,
								(1 - (high - minValue) / (offset * wight))
										* upHeight + downHeight, linePaint);
					}
					linePaint.setStrokeWidth(12);
					canvas.drawRect(pos - (evenWidth / 2) + 1,
							(1 - (fopen - minValue) / (offset * wight))
									* upHeight + downHeight, pos
									+ (evenWidth / 2) - 1,
							(1 - (fopen - minValue) / (offset * wight))
									* upHeight + downHeight, linePaint);
				}
			}
		}
	}

	/**
	 * 画底部交易量
	 * 
	 * @param canvas
	 */
	private void drawColum2(Canvas canvas) {
		timePaint.setStrokeWidth(1.0f);
		timePaint.setColor(getResources().getColor(R.color.k_bg_line));
		textPaint.setColor(getResources().getColor(R.color.k_big_text));
		textPaint.setTextSize(DEFAULT_AXIS_TITLE_SIZE);
		if (dataList != null && dataList.size() > 0) {
			// float evenWidth=width/mShowDataNum;
			float lowertop = LOWER_CHART_TOP + 1;
			float lowerHight = getHeight() - lowertop - 4;
			String sub1 = dataList.get(mDataStartIndext).getStocktime()
					.substring(5, 6);
			String nSub = dataList.get(mDataStartIndext).getStocktime()
					.substring(3, 4);
			String whereM = "2";
			mPaint.setStrokeWidth(evenWidth - 2);
			for (int i = 0; i < mShowDataNum
					&& mDataStartIndext + i < dataList.size(); i++) {
				float pos = ((evenWidth * i + evenWidth * (i + 1)) / 2 + getTextSpace());
				String fopen = dataList.get(mDataStartIndext + i).getNewprice();
				String lastclose = dataList.get(mDataStartIndext + i)
						.getFopen();
				if (Double.parseDouble(fopen) >= Double.parseDouble(lastclose)) {
					mPaint.setColor(getResources().getColor(R.color.font_up));
				} else {
					mPaint.setColor(getResources().getColor(R.color.font_green));
				}
				num++;
				/**
				 * // 画时间线 if (from == 1) { String stocktime =
				 * dataList.get(mDataStartIndext + i) .getStocktime(); String
				 * sub2 = stocktime.substring(5, 6); if (!sub2.equals(sub1)) {
				 * sub1 = sub2; canvas.drawLine(pos, DEFAULT_AXIS_TITLE_SIZE +
				 * 3, pos, getUperChartHeight() + DEFAULT_AXIS_TITLE_SIZE + 3,
				 * timePaint);
				 * 
				 * canvas.drawLine(pos, getUperChartHeight() +
				 * DEFAULT_AXIS_TITLE_SIZE + 3, pos, getWidth(), timePaint);
				 * 
				 * canvas.drawText(stocktime.substring(0, 4) + "-" +
				 * stocktime.substring(4, 6), pos - 2 DEFAULT_AXIS_TITLE_SIZE,
				 * getUperChartHeight() + 2 * DEFAULT_AXIS_TITLE_SIZE,
				 * textPaint); }
				 * 
				 * } else if (from == 2) { String stocktime =
				 * dataList.get(mDataStartIndext + i) .getStocktime(); String
				 * sub2 = stocktime.substring(4, 6); if ((sub2.equals("11") &&
				 * (!whereM.equals(sub2))) || (sub2.equals("05") &&
				 * (!whereM.equals(sub2)))) { whereM = sub2;
				 * canvas.drawLine(pos, DEFAULT_AXIS_TITLE_SIZE + 3, pos,
				 * getUperChartHeight() + DEFAULT_AXIS_TITLE_SIZE + 3,
				 * timePaint); canvas.drawLine(pos, getUperChartHeight() +
				 * DEFAULT_AXIS_TITLE_SIZE + 3, pos, getWidth(), timePaint);
				 * canvas.drawText(stocktime.substring(0, 4) + "-" +
				 * stocktime.substring(4, 6), pos - 2 DEFAULT_AXIS_TITLE_SIZE,
				 * getUperChartHeight() + 2 * DEFAULT_AXIS_TITLE_SIZE,
				 * textPaint); } } else if (from == 3) { String stocktime =
				 * dataList.get(mDataStartIndext + i) .getStocktime(); String
				 * sub2 = stocktime.substring(3, 4); if (!sub2.equals(nSub)) {
				 * nSub = sub2; canvas.drawLine(pos, DEFAULT_AXIS_TITLE_SIZE +
				 * 3, pos, getUperChartHeight() + DEFAULT_AXIS_TITLE_SIZE + 3,
				 * timePaint); canvas.drawLine(pos, getUperChartHeight() +
				 * DEFAULT_AXIS_TITLE_SIZE + 3, pos, getWidth(), timePaint);
				 * canvas.drawText(stocktime.substring(0, 4), pos -
				 * DEFAULT_AXIS_TITLE_SIZE, getUperChartHeight() + 2 *
				 * DEFAULT_AXIS_TITLE_SIZE, textPaint); } } //
				 * ++++++++++++++++++++++++++++++++++=
				 **/
				canvas.drawRect(
						pos - (evenWidth / 2) + 1,
						(float) ((height - lowerHight) + ((1 - Double
								.parseDouble(dataList.get(mDataStartIndext + i)
										.getVolume())
								/ (maxVolume * wight)) * lowerHight)), pos
								+ (evenWidth / 2), height, mPaint);
			}

		}
	}

	/**
	 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 */

	/**
	 * 画滑动线
	 */
	private void drawHDLine(Canvas canvas) {
		if (moveListener != null) {
			moveListener.doMove();
		}
		areaPaint.setColor(getResources().getColor(R.color.k_area_fq));
		areaPaint.setStyle(Style.FILL);
		areaPaint.setAlpha(100);
		mPaint.setColor(getResources().getColor(R.color.k_point_line));
		mPaint.setStrokeWidth(2.0f);
		if (touchX - getTextSpace() - 2 > 0
				&& touchX < getTextSpace() + 1 + width) {
			touchNum = (int) ((touchX - getTextSpace() - 1) / width * mShowDataNum);
		} else if (touchX - getTextSpace() - 2 < 0) {
			touchNum = 0;
		} else if (touchX > getTextSpace() + 1 + width) {
			touchNum = mShowDataNum - 1;
		}
		if (showDetails) {
			if(dataList==null||dataList.size()==0){
				return;
			}
			if (touchX - getTextSpace() <= 0) {
				canvas.drawLine(getTextSpace(), 0, getTextSpace(), height,
						mPaint);
			} else if (touchX > 0 && touchX < width + getTextSpace()) {

				if (touchNum < dataList.size()) {
					canvas.drawLine(getTextSpace() + evenWidth * (touchNum)
							+ (evenWidth / 2) + 4, 0, getTextSpace()
							+ evenWidth * (touchNum) + (evenWidth / 2) + 4,
							height, mPaint);
				} else {
					canvas.drawLine(
							getTextSpace() + evenWidth * dataList.size(), 0,
							getTextSpace() + evenWidth * dataList.size(),
							height, mPaint);
				}
			} else {
				canvas.drawLine(width + getTextSpace(), 0, width
						+ getTextSpace(), height, mPaint);
			}

			/**
			 * 画交易量显示区域
			 */
			canvas.drawRect(getTextSpace(), LOWER_CHART_TOP, getTextSpace() + 5
					* DEFAULT_AXIS_TITLE_SIZE, LOWER_CHART_TOP + 1.5f
					* DEFAULT_AXIS_TITLE_SIZE, areaPaint);

			if (!isSmall) {
				String volumePoint = dataList.get(
						mShowDataNum - touchNum + mDataStartIndext - 1)
						.getVolume();
				double parseDouble = Double.parseDouble(volumePoint);
				if (parseDouble >= 10E7) {
					canvas.drawText(decimalFormat.format(parseDouble / 10E7)
							+ "亿", getTextSpace(), LOWER_CHART_TOP
							+ DEFAULT_AXIS_TITLE_SIZE, textPaint);
				}
				if (parseDouble >= 10E3 && parseDouble < 10E7) {
					canvas.drawText(decimalFormat.format(parseDouble / 10E3)
							+ "万", getTextSpace(), LOWER_CHART_TOP
							+ DEFAULT_AXIS_TITLE_SIZE, textPaint);
				}

				if (parseDouble >= 0 && parseDouble < 10E3) {
					canvas.drawText(parseDouble + "", getTextSpace(),
							LOWER_CHART_TOP + 1.4f * DEFAULT_AXIS_TITLE_SIZE,
							textPaint);
				}

			} else {
				String volumePoint = null;
				if (mDataStartIndext + touchNum >= dataList.size()) {
					volumePoint = dataList.get(dataList.size() - 1).getVolume();
				} else {
					volumePoint = dataList.get(mDataStartIndext + touchNum)
							.getVolume();
				}
				double parseDouble = Double.parseDouble(volumePoint);
				if (parseDouble >= 10E7) {
					canvas.drawText(decimalFormat.format(parseDouble / 10E7)
							+ "亿", getTextSpace(), LOWER_CHART_TOP
							+ DEFAULT_AXIS_TITLE_SIZE, textPaint);
				}
				if (parseDouble >= 10E4 && parseDouble < 10E7) {
					canvas.drawText(decimalFormat.format(parseDouble / 10E3)
							+ "万", getTextSpace(), LOWER_CHART_TOP
							+ DEFAULT_AXIS_TITLE_SIZE, textPaint);
				}
				if (parseDouble >= 0 && parseDouble < 10E4) {
					canvas.drawText(parseDouble + "", getTextSpace(),
							LOWER_CHART_TOP + 1.4f * DEFAULT_AXIS_TITLE_SIZE,
							textPaint);
				}
			}

			/**
			 * 画赋权区域
			 */
			if (from == 1) {
				if (!isSmall) {
					String quanxi = dataList.get(
							mShowDataNum - touchNum + mDataStartIndext - 1)
							.getQuanxi();
					String perhongli = dataList.get(
							mShowDataNum - touchNum + mDataStartIndext - 1)
							.getPerhongli();
					String persong = dataList.get(
							mShowDataNum - touchNum + mDataStartIndext - 1)
							.getPersong();
					String prepei = dataList.get(
							mShowDataNum - touchNum + mDataStartIndext - 1)
							.getPrepei();

					if (touchX > getWidth() / 2.0f) {
						if (!TextUtils.isEmpty(quanxi) && "1".equals(quanxi)) {
							canvas.drawRect(getTextSpace(),
									1.2f * DEFAULT_AXIS_TITLE_SIZE,
									getTextSpace() + 6.5f
											* DEFAULT_AXIS_TITLE_SIZE,
									2.5f * DEFAULT_AXIS_TITLE_SIZE, areaPaint);
							String str = "10";
							if (!TextUtils.isEmpty(perhongli)
									&& !"0".equals(perhongli)) {
								str = str + "派" + perhongli + "元";
							}
							if (!TextUtils.isEmpty(persong)
									&& !"0".equals(persong)) {
								str = str + "送" + persong + "股";

							}
							if (!TextUtils.isEmpty(prepei)
									&& !"0".equals(prepei)) {
								str = str + "配" + prepei + "股";
							}

							canvas.drawText(str, getTextSpace() + 2,
									2.2f * DEFAULT_AXIS_TITLE_SIZE, textPaint);

						}

					} else {
						if (!TextUtils.isEmpty(quanxi) && "1".equals(quanxi)) {
							canvas.drawRect(getWidth() - getTextSpace() - 6.5f
									* DEFAULT_AXIS_TITLE_SIZE,
									1.2f * DEFAULT_AXIS_TITLE_SIZE, getWidth()
											- getTextSpace(),
									2.5f * DEFAULT_AXIS_TITLE_SIZE, areaPaint);
							String str = "10";
							if (!TextUtils.isEmpty(perhongli)
									&& !"0".equals(perhongli)) {
								str = str + "派" + perhongli + "元";
							}
							if (!TextUtils.isEmpty(persong)
									&& !"0".equals(persong)) {
								str = str + "送" + persong + "股";

							}
							if (!TextUtils.isEmpty(prepei)
									&& !"0".equals(prepei)) {
								str = str + "配" + prepei + "股";
							}

							canvas.drawText(str, getWidth() - getTextSpace()
									- 6.5f * DEFAULT_AXIS_TITLE_SIZE,
									2.2f * DEFAULT_AXIS_TITLE_SIZE, textPaint);
						}
					}
				} else {
					int num;
					if(mDataStartIndext+touchNum>=dataList.size()){
						num=dataList.size()-1;
					}else{
						num=mDataStartIndext + touchNum;
					}
					String quanxi = dataList.get(num)
							.getQuanxi();
					String perhongli = dataList
							.get(num).getPerhongli();
					String persong = dataList.get(num)
							.getPersong();
					String prepei = dataList.get(num)
							.getPrepei();

					if (touchX > getWidth() / 2.0f) {
						if (!TextUtils.isEmpty(quanxi) && "1".equals(quanxi)) {
							canvas.drawRect(getTextSpace(),
									1.2f * DEFAULT_AXIS_TITLE_SIZE,
									getTextSpace() + 4
											* DEFAULT_AXIS_TITLE_SIZE,
									2.5f * DEFAULT_AXIS_TITLE_SIZE, areaPaint);
							String str = "10";
							if (!TextUtils.isEmpty(perhongli)
									&& !"0".equals(perhongli)) {
								str = str + "派" + perhongli + "元";
							}
							if (!TextUtils.isEmpty(persong)
									&& !"0".equals(persong)) {
								str = str + "送" + persong + "股";

							}
							if (!TextUtils.isEmpty(prepei)
									&& !"0".equals(prepei)) {
								str = str + "配" + prepei + "股";
							}
							canvas.drawText(str, getTextSpace() + 2,
									2 * DEFAULT_AXIS_TITLE_SIZE, textPaint);
						}
					} else {
						if (!TextUtils.isEmpty(quanxi) && "1".equals(quanxi)) {
							canvas.drawRect(
									getWidth() - getTextSpace(),
									1.2f * DEFAULT_AXIS_TITLE_SIZE,
									getWidth()
											- (getTextSpace() + 4 * DEFAULT_AXIS_TITLE_SIZE),
									2.5f * DEFAULT_AXIS_TITLE_SIZE, areaPaint);

							String str = "10";
							if (!TextUtils.isEmpty(perhongli)
									&& !"0".equals(perhongli)) {
								str = str + "派" + perhongli + "元";
							}
							if (!TextUtils.isEmpty(persong)
									&& !"0".equals(persong)) {
								str = str + "送" + persong + "股";

							}
							if (!TextUtils.isEmpty(prepei)
									&& !"0".equals(prepei)) {
								str = str + "配" + prepei + "股";
							}

							canvas.drawText(str, getWidth() - 2
									* getTextSpace(),
									2.5f * DEFAULT_AXIS_TITLE_SIZE, textPaint);
						}
					}
				}

			}

		}
		float left = 3.0f;
		float top = 3.0f;
		float right = width / 2;
		float bottom = 10.0f + DEFAULT_AXIS_TITLE_SIZE;
		if (touchX < width / 2.0f) {
			right = width - 1.0f;
			left = width - 1.0f - width / 2;
		}
		textPaint.setColor(getResources().getColor(R.color.k_big_text));
		textPaint.setTextSize(DEFAULT_AXIS_TITLE_SIZE);
		double ma5 = 0;
		double ma10 = 0;
		double ma20 = 0;
		if (touchX > getWidth() / 2.0f || !showDetails) {
			mPaint.setColor(Color.WHITE);
			mPaint.setStyle(Style.FILL);
			mPaint.setAlpha(0);
			canvas.drawRect(left, top, right, bottom, mPaint);
			mPaint.setColor(getResources().getColor(R.color.k_lin5));
			canvas.drawCircle(getTextSpace() + 10,
					2.0f + DEFAULT_AXIS_TITLE_SIZE / 2, 5, mPaint);
			canvas.drawText("MA5", getTextSpace() + 20,
					10.0f + DEFAULT_AXIS_TITLE_SIZE / 2, textPaint);
			mPaint.setColor(getResources().getColor(R.color.k_lin10));
			canvas.drawCircle(getTextSpace() + (width / 2) / 3,
					2.0f + DEFAULT_AXIS_TITLE_SIZE / 2, 5, mPaint);
			canvas.drawText("MA10", getTextSpace() + 10 + (width / 2) / 3,
					10.0f + DEFAULT_AXIS_TITLE_SIZE / 2, textPaint);
			mPaint.setColor(getResources().getColor(R.color.k_lin20));
			canvas.drawCircle(getTextSpace() + (width / 2) * 2 / 3,
					2.0f + DEFAULT_AXIS_TITLE_SIZE / 2, 5, mPaint);
			canvas.drawText("MA20", getTextSpace() + 10 + (width / 2) * 2 / 3,
					10.0f + DEFAULT_AXIS_TITLE_SIZE / 2, textPaint);

			/**
			 * 互动画ma值
			 */
			if (dataList != null && dataList.size() != 0) {
				if (!isSmall) {
					if (!showDetails) {

						ma5 = Double.parseDouble(dataList.get(mDataStartIndext)
								.getLine5());
						ma10 = Double.parseDouble(dataList
								.get(mDataStartIndext).getLine10());
						ma20 = Double.parseDouble(dataList
								.get(mDataStartIndext).getLine20());
					} else {

						ma5 = Double.parseDouble(dataList.get(
								mShowDataNum - touchNum + mDataStartIndext - 1)
								.getLine5());
						ma10 = Double.parseDouble(dataList.get(
								mShowDataNum - touchNum + mDataStartIndext - 1)
								.getLine10());
						ma20 = Double.parseDouble(dataList.get(
								mShowDataNum - touchNum + mDataStartIndext - 1)
								.getLine20());

					}

				} else if (isSmall) {
					if (!showDetails || touchNum >= dataList.size()) {
						ma5 = Double.parseDouble(dataList.get(
								dataList.size() - 1).getLine5());
						ma10 = Double.parseDouble(dataList.get(
								dataList.size() - 1).getLine10());
						ma20 = Double.parseDouble(dataList.get(
								dataList.size() - 1).getLine20());
					} else {
						ma5 = Double.parseDouble(dataList.get(
								touchNum + mDataStartIndext).getLine5());
						ma10 = Double.parseDouble(dataList.get(
								touchNum + mDataStartIndext).getLine10());
						ma20 = Double.parseDouble(dataList.get(
								touchNum + mDataStartIndext).getLine20());
					}
				}

				canvas.drawText(decimalFormat.format(ma5), getTextSpace() + 12
						+ 3 * DEFAULT_AXIS_TITLE_SIZE,
						10.0f + DEFAULT_AXIS_TITLE_SIZE / 2, textPaint);
				canvas.drawText(decimalFormat.format(ma10), getTextSpace() + 12
						+ (width / 2) / 3 + 3 * DEFAULT_AXIS_TITLE_SIZE,
						10.0f + DEFAULT_AXIS_TITLE_SIZE / 2, textPaint);
				canvas.drawText(decimalFormat.format(ma20), getTextSpace() + 12
						+ (width / 2) * 2 / 3 + 3 * DEFAULT_AXIS_TITLE_SIZE,
						10.0f + DEFAULT_AXIS_TITLE_SIZE / 2, textPaint);
			}

		} else if (touchX <= getWidth() / 2.0f && showDetails) {
			mPaint.setColor(Color.WHITE);
			mPaint.setStyle(Style.FILL);
			mPaint.setAlpha(0);
			canvas.drawRect(left, top, right, bottom, mPaint);
			mPaint.setColor(getResources().getColor(R.color.k_lin20));
			canvas.drawCircle(getWidth() - getTextSpace() - width / 6 + 5,
					2.0f + DEFAULT_AXIS_TITLE_SIZE / 2, 5, mPaint);
			mPaint.setColor(getResources().getColor(R.color.k_lin10));
			canvas.drawCircle(getWidth() - getTextSpace() - width / 3 + 5,
					2.0f + DEFAULT_AXIS_TITLE_SIZE / 2, 5, mPaint);
			mPaint.setColor(getResources().getColor(R.color.k_lin5));
			canvas.drawCircle(getWidth() - getTextSpace() - width / 2 + 5,
					2.0f + DEFAULT_AXIS_TITLE_SIZE / 2, 5, mPaint);
			canvas.drawText("MA20", getWidth() - getTextSpace() - width / 6
					+ 15, 10.0f + DEFAULT_AXIS_TITLE_SIZE / 2, textPaint);
			canvas.drawText("MA10", getWidth() - getTextSpace() - width / 3
					+ 15, 10.0f + DEFAULT_AXIS_TITLE_SIZE / 2, textPaint);
			canvas.drawText("MA5",
					getWidth() - getTextSpace() - width / 2 + 15,
					10.0f + DEFAULT_AXIS_TITLE_SIZE / 2, textPaint);

			if (!isSmall) {
				ma5 = Double.parseDouble(dataList.get(
						mShowDataNum - touchNum + mDataStartIndext - 1)
						.getLine5());
				ma10 = Double.parseDouble(dataList.get(
						mShowDataNum - touchNum + mDataStartIndext - 1)
						.getLine10());
				ma20 = Double.parseDouble(dataList.get(
						mShowDataNum - touchNum + mDataStartIndext - 1)
						.getLine20());
			} else {
				if (showDetails && touchNum < dataList.size()) {
					ma5 = Double.parseDouble(dataList.get(
							touchNum + mDataStartIndext).getLine5());
					ma10 = Double.parseDouble(dataList.get(
							touchNum + mDataStartIndext).getLine10());
					ma20 = Double.parseDouble(dataList.get(
							touchNum + mDataStartIndext).getLine20());
				} else {
					ma5 = Double.parseDouble(dataList.get(dataList.size() - 1)
							.getLine5());
					ma10 = Double.parseDouble(dataList.get(dataList.size() - 1)
							.getLine10());
					ma20 = Double.parseDouble(dataList.get(dataList.size() - 1)
							.getLine20());
				}
			}

			canvas.drawText(decimalFormat.format(ma5), getWidth()
					- getTextSpace() - width / 2 + 17 + 3
					* DEFAULT_AXIS_TITLE_SIZE,
					10.0f + DEFAULT_AXIS_TITLE_SIZE / 2, textPaint);
			canvas.drawText(decimalFormat.format(ma10), getWidth()
					- getTextSpace() - width / 3 + 17 + 3
					* DEFAULT_AXIS_TITLE_SIZE,
					10.0f + DEFAULT_AXIS_TITLE_SIZE / 2, textPaint);
			canvas.drawText(decimalFormat.format(ma20), getWidth()
					- getTextSpace() - width / 6 + 17 + 3
					* DEFAULT_AXIS_TITLE_SIZE,
					10.0f + DEFAULT_AXIS_TITLE_SIZE / 2, textPaint);
		}
	}

	/**
	 * +++++++++++++++++++++++++++++==滑动的一系列事件+++++++++++++++++++++++++++
	 */
	private double nLenStart = 0;
	private float pointx;
	private float pointy;
	private boolean dFlag = false;
	private float touchX;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int nCnt = event.getPointerCount();
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		// 设置触摸模式
		case MotionEvent.ACTION_DOWN:
			TOUCH_MODE = DOWN;
			mStartX = event.getRawX();
			mStartY = event.getRawY();
			dFlag = false;
			if (!dFlag) {
				pointx = event.getRawX();
				pointy = event.getRawY();
				dFlag = true;
			}
			touchX = event.getRawX();
			showDetails = false;
			isReleased = false;
			isMoved = false;
			postDelayed(mLongPressRunnable, 500);// 按下 1.5秒后调用线程

			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			TOUCH_MODE = DOWN;
			mStartX = event.getRawX();
			mStartY = event.getRawY();

			if (nCnt == 2) {
				for (int i = 0; i < nCnt; i++) {
					float x = event.getX(i);
					float y = event.getY(i);

					Point pt = new Point((int) x, (int) y);

				}

				int xlen = Math.abs((int) event.getX(0) - (int) event.getX(1));
				int ylen = Math.abs((int) event.getY(0) - (int) event.getY(1));

				nLenStart = Math.sqrt((double) xlen * xlen + (double) ylen
						* ylen);

			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			isReleased = true;// 释放了
			dFlag = false;
			if (showDetails) {
				showDetails = false;
				postInvalidate();
			}
			nLenStart = 0;
			break;
		case MotionEvent.ACTION_CANCEL:
			nLenStart = 0;
			if (showDetails) {
				showDetails = false;
				postInvalidate();
			}
			TOUCH_MODE = NONE;
			break;
		case MotionEvent.ACTION_MOVE:
			if (dataList == null || dataList.size() <= 0) {
				return true;
			}
			float lastx = event.getRawX();
			float lasty = event.getRawY();
			if (Math.abs(lastx - pointx) < 30.0f
					&& Math.abs(lasty - pointy) < 30.0f) {
				isMoved = false;
			} else {
				isMoved = true;
			}

			if (showDetails) {
				touchX = event.getRawX();
				postInvalidate();
			} else {

				if (TOUCH_MODE == MOVE && nCnt == 1) {
					float horizontalSpacing = event.getRawX() - mStartX;
					if (Math.abs(horizontalSpacing) < MIN_MOVE_DISTANCE) {
						return true;
					}
					mStartX = event.getRawX();
					mStartY = event.getRawY();
					if (isSmall) {
						if (horizontalSpacing < 0) {
							mDataStartIndext++;
							if ((mDataStartIndext == dataList.size()
									- mShowDataNum + 1)
									&& flag) {

							}

						} else if (horizontalSpacing > 0) {
							mDataStartIndext--;
							if (mDataStartIndext < 0 && flag) {
								mDataStartIndext = 0;
								flag = false;
								Toast.makeText(getContext(), "没有更多数据", 0)
										.show();
							}
						}
					} else {
						if (horizontalSpacing < 0) {
							int moved = (int) (horizontalSpacing / 4);
							mDataStartIndext = mDataStartIndext + moved;
							if (mDataStartIndext < 0) {
								mDataStartIndext = 0;
							}
						} else if (horizontalSpacing > 0) {
							int moved = (int) (horizontalSpacing / 4);
							mDataStartIndext = mDataStartIndext + moved;
							if (flag) {
								if ((mDataStartIndext >= dataList.size()
										- mShowDataNum + 1)) {
									flag = false;
									if (rfreshListener != null) {
										rfreshListener.getNewData();
									}
									Toast.makeText(getContext(), "正在加载", 0)
											.show();
								}
							} else {

							}
						}
					}
					setCurrentData();
					postInvalidate();
				}

				else if (TOUCH_MODE == DOWN && nCnt == 1) {
					setTouchMode(event);
				} else if (nCnt == 2) {
					for (int i = 0; i < nCnt; i++) {
						float x = event.getX(i);
						float y = event.getY(i);

						Point pt = new Point((int) x, (int) y);

					}

					int xlen = Math.abs((int) event.getX(0)
							- (int) event.getX(1));
					int ylen = Math.abs((int) event.getY(0)
							- (int) event.getY(1));

					double nLenEnd = Math.sqrt((double) xlen * xlen
							+ (double) ylen * ylen);

					if (nLenEnd > nLenStart + 15) {
						int zoouBig = (int) (nLenEnd - nLenStart) / 20;
						zoomOut(zoouBig);
					} else if (nLenEnd < nLenStart - 15) {
						int zoouSmall = (int) (nLenStart - nLenEnd) / 20;
						zoomIn(zoouSmall);
					}
					setCurrentData();
					postInvalidate();
				}
			}

			break;

		}
		return true;
	}

	private void setCurrentData() {
		if (mShowDataNum > dataList.size()) {
			// mShowDataNum = dataList.size();
			isSmall = true;
		}
		if (MIN_CANDLE_NUM > dataList.size()) {
			// mShowDataNum = MIN_CANDLE_NUM;
		}

		if (mShowDataNum > dataList.size()) {
			mDataStartIndext = 0;
		} else if (mShowDataNum + mDataStartIndext > dataList.size()) {
			mDataStartIndext = dataList.size() - mShowDataNum;
		}
		if (mDataStartIndext < 0) {
			mDataStartIndext = 0;
		}
		minValue = Float.parseFloat(dataList.get(mDataStartIndext).getLow());
		maxValue = Float.parseFloat(dataList.get(mDataStartIndext).getHigh());
		for (int i = mDataStartIndext + 1; i < dataList.size()
				&& i < mShowDataNum + mDataStartIndext; i++) {
			DayKModel entity = dataList.get(i);
			minValue = minValue < Float.parseFloat(entity.getLow()) ? minValue
					: Float.parseFloat(entity.getLow());
			maxValue = maxValue > Float.parseFloat(entity.getHigh()) ? maxValue
					: Float.parseFloat(entity.getHigh());
		}

		offset = maxValue - minValue;
		maxVolume = getMaxVolume();
		postInvalidate();

	}

	private void zoomIn(int big) {
		mShowDataNum = mShowDataNum + big;
		if (mShowDataNum > dataList.size()) {
			if (!isSmall) {
				mShowDataNum = MIN_CANDLE_NUM > dataList.size() ? MIN_CANDLE_NUM
						: dataList.size();
			}
		}

	}

	private void zoomOut(int small) {
		mShowDataNum = mShowDataNum - small;
		if (mShowDataNum < MIN_CANDLE_NUM) {
			mShowDataNum = MIN_CANDLE_NUM;
		}

	}

	private void setTouchMode(MotionEvent event) {
		float daltX = Math.abs(event.getRawX() - mStartX);
		float daltY = Math.abs(event.getRawY() - mStartY);
		if (FloatMath.sqrt(daltX * daltX + daltY * daltY) > MIN_MOVE_DISTANCE) {
			if (daltX < daltY) {
				TOUCH_MODE = ZOOM;
			} else {
				TOUCH_MODE = MOVE;
			}
			mStartX = event.getRawX();
			mStartY = event.getRawY();
		}
	}

	/**
	 * ++++++++++++++++++++++++++数据处理++++++++++++++++++++++++++++++++++
	 */

	private float getMaxVolume() {
		if (dataList != null && dataList.size() > 0) {
			float max = (float) Double.parseDouble(dataList.get(
					mDataStartIndext).getVolume());
			float nextMax = 0f;
			for (int i = mDataStartIndext + 1; i < dataList.size()
					&& i < mShowDataNum + mDataStartIndext; i++) {
				if (dataList.get(i) != null && !dataList.get(i).equals("")) {
					if (dataList.get(i).getVolume() != null
							&& !dataList.get(i).getAmount().equals("")) {
						nextMax = (float) Double.parseDouble(dataList.get(i)
								.getVolume());
					}
					max = Math.max(max, nextMax);
				}
			}

			return max;
		}

		return 0;

	}

	public void setData(List<DayKModel> list) {
		dataList = list;
		setCurrentData();
	}

	private int from = 1;

	public void setFrom(int from) {
		this.from = from;
	}

	/**
	 * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 * +++++++++++++++
	 */

	/**
	 * 返回外部数据
	 */

	/**
	 * 获取滑动数据
	 * 
	 * @return
	 */
	public DayKModel getTouchData() {
		if(dataList==null||dataList.size()==0){
			return null;
		}
		if (isSmall) {
			if (touchNum + mDataStartIndext < dataList.size()) {
				return dataList.get(touchNum + mDataStartIndext);
			} else {
				return dataList.get(dataList.size() - 1);
			}
		} else {
			if (touchNum + mDataStartIndext < dataList.size()) {
				return dataList.get(dataList.size() - touchNum - 1
						- (dataList.size() - mShowDataNum - mDataStartIndext));
			} else {
				return dataList.get(dataList.size() - 1);
			}
		}
	}

	public boolean getIsShow() {
		return showDetails;
	}

	/**
	 * 刷新接口
	 * 
	 * @author xiongfeng
	 * 
	 */
	public interface MyDataListener extends EventListener {
		public void getNewData();
	}

	/**
	 * 滑动监听接口
	 * 
	 * @author xiongfeng
	 * 
	 */
	public interface DayMoveListener extends EventListener {
		public void doMove();
	}

	/**
	 * 设置是否可以继续加载
	 */
	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public void setFlagSwitch(boolean flagSwitch) {
		this.flagSwitch = flagSwitch;
	}
	
	public void setShowNum(int mShowDataNum){
		this.mShowDataNum=mShowDataNum;
	}

}
