package com.treasure.dreamstock.weight.bigkline;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import com.treasure.dreamstock.R;
import com.treasure.dreamstock.weight.kline.FenshiModel;

import android.R.color;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

/**
 * 分时k线
 * 
 * @author xiongfeng
 * 
 */
public class MyFenSHiView extends GridChart {

	private Paint mPaint, mPaint2, mPaint3, textPaint, areaPaint;
	private float uperBottom;// 上表底部
	private float uperHeight;// 上表高度
	private float lowerBottom;// 下表底部
	private float lowerHeight;// 下表高度
	private float dataSpacing;// 间距
	private final int DATA_MAX_COUNT = 60 * 4;

	private double initialWeightedIndex;
	private float uperHalfHigh;
	private float lowerHigh;
	private float uperRate;
	private float lowerRate;
	private int width;

	private boolean showDetails;
	private float touchX;
	private float weight = 1.0f;
	private float small = 0.8f;
	private boolean isRefresh = false;
	private int prePointX = 0;
	private int prePointY = 0;

	// 均价集合
	private List<Float> evenPriceList = new ArrayList<Float>();
	// 交易量集合
	private List<Long> volumeList = new ArrayList<Long>();
	// 当前价格集合
	private List<Float> dataList = new ArrayList<Float>();
	// 时间集合
	private List<String> timeList = new ArrayList<String>();
	private FenshiModel data;
	private float lastClose = 0.0f;
	private float maxValus;
	private float maxNum;
	private float minValue;
	private float maxVValus;
	private DecimalFormat decimalFormat;
	private int dataOrder;
	private FenMoveListener listener;
	/**
	 * 波纹效果所需要的参数
	 */
	private boolean isRuning = false;
	private boolean flag = true;
	private int pointX;
	private int pointY;
	private int[] colors = new int[] { getResources().getColor(R.color.kline),
			getResources().getColor(R.color.kline),
			getResources().getColor(R.color.kline) };
	List<Wave> waveList = new ArrayList<Wave>();

	public MyFenSHiView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public MyFenSHiView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MyFenSHiView(Context context) {
		super(context);
		init();
	}

	public void setMoveListener(FenMoveListener listener) {
		this.listener = listener;
	}

	/**
	 * view创建时初始化变量
	 */
	private void init() {
		super.setDefaultLineUp(1);
		super.setDefaultLineDown(0);
		super.setLongiLatitudeColor(getResources().getColor(R.color.k_bg_line));
		super.setBorderColor(getResources().getColor(R.color.k_bg_line));
		super.setAxisColor(getResources().getColor(R.color.k_bg_line));
		super.setShowLowerChartTabs(false);
		super.setShowTopTitles(false);
		super.setTextSpace(DEFAULT_AXIS_TITLE_SIZE * 4);

		uperBottom = 0;
		uperHeight = 0;
		lowerBottom = 0;
		lowerHeight = 0;
		dataSpacing = 0;

		initialWeightedIndex = 0;
		uperHalfHigh = 0;
		lowerHigh = 0;
		uperRate = 0;
		lowerRate = 0;
		showDetails = false;
		touchX = 0;
		data = null;
		decimalFormat = new DecimalFormat("0.00");

		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		areaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		super.setLogitudeNum(3);
		if (data == null) {
			return;
		}
		getLastClose();
		maxNum = getMaxNum() * weight;
		maxValus = lastClose + maxNum;
		minValue = lastClose - maxNum;
		maxVValus = getVMaxValue();

		width = (int) (getWidth() - 2 * getTextSpace());
		uperBottom = UPER_CHART_BOTTOM - 2;
		uperHeight = getUperChartHeight();
		lowerBottom = getHeight() - 3;
		lowerHeight = getLowerChartHeight() - 2;
		dataSpacing = (getWidth() - 2 * getTextSpace()) * 10.0f / 10.0f
				/ DATA_MAX_COUNT;

		if (uperHalfHigh > 0) {
			uperRate = uperHeight / uperHalfHigh / 2.0f;
		}
		if (lowerHigh > 0) {
			lowerRate = lowerHeight / lowerHigh;
		}

		// 绘制分时线
		drawFenLine(canvas);

		// 绘制均线
		drawEveLines(canvas);

		// 绘制底部柱状条
		drawColum(canvas);

		// 绘制坐标标题
		drawXYText(canvas);

		// 画点击效果
		drawPointLine(canvas);

		// 画最后一个点的闪烁效果
		drawWave(canvas);

	}

	/**
	 * 绘制坐标标题
	 * 
	 * @param canvas
	 */
	private void drawXYText(Canvas canvas) {
		textPaint.setColor(getResources().getColor(R.color.k_big_text));
		textPaint.setTextSize(DEFAULT_AXIS_TITLE_SIZE);

		canvas.drawText(decimalFormat.format(lastClose), 0, uperHeight / 2,
				textPaint);
		canvas.drawText("0.00%", getWidth() - getTextSpace() + 2,
				uperHeight / 2, textPaint);
		canvas.drawText("万手", 0, getHeight() - DEFAULT_AXIS_TITLE_SIZE / 2,
				textPaint);
		canvas.drawText(decimalFormat.format(maxVValus / 10000), 0, getHeight()
				- lowerHeight + DEFAULT_AXIS_TITLE_SIZE / 2, textPaint);

		canvas.drawText("09:30", 2 + getTextSpace(), uperBottom
				+ DEFAULT_AXIS_TITLE_SIZE, textPaint);
		canvas.drawText("11:30/13:00", getWidth() / 2.0f
				- DEFAULT_AXIS_TITLE_SIZE * 2.5f, uperBottom
				+ DEFAULT_AXIS_TITLE_SIZE, textPaint);
		canvas.drawText("15:00", getWidth() - 2 - DEFAULT_AXIS_TITLE_SIZE
				* 2.5f - getTextSpace(), uperBottom + DEFAULT_AXIS_TITLE_SIZE,
				textPaint);
		canvas.drawText("10:30", 2 + getTextSpace() + width / 4
				- DEFAULT_AXIS_TITLE_SIZE * 1.25f, uperBottom
				+ DEFAULT_AXIS_TITLE_SIZE, textPaint);
		canvas.drawText("14:00", 2 + getTextSpace() + width * 3 / 4
				- DEFAULT_AXIS_TITLE_SIZE * 1.25f, uperBottom
				+ DEFAULT_AXIS_TITLE_SIZE, textPaint);
		textPaint.setColor(getResources().getColor(R.color.font_up));
		if (maxValus == minValue && maxNum == 0) {
			canvas.drawText(decimalFormat.format(maxValus * 1.1f), 0,
					DEFAULT_AXIS_TITLE_SIZE, textPaint);
			canvas.drawText("+10.00%", getWidth() - getTextSpace() + 2,
					DEFAULT_AXIS_TITLE_SIZE, textPaint);

			textPaint.setColor(getResources().getColor(R.color.font_green));
			canvas.drawText(decimalFormat.format(minValue * 0.9f), 0,
					uperHeight, textPaint);
			canvas.drawText("-10.00%", getWidth() - getTextSpace() + 2,
					uperHeight, textPaint);
		} else {
			canvas.drawText(decimalFormat.format(maxValus), 0,
					DEFAULT_AXIS_TITLE_SIZE, textPaint);
			canvas.drawText(
					"+" + decimalFormat.format(maxNum / lastClose * 100) + "%",
					getWidth() - getTextSpace() + 2, DEFAULT_AXIS_TITLE_SIZE,
					textPaint);

			textPaint.setColor(getResources().getColor(R.color.font_green));
			canvas.drawText(decimalFormat.format(minValue), 0, uperHeight,
					textPaint);
			canvas.drawText(
					"-" + decimalFormat.format(maxNum / lastClose * 100) + "%",
					getWidth() - getTextSpace() + 2, uperHeight, textPaint);
		}

	}

	/**
	 * 画最后一个点的闪烁效果
	 * 
	 * @param canvas
	 */
	private void drawWave(Canvas canvas) {
		for (Wave wave : waveList) {
			canvas.drawCircle(wave.cx, wave.cy, wave.cr, wave.cp);
		}

	}

	/**
	 * 绘制分时线
	 * 
	 * @param canvas
	 */
	private void drawFenLine(Canvas canvas) {
		float fisty = 0;
		float evenWidth = (float) width / DATA_MAX_COUNT;
		mPaint.setColor(getResources().getColor(R.color.kline));
		mPaint.setStyle(Style.FILL);
		mPaint.setAlpha(15);
		mPaint.setStrokeWidth(2);
		mPaint2.setStrokeWidth(2.5f);
		mPaint2.setStyle(Style.STROKE);
		mPaint2.setColor(getResources().getColor(R.color.kline));
		Path path = new Path();
		Path path2 = new Path();
		float y = 0f;
		path.moveTo(getTextSpace(), uperHeight);
		if (dataList.size() != 0) {
			if (dataList.get(0) - lastClose == 0) {
				fisty = uperHeight / 2;
			} else if (dataList.get(0) - lastClose > 0) {
				fisty = uperHeight
						/ 2
						- (Math.abs(dataList.get(0) - lastClose) / (maxNum * 2))
						* uperHeight + 4;
			} else {
				fisty = uperHeight
						/ 2
						+ (Math.abs(dataList.get(0) - lastClose) / (maxNum * 2))
						* uperHeight;
			}
		}
		path2.moveTo(getTextSpace(), fisty);
		for (int i = 1; i < dataList.size(); i++) {
			float startY = 0.0f, stopY = 0.0f;
			if ((dataList.get(i) - lastClose) >= 0) {
				if (dataList.get(i) - lastClose == 0) {
					startY = uperHeight / 2;
				} else {
					startY = uperHeight
							/ 2
							- (Math.abs(dataList.get(i) - lastClose) / (maxNum * 2))
							* uperHeight + 4;
				}
			} else {
				startY = uperHeight
						/ 2
						+ (Math.abs(dataList.get(i) - lastClose) / (maxNum * 2))
						* uperHeight;
			}
			path.lineTo(i * evenWidth + getTextSpace(), startY);
			path2.lineTo(i * evenWidth + getTextSpace(), startY);

			if (i == dataList.size() - 1) {
				mPaint3.setColor(getResources().getColor(R.color.kline));
				mPaint3.setStyle(Style.FILL);
				if (!"1500".equals(timeList.get(timeList.size() - 1))) {
					canvas.drawCircle((i * evenWidth + getTextSpace()), startY,
							8, mPaint3);
				}
				if (isRefresh) {
					// flag = false;
					isRefresh = false;
					pointX = (int) (i * evenWidth + getTextSpace());
					pointY = (int) (startY);
					if (prePointX == 0 && prePointY == 0) {
						prePointX = pointX;
						prePointY = pointY;
						waveList.add(new Wave(pointX, pointY));
						if (!"1500".equals(timeList.get(timeList.size() - 1))) {
							startAnimation();
						}
					}
					if (prePointX != pointX && prePointY != pointY) {
						prePointX = pointX;
						prePointY = pointY;
						if (waveList != null) {
							waveList.clear();
						}
						waveList.add(new Wave(pointX, pointY));
						// startAnimation();
					}
				}
			}

		}
		path.lineTo(evenWidth * (dataList.size() - 1) + getTextSpace(),
				uperHeight);

		path.close();
		canvas.drawPath(path, mPaint);
		canvas.drawPath(path2, mPaint2);

	}

	/**
	 * 绘制均线
	 * 
	 * @param canvas
	 */
	private void drawEveLines(Canvas canvas) {
		getEvenPrice();
		if (evenPriceList != null && evenPriceList.size() > 0) {
			float evenWidth = (float) width / DATA_MAX_COUNT;
			mPaint.setColor(getResources().getColor(R.color.k_lin10));
			mPaint.setStrokeWidth(2.5f);

			if (evenPriceList != null && evenPriceList.size() > 0) {
				for (int i = 0; i < evenPriceList.size(); i++) {
					if (i + 1 >= evenPriceList.size()) {
					} else {

						float evenPrice = evenPriceList.get(i);
						float nextPrice = evenPriceList.get(i + 1);
						canvas.drawLine(evenWidth * i + getTextSpace(),
								((maxValus - evenPrice) / (maxNum * 2))
										* uperHeight, evenWidth * (i + 1)
										+ getTextSpace(),
								((maxValus - nextPrice) / (maxNum * 2))
										* uperHeight, mPaint);
					}

				}
			}
		}

	}

	/**
	 * 画底部柱子
	 * 
	 */
	private void drawColum(Canvas canvas) {
		if (volumeList != null && volumeList.size() > 0) {
			mPaint.setStrokeWidth(dataSpacing * 0.9f);
			mPaint.setColor(Color.RED);
			// addHeight=width/30;

			for (int i = 0; i < dataList.size() - 1; i++) {

				float price = dataList.get(i);
				float prePrice = 0;
				if (i > 0) {
					prePrice = dataList.get(i - 1);
				}
				float volum = volumeList.get(i);
				float pos = 3 + dataSpacing * i + getTextSpace();
				if (i == 0) {
					if (price >= lastClose) {
						mPaint.setColor(getResources()
								.getColor(R.color.font_up));
					} else {
						mPaint.setColor(getResources().getColor(
								R.color.font_green));
					}
				} else {
					if (price >= prePrice) {
						mPaint.setColor(getResources()
								.getColor(R.color.font_up));
					} else {
						mPaint.setColor(getResources().getColor(
								R.color.font_green));
					}
				}
				canvas.drawLine(pos, lowerBottom
						- (volumeList.get(i) / (maxVValus * weight))
						* lowerHeight * 0.9f, pos, lowerBottom, mPaint);

			}
		}

	}

	/**
	 * 绘制点击效果
	 */
	private void drawPointLine(Canvas canvas) {
		if (listener != null) {
			listener.doMove();
		}
		if (showDetails) {
			textPaint.setColor(getResources().getColor(R.color.white));
			textPaint.setTextSize(DEFAULT_AXIS_TITLE_SIZE);
			areaPaint.setColor(getResources().getColor(R.color.k_point_line));
			Paint paint = new Paint();
			paint.setStrokeWidth(2);
			paint.setColor(getResources().getColor(R.color.k_point_line));
			int i = (int) (((touchX - getTextSpace()) / width) * DATA_MAX_COUNT);
			if (i <= 0) {
				canvas.drawLine(getTextSpace(), 2.0f, getTextSpace(),
						UPER_CHART_BOTTOM, paint);
				canvas.drawLine(getTextSpace(), lowerBottom - lowerHeight,
						getTextSpace(), lowerBottom, paint);
			} else if (i > 0 && i < dataList.size()) {
				canvas.drawLine(touchX, 2.0f, touchX, UPER_CHART_BOTTOM, paint);
				canvas.drawLine(touchX, lowerBottom - lowerHeight, touchX,
						lowerBottom, paint);
			} else if (i > dataList.size()) {
				if (dataList.size() * 1.0f / DATA_MAX_COUNT * 1.0f * width
						+ getTextSpace() > width + getTextSpace()) {
					canvas.drawLine(getTextSpace() + width, 2.0f,
							getTextSpace() + width, UPER_CHART_BOTTOM, paint);
					canvas.drawLine(getTextSpace() + width, lowerBottom
							- lowerHeight, getTextSpace() + width, lowerBottom,
							paint);
				} else {
					canvas.drawLine(dataList.size() * 1.0f / DATA_MAX_COUNT
							* 1.0f * width + getTextSpace(), 2.0f,
							dataList.size() * 1.0f / DATA_MAX_COUNT * 1.0f
									* width + getTextSpace(),
							UPER_CHART_BOTTOM, paint);
					canvas.drawLine(dataList.size() * 1.0f / DATA_MAX_COUNT
							* 1.0f * width + getTextSpace(), lowerBottom
							- lowerHeight, dataList.size() * 1.0f
							/ DATA_MAX_COUNT * 1.0f * width + getTextSpace(),
							lowerBottom, paint);
				}
			}
			float ly = 0;
			if (i >= dataList.size()) {
				dataOrder = dataList.size() - 1;
				if (dataList.get(dataList.size() - 1) > lastClose) {
					ly = uperHeight
							/ 2
							- (Math.abs(dataList.get(dataList.size() - 1)
									- lastClose) / (maxNum * 2)) * uperHeight
							+ 4;

				} else {
					ly = uperHeight
							/ 2
							+ (Math.abs(dataList.get(dataList.size() - 1)
									- lastClose) / (maxNum * 2)) * uperHeight
							+ 4;
				}

			} else if (i > 0 && i < dataList.size()) {
				dataOrder = i;
				if (dataList.get(i) >= lastClose) {
					ly = uperHeight
							/ 2
							- (Math.abs(dataList.get(i) - lastClose) / (maxNum * 2))
							* uperHeight + 4;
				} else {
					ly = uperHeight
							/ 2
							+ (Math.abs(dataList.get(i) - lastClose) / (maxNum * 2))
							* uperHeight + 4;
				}

			} else if (i <= 0) {
				dataOrder = 0;
				if (dataList.get(0) >= lastClose) {
					ly = uperHeight
							/ 2
							- (Math.abs(dataList.get(0) - lastClose) / (maxNum * 2))
							* uperHeight + 4;
				} else {
					ly = uperHeight
							/ 2
							+ (Math.abs(dataList.get(0) - lastClose) / (maxNum * 2))
							* uperHeight + 4;
				}
			}

			canvas.drawLine(getTextSpace(), ly, width + getTextSpace(), ly,
					paint);
			areaPaint.setStyle(Style.FILL);
			canvas.drawRect(0, ly - DEFAULT_AXIS_TITLE_SIZE / 2 - 2,
					getTextSpace(), ly + DEFAULT_AXIS_TITLE_SIZE / 2 + 4,
					areaPaint);
			canvas.drawRect(getWidth() - getTextSpace(), ly
					- DEFAULT_AXIS_TITLE_SIZE / 2 - 2, getWidth(), ly
					+ DEFAULT_AXIS_TITLE_SIZE / 2 + 4, areaPaint);
			if (i <= 0) {
				canvas.drawText(decimalFormat.format(dataList.get(0)), 2, ly
						+ DEFAULT_AXIS_TITLE_SIZE / 2 - 2, textPaint);
				canvas.drawText(
						decimalFormat.format((dataList.get(0) - lastClose)
								/ lastClose * 100)
								+ "%", getWidth() - getTextSpace() + 2, ly
								+ DEFAULT_AXIS_TITLE_SIZE / 2 - 2, textPaint);
			} else if (i > 0 && i < dataList.size()) {
				canvas.drawText(decimalFormat.format(dataList.get(i)), 2, ly
						+ DEFAULT_AXIS_TITLE_SIZE / 2 - 2, textPaint);
				canvas.drawText(
						decimalFormat.format((dataList.get(i) - lastClose)
								/ lastClose * 100)
								+ "%", getWidth() - getTextSpace() + 2, ly
								+ DEFAULT_AXIS_TITLE_SIZE / 2 - 2, textPaint);
			} else {
				canvas.drawText(
						decimalFormat.format(dataList.get(dataList.size() - 1)),
						2, ly + DEFAULT_AXIS_TITLE_SIZE / 2 - 2, textPaint);
				canvas.drawText(
						decimalFormat.format((dataList.get(dataList.size() - 1) - lastClose)
								/ lastClose * 100)
								+ "%", getWidth() - getTextSpace() + 2, ly
								+ DEFAULT_AXIS_TITLE_SIZE / 2 - 2, textPaint);
			}

		}
	}

	/**
	 * 数据处理
	 * +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 */

	public void setData(FenshiModel model, boolean isRefresh) {
		data = model;
		this.isRefresh = isRefresh;
		DealData();
		
	}

	private void DealData() {
		if (data != null) {
			String price = data.getStockprice();

			String[] everyPrice = price.split(",");
			if (everyPrice != null && everyPrice.length > 0) {
				if (dataList != null && dataList.size() > 0) {
					dataList.clear();
				}

				for (int i = 0; i < everyPrice.length; i++) {
					dataList.add(Float.parseFloat(everyPrice[i]));
				}

			}
			getvolumData();
			setTimeList();
		}

	}

	private float getMaxValue() {
		float max = 0.0f;
		if (dataList != null && dataList.size() > 0) {
			for (int i = 0; i < dataList.size(); i++) {
				float nextPrice = dataList.get(i);
				max = Math.max(max, nextPrice);
			}
		}
		return max;
	}

	/**
	 * 价格最小值
	 * 
	 * @return
	 */
	private float getMinValue() {
		float min = 0.0f;
		if (dataList != null && dataList.size() > 0) {
			for (int i = 0; i < dataList.size(); i++) {
				float nextPrice = dataList.get(i);
				if (i == 0) {
					min = nextPrice;
				}
				min = Math.min(min, nextPrice);
			}
		}
		return min;
	}

	/**
	 * 价格的最大值
	 * 
	 * @return
	 */
	private float getMaxNum() {
		float max = 0.0f;
		if (dataList != null) {
			for (int i = 0; i < dataList.size(); i++) {
				float price = dataList.get(i);
				float off = Math.abs(price - lastClose);
				max = Math.max(off, max);
			}
		}
		return max;
	}

	public FenshiModel getData() {
		return data;
	}

	private void getLastClose() {
		if (data != null) {
			lastClose = Float.parseFloat(data.getStocklastclose());
		}
	}

	public List<Float> getDataList() {
		return dataList;
	}

	private void getEvenPrice() {
		if (data != null) {
			String price = data.getStockaveprice();
			String[] priceArray = price.split(",");
			if (evenPriceList != null && evenPriceList.size() > 0) {
				evenPriceList.clear();
			}
			for (int i = 0; i < priceArray.length; i++) {
				evenPriceList.add(Float.parseFloat(priceArray[i]));
			}
		}
	}

	/**
	 * 获取交易量
	 */
	private void getvolumData() {
		if (data != null) {
			String volum = data.getStockvolume();
			String[] everyVolum = volum.split(",");

			if (volumeList != null && volumeList.size() > 0) {
				volumeList.clear();
			}
			for (int i = 0; i < everyVolum.length; i++) {
				if (i + 1 >= everyVolum.length) {

				} else {
					long prime = Long.parseLong(everyVolum[i]);
					long next = Long.parseLong(everyVolum[i + 1]);
					long chazhi = next - prime;
					if (i == 0) {
						volumeList.add(prime);
					} else {
						volumeList.add(chazhi);
					}
				}

			}
			postInvalidate();
		}
	}

	/**
	 * 交易量的最大值
	 * 
	 * @return
	 */
	private float getVMaxValue() {
		float max = 0.0f;
		if (volumeList != null && volumeList.size() > 0) {
			for (int i = 0; i < volumeList.size(); i++) {
				float nextPrice = volumeList.get(i);
				max = Math.max(max, nextPrice);
			}
		}
		return max;
	}

	/**
	 * 交易量d最小值
	 * 
	 * @return
	 */
	private float getVMinValue() {
		float min = 0.0f;
		if (volumeList != null && volumeList.size() > 0) {
			for (int i = 0; i < volumeList.size(); i++) {
				float nextPrice = volumeList.get(i);
				if (i == 0) {
					min = nextPrice;
				}
				min = Math.min(min, nextPrice);
			}
		}
		return min;
	}

	private void setTimeList() {
		if (data != null) {
			if(timeList!=null){
				timeList.clear();
			}
			String[] split = data.getStocktime().split(",");
			for (int i = 0; i < split.length; i++) {
				timeList.add(split[i]);
			}
		}
	}

	/**
	 * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 * ++++++
	 */
	/**
	 * 滑动事件
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
			touchX = event.getRawX();
			if (touchX < 2 || touchX > getWidth() - 2) {
				return false;
			}
			showDetails = true;
			postInvalidate();
			break;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_OUTSIDE:
			showDetails = false;
			postInvalidate();
			break;

		default:
			break;
		}

		return true;
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			// 改变波纹数据
			for (int i = 0; i < waveList.size(); i++) {

				Wave wave = waveList.get(i);

				// 如果全部透明了就移除这个波纹
				int alpha = wave.cp.getAlpha();
				if (alpha == 0) {
					waveList.remove(wave);
					waveList.add(new Wave(pointX, pointY));
					startAnimation();
					continue;
				}

				// 降低透明度
				alpha = Math.max(0, alpha - 35);
				wave.cp.setAlpha(alpha);

				// 扩大半径
				wave.cr += 4;

				// 画笔增宽
				wave.cp.setStrokeWidth(wave.cr / 3);
			}

			// 如果集合没有内容了停止刷新
			if (waveList.size() == 0) {
				isRuning = false;
			}

			invalidate();

			if (isRuning) {
				handler.sendEmptyMessageDelayed(0, 200);
			}
		}

	};

	class Wave {

		public Wave(int x, int y) {
			this.cx = x;
			this.cy = y;
		}

		int cx;
		int cy;
		int cr = 0;
		Paint cp = getPaint(cr);
	}

	/**
	 * 随机获取画笔
	 * 
	 * @return
	 */
	private Paint getPaint(int pWidth) {

		Paint p = new Paint();
		p.setColor(colors[(int) (Math.random() * 3)]);
		p.setAntiAlias(true);
		p.setAlpha(255);
		p.setStyle(Style.FILL);
		p.setStrokeWidth(pWidth);
		return p;
	}

	/**
	 * 开启动画
	 */
	private void startAnimation() {

		// 如果动画正在执行,就不发消息再次执行
		if (isRuning) {
			return;
		}
		isRuning = true;
		if (!"1500".equals(timeList.get(timeList.size() - 1))) {
			handler.sendEmptyMessage(0);
		}
		Log.i("startAnimation", "startAnimation");
	}

	/**
	 * ++++++++++++++++++++++++++++++++++外部获取内部数据++++++++++++++++++++++++++++++
	 */

	/**
	 * 获取当前是第几个
	 * 
	 * @return
	 */
	public int getDataOrder() {
		return dataOrder;
	}

	/**
	 * 获取当前时间
	 * 
	 * @return
	 */
	public String getTime() {
		if (timeList.size() > 0) {
			if (dataOrder < timeList.size()) {
				return timeList.get(dataOrder);
			}else{
				return timeList.get(timeList.size()-1);
			}
		}
		return "--";
	}

	/**
	 * 获取当前价格
	 * 
	 * @return
	 */
	public float getPrice() {
		if (dataList.size() > 0) {
			return dataList.get(dataOrder);
		}
		return 0.0f;
	}

	/**
	 * 获取当前成交量
	 */
	public long getVolume() {
		if (volumeList.size() > 0) {
			if (dataOrder < volumeList.size()) {
				return volumeList.get(dataOrder);
			} else {
				return volumeList.get(volumeList.size() - 1);
			}
		}
		return 0;
	}

	/**
	 * 获取当前均价
	 */
	public float getEvePrice() {
		if (evenPriceList.size() > 0) {
			return evenPriceList.get(dataOrder);
		}
		return 0;
	}

	/**
	 * 获取昨收价
	 */
	public float getLastPrice() {
		return lastClose;
	}

	/**
	 * 获取是否点击
	 * 
	 * @return
	 */
	public boolean getIsShow() {
		return showDetails;
	}

	/**
	 * 分时内部接口
	 * 
	 * @author xiongfeng
	 * 
	 */
	public interface FenMoveListener extends EventListener {
		public void doMove();
	}

}
