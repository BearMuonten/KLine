package com.treasure.dreamstock.weight.bigkline;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import com.treasure.dreamstock.R;
import com.treasure.dreamstock.utils.UIUtils;
import com.treasure.dreamstock.weight.kline.FiveDayEveryModel;
import com.treasure.dreamstock.weight.kline.FiveDayModel;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 5日k线
 * 
 * @author xiongfeng
 * 
 */
public class MyFiveDayView extends GridChart {

	private Paint mPaint, mPaint2, textPaint, areaPaint;
	private float maxValus, minValue;
	private float maxValus2, minValue2;
	private float offset;
	private float addHeight;
	private int length = 0;
	private float startX, startY, stopX, stopY;
	private float uperBottom;// 上表底部
	private float uperHeight;// 上表高度
	private float lowerBottom;// 下表底部
	private float lowerHeight;// 下表高度
	private float dataSpacing;// 间距
	private int DATA_MAX_COUNT = 49 * 5;

	private double initialWeightedIndex;
	private float uperHalfHigh;
	private float lowerHigh;
	private float uperRate;
	private float lowerRate;
	private int width, height;

	private boolean showDetails;
	private float touchX;
	private float weight = 1.00f;
	private float small = 0.8f;
	private FiveDayModel model;
	private List<String> listTime = new ArrayList<String>();
	private List<Float> allList = new ArrayList<Float>();
	private float lastClose = 0.0f;
	private float maxNum = 0f;

	private List<Long> volumeList = new ArrayList<Long>();
	private List<Float> dataList = new ArrayList<Float>();
	private List<Float> list1 = new ArrayList<Float>(),
			list2 = new ArrayList<Float>(), list3 = new ArrayList<Float>(),
			list4 = new ArrayList<Float>(), list5 = new ArrayList<Float>();
	private List<Double> listZF = new ArrayList<Double>();
	private List<Float> evePriceList = new ArrayList<Float>();
	private FiveMoveListener listener;

	private DecimalFormat decimalFormat;
	private int dataOrder;

	public MyFiveDayView(Context context) {
		super(context);
		init();
	}

	public MyFiveDayView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MyFiveDayView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public void setMoveListener(FiveMoveListener listener) {
		this.listener = listener;
	}

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
		decimalFormat = new DecimalFormat("0.00");

		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		areaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		super.setLogitudeNum(4);
		if (model == null) {
			return;
		}

		getLastClose();
		getFiveDayData();
		getMaxValue();
		getListTime();
		getEvePriceList();

		maxValus = lastClose + maxNum;
		minValue = lastClose - maxNum;

		width = (int) (getWidth() - 2 * getTextSpace());
		height = getHeight();
		uperBottom = UPER_CHART_BOTTOM - 2;
		uperHeight = getUperChartHeight();
		lowerBottom = getHeight() - 3;
		lowerHeight = getLowerChartHeight() - 2;
		dataSpacing = width * 10.0f / 10.0f / DATA_MAX_COUNT;

		if (uperHalfHigh > 0) {
			uperRate = uperHeight / uperHalfHigh / 2.0f;
		}
		if (lowerHigh > 0) {
			lowerRate = lowerHeight / lowerHigh;
		}

		drawFiveLine(canvas);// 画5日k线

		drawYellowLine(canvas);// 画均线

		drawDownColum(canvas);

		drawXYText(canvas);
		drawPointLine(canvas);
	}

	/**
	 * xy轴的标题
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
		canvas.drawText(decimalFormat.format(maxValus2 / 10000), 0, getHeight()
				- lowerHeight + DEFAULT_AXIS_TITLE_SIZE / 2, textPaint);

		if (model != null) {
			if (model.getStockfiveday().size() >= 1) {
				canvas.drawText(
						UIUtils.cutDataString(model.getStockfiveday().get(0)
								.getStockdate()), 2 + getTextSpace() + width
								/ 10, uperBottom + DEFAULT_AXIS_TITLE_SIZE,
						textPaint);
			}
			if (model.getStockfiveday().size() >= 2) {
				canvas.drawText(
						UIUtils.cutDataString(model.getStockfiveday().get(1)
								.getStockdate()), 2 + getTextSpace() + width
								* 3 / 10 - DEFAULT_AXIS_TITLE_SIZE * 1.25f,
						uperBottom + DEFAULT_AXIS_TITLE_SIZE, textPaint);
			}

			if (model.getStockfiveday().size() >= 3) {
				canvas.drawText(
						UIUtils.cutDataString(model.getStockfiveday().get(2)
								.getStockdate()), 2 + getTextSpace() + width
								* 5 / 10 - DEFAULT_AXIS_TITLE_SIZE * 1.25f,
						uperBottom + DEFAULT_AXIS_TITLE_SIZE, textPaint);
			}

			if (model.getStockfiveday().size() >= 4) {
				canvas.drawText(
						UIUtils.cutDataString(model.getStockfiveday().get(3)
								.getStockdate()), 2 + getTextSpace() + width
								* 7 / 10 - DEFAULT_AXIS_TITLE_SIZE * 1.25f,
						uperBottom + DEFAULT_AXIS_TITLE_SIZE, textPaint);
			}
			if (model.getStockfiveday().size() >= 5) {
				canvas.drawText(
						UIUtils.cutDataString(model.getStockfiveday().get(4)
								.getStockdate()), 2 + getTextSpace() + width
								* 9 / 10 - DEFAULT_AXIS_TITLE_SIZE * 1.25f,
						uperBottom + DEFAULT_AXIS_TITLE_SIZE, textPaint);
			}

		}
		if (maxNum == 0 && maxValus == minValue) {
			textPaint.setColor(getResources().getColor(R.color.font_up));
			canvas.drawText(decimalFormat.format(maxValus * 1.1f), 0,
					DEFAULT_AXIS_TITLE_SIZE, textPaint);
			canvas.drawText("+10.00%", getWidth() - getTextSpace() + 2,
					DEFAULT_AXIS_TITLE_SIZE, textPaint);

			textPaint.setColor(getResources().getColor(R.color.font_green));
			canvas.drawText(decimalFormat.format(minValue * 0.9), 0,
					uperHeight, textPaint);
			canvas.drawText("-10.00%", getWidth() - getTextSpace() + 2,
					uperHeight, textPaint);
		} else {
			textPaint.setColor(getResources().getColor(R.color.font_up));
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
	 * 画5日k线
	 * 
	 * @param canvas
	 */
	private void drawFiveLine(Canvas canvas) {
		float fisty = 0;
		float fwordy = 0;

		mPaint.setColor(getResources().getColor(R.color.kline));
		mPaint.setStyle(Style.FILL);
		mPaint.setAlpha(15);
		mPaint2.setColor(getResources().getColor(R.color.kline));
		mPaint2.setStrokeWidth(3f);
		mPaint2.setStyle(Style.STROKE);
		if (model != null) {
			Path path = new Path();
			Path path2 = new Path();
			path.moveTo(getTextSpace(), uperHeight);
			if (allList.size() != 0) {
				if (allList.get(0) - lastClose == 0) {
					fisty = uperHeight / 2;
				} else {
					fisty = (1 - (allList.get(0) - minValue) / (maxNum * 2))
							* uperHeight;
				}
				path2.moveTo(getTextSpace(), fisty);
			}
			float evenWidth = 0;
			if (list1.size() <= 47) {
				evenWidth = (width * 0.2f) / 48.0f;
			} else {
				evenWidth = (width * 0.2f) / list1.size();
			}
			startY = uperHeight / 2;
			for (int i = 0; i < allList.size(); i++) {
				if (maxNum == 0) {
					startY = startY;
				} else {
					startY = (1 - (allList.get(i) - minValue) / (maxNum * 2))
							* uperHeight;
				}
				if (i > 0) {
					if (listTime.size() > 0) {
						if (listTime.get(i).equals("0930")) {
							path2.moveTo(evenWidth * i + getTextSpace(), startY);
						} else {
							path2.lineTo(evenWidth * i + getTextSpace(), startY);
						}
					}
				}

				path.lineTo(evenWidth * i + getTextSpace(), startY);

			}

			path.lineTo(evenWidth * allList.size() + getTextSpace(), uperHeight);
			path.lineTo(getTextSpace(), uperHeight);
			canvas.drawPath(path, mPaint);
			canvas.drawPath(path2, mPaint2);

		}

	}

	/**
	 * 画5日均线
	 * 
	 * @param canvas
	 */
	private void drawYellowLine(Canvas canvas) {
		mPaint.setStrokeWidth(1.5f);
		mPaint.setColor(getResources().getColor(R.color.k_lin10));
		if (model != null) {
			float evenWidth = 0;
			if (list1.size() <= 47) {
				evenWidth = (width * 0.2f) / 48.0f;
			} else {
				evenWidth = (width * 0.2f) / list1.size();
			}
			List<FiveDayEveryModel> five_day = model.getStockfiveday();
			for (int j = 0; j < five_day.size(); ++j) {
				String res_five = five_day.get(j).getStockaveprice();
				String flo_five[] = res_five.split(",");

				for (int i = 0; i < flo_five.length - 1; ++i) {
					float y1 = Float.parseFloat(flo_five[i]);
					float y2 = Float.parseFloat(flo_five[i + 1]);

					float yy1 = (1 - (y1 - minValue) / (maxNum * 2))
							* uperHeight;
					float yy2 = (1 - (y2 - minValue) / (maxNum * 2))
							* uperHeight;
					float x1 = ((float) (evenWidth * i + getTextSpace()) + (j
							* 1.0f * width / 5));
					float x2 = ((float) (evenWidth * (i + 1) + getTextSpace()) + (j
							* 1.0f * width / 5));

					canvas.drawLine(x1, yy1, x2, yy2, mPaint);
				}
			}

		}

	}

	/**
	 * 画底部条条
	 * 
	 * @param canvas
	 */
	private void drawDownColum(Canvas canvas) {

		if (volumeList != null && volumeList.size() > 0) {
			mPaint.setStrokeWidth(dataSpacing * 0.9f);
			addHeight = lowerHeight / 20;

			for (int i = 0; i < allList.size(); i++) {

				float price = allList.get(i);
				float price2 = 0;
				float bilv=0;
				if (i > 0) {
					price2 = allList.get(i - 1);
				}
				float volum = 0;
				if (i < volumeList.size()) {
					volum = volumeList.get(i);
				}
				float pos = 3 + dataSpacing * i + getTextSpace();
				if (i < volumeList.size()) {
					bilv= (volumeList.get(i) - minValue) / offset;
				}
				float y1 = (1 - bilv) * lowerHeight * 0.9f - addHeight;
				if (i == 0) {
					if (listZF.get(0) >= 0) {
						mPaint.setColor(getResources()
								.getColor(R.color.font_up));
					} else {
						mPaint.setColor(getResources().getColor(
								R.color.font_green));
					}
				} else {
					if (price >= price2) {
						mPaint.setColor(getResources()
								.getColor(R.color.font_up));
					} else {
						mPaint.setColor(getResources().getColor(
								R.color.font_green));
					}
				}
				canvas.drawLine(pos, y1 + (height - lowerHeight) + 2
						* DEFAULT_AXIS_TITLE_SIZE, pos, lowerBottom, mPaint);
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
			Paint paint = new Paint();
			paint.setStrokeWidth(2);
			paint.setColor(getResources().getColor(R.color.k_point_line));
			int i = (int) (((touchX - getTextSpace()) / width) * DATA_MAX_COUNT);
			if (i <= 0) {
				canvas.drawLine(getTextSpace(), 2.0f, getTextSpace(),
						UPER_CHART_BOTTOM, paint);
				canvas.drawLine(getTextSpace(), lowerBottom - lowerHeight,
						getTextSpace(), lowerBottom, paint);
			} else if (i < dataList.size()) {
				canvas.drawLine(touchX, 2.0f, touchX, UPER_CHART_BOTTOM, paint);
				canvas.drawLine(touchX, lowerBottom - lowerHeight, touchX,
						lowerBottom, paint);
			} else {
				canvas.drawLine(dataList.size() * 1.0f / DATA_MAX_COUNT * 1.0f
						* width + getTextSpace(), 2.0f, dataList.size() * 1.0f
						/ DATA_MAX_COUNT * 1.0f * width + getTextSpace(),
						UPER_CHART_BOTTOM, paint);
				canvas.drawLine(dataList.size() * 1.0f / DATA_MAX_COUNT * 1.0f
						* width + getTextSpace(), lowerBottom - lowerHeight,
						dataList.size() * 1.0f / DATA_MAX_COUNT * 1.0f * width
								+ getTextSpace(), lowerBottom, paint);
			}

			float ly = 0;
			if (i <= 0) {
				dataOrder = 0;
				ly = (1 - (allList.get(0) - minValue) / (maxNum * 2))
						* uperHeight;
			} else if (i < dataList.size()) {
				dataOrder = i;
				ly = (1 - (allList.get(i) - minValue) / (maxNum * 2))
						* uperHeight;
			} else {
				dataOrder = allList.size() - 1;
				ly = (1 - (allList.get(allList.size() - 1) - minValue)
						/ (maxNum * 2))
						* uperHeight;
			}

			canvas.drawLine(getTextSpace(), ly, width + getTextSpace(), ly,
					paint);
			areaPaint.setColor(getResources().getColor(R.color.k_point_line));
			areaPaint.setStyle(Style.FILL);
			canvas.drawRect(0, ly - DEFAULT_AXIS_TITLE_SIZE / 2 - 2,
					getTextSpace(), ly + DEFAULT_AXIS_TITLE_SIZE / 2 + 4,
					areaPaint);
			canvas.drawRect(getWidth() - getTextSpace(), ly
					- DEFAULT_AXIS_TITLE_SIZE / 2 - 2, getWidth(), ly
					+ DEFAULT_AXIS_TITLE_SIZE / 2 + 4, areaPaint);
			textPaint.setColor(Color.WHITE);
			if (i <= 0) {
				canvas.drawText(decimalFormat.format(allList.get(0)), 2, ly
						+ DEFAULT_AXIS_TITLE_SIZE / 2 - 2, textPaint);
				canvas.drawText(decimalFormat.format((listZF.get(0)) * 100)
						+ "%", getWidth() - getTextSpace() + 2, ly
						+ DEFAULT_AXIS_TITLE_SIZE / 2 - 2, textPaint);
			} else if (i > 0 && i < dataList.size()) {
				canvas.drawText(decimalFormat.format(allList.get(i)), 2, ly
						+ DEFAULT_AXIS_TITLE_SIZE / 2 - 2, textPaint);
				canvas.drawText(decimalFormat.format((listZF.get(i)) * 100)
						+ "%", getWidth() - getTextSpace() + 2, ly
						+ DEFAULT_AXIS_TITLE_SIZE / 2 - 2, textPaint);
			} else {
				canvas.drawText(
						decimalFormat.format(allList.get(allList.size() - 1)),
						2, ly + DEFAULT_AXIS_TITLE_SIZE / 2 - 2, textPaint);
				canvas.drawText(
						decimalFormat.format((listZF.get(listZF.size() - 1)) * 100)
								+ "%", getWidth() - getTextSpace() + 2, ly
								+ DEFAULT_AXIS_TITLE_SIZE / 2 - 2, textPaint);
			}

		}
	}

	/**
	 * 数据处理
	 * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	 * 
	 * @param model
	 */

	public void setData(FiveDayModel model) {
		this.model = model;
		if (model != null) {
			getVolumData();
			getMaxCValue();
			getZhangFuList();
		}
	}

	public FiveDayModel getData() {
		return this.model;
	}

	/**
	 * 获取均价
	 */
	private void getEvePriceList() {
		if (model != null) {
			if (evePriceList != null && evePriceList.size() > 0) {
				evePriceList.clear();
			}
			List<FiveDayEveryModel> stockfiveday = model.getStockfiveday();
			for (int j = 0; j < stockfiveday.size(); j++) {
				List<Float> evePList = new ArrayList<Float>();
				String[] split = stockfiveday.get(j).getStockaveprice()
						.split(",");
				for (int i = 0; i < split.length; i++) {
					evePList.add(Float.parseFloat(split[i]));
				}
				evePriceList.addAll(evePList);
			}
		}

	}

	private void getListTime() {
		if (model != null) {
			if (listTime != null || listTime.size() > 0) {
				listTime.clear();
			}
			List<FiveDayEveryModel> fivedayList = model.getStockfiveday();

			for (int i = 0; i < fivedayList.size(); i++) {
				FiveDayEveryModel obj = fivedayList.get(i);
				String time = obj.getStocktime();
				String[] times = time.split(",");
				List<String> tList = new ArrayList<String>();
				for (int j = 0; j < times.length; j++) {
					String p = times[j];
					tList.add(p);
				}

				listTime.addAll(tList);
			}

		}
	}

	private void getMaxValue() {
		if (model != null) {
			if (allList != null || allList.size() > 0) {
				allList.clear();
			}
			List<FiveDayEveryModel> fivedayList = model.getStockfiveday();

			for (int i = 0; i < fivedayList.size(); i++) {
				FiveDayEveryModel obj = fivedayList.get(i);
				String everyPrice = obj.getStockprice();
				String[] price = everyPrice.split(",");
				List<Float> priceList = new ArrayList<Float>();

				for (int j = 0; j < price.length; j++) {
					float p = Float.parseFloat(price[j]);
					float offset = Math.abs(p - lastClose);
					maxNum = Math.max(maxNum, offset);
					priceList.add(p);

				}
				allList.addAll(priceList);
			}
		}
	}

	private void getFiveDayData() {
		if (model != null) {
			List<FiveDayEveryModel> fivedayList = model.getStockfiveday();

			if (list1 != null && list1.size() > 0) {
				list1.clear();
			}
			if (list2 != null && list2.size() > 0) {
				list2.clear();
			}
			if (list3 != null && list3.size() > 0) {
				list3.clear();
			}
			if (list4 != null && list4.size() > 0) {
				list4.clear();
			}
			if (list5 != null && list5.size() > 0) {
				list5.clear();
			}

			for (int i = 0; i < fivedayList.size(); i++) {
				FiveDayEveryModel obj = fivedayList.get(i);
				String everyPrice = obj.getStockprice();
				String[] price = everyPrice.split(",");
				switch (i) {
				case 0:
					for (int j = 0; j < price.length; j++) {
						float p = Float.parseFloat(price[j]);
						list1.add(p);
					}
					break;
				case 1:
					for (int j = 0; j < price.length; j++) {
						float p = Float.parseFloat(price[j]);
						list2.add(p);
					}
					break;
				case 2:
					for (int j = 0; j < price.length; j++) {
						float p = Float.parseFloat(price[j]);
						list3.add(p);
					}
					break;
				case 3:
					for (int j = 0; j < price.length; j++) {
						float p = Float.parseFloat(price[j]);
						list4.add(p);
					}
					break;
				case 4:
					for (int j = 0; j < price.length; j++) {
						float p = Float.parseFloat(price[j]);
						list5.add(p);
					}
					break;
				}
			}
		}
	}

	private void getLastClose() {
		if (model != null) {
			lastClose = (float) Double.parseDouble(model.getStocklastclose());
		}
	}

	/**
	 * 底部数据处理
	 */
	private void getVolumData() {
		if (model != null) {

			if (volumeList != null && volumeList.size() > 0) {
				volumeList.clear();
			}
			if (dataList != null && dataList.size() > 0) {
				dataList.clear();
			}

			List<FiveDayEveryModel> list = model.getStockfiveday();
			for (int i = 0; i < list.size(); i++) {

				FiveDayEveryModel model = list.get(i);
				if (model != null) {
					String volum = model.getStockvolume();
					String[] everyVolum = volum.split(",");
					if (i == 0) {
						length = everyVolum.length;
						System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  "
								+ length);
					}

					String price = model.getStockprice();
					String[] everyPrice = price.split(",");

					for (int j = 0; j < everyVolum.length; j++) {
						if (everyVolum[j] != null) {
							if (j == everyVolum.length - 1) {

							} else {
								long v1 = Long.parseLong(everyVolum[j]);
								long v2 = Long.parseLong(everyVolum[j + 1]);
								if (j == 0) {
									volumeList.add(v1);
									volumeList.add(Math.abs(v2 - v1));
								} else {
									volumeList.add(Math.abs(v2 - v1));
								}
							}
						}
						if (everyPrice[j] != null) {
							if (j == everyVolum.length - 1) {
							} else {
								float p1 = Float.parseFloat(everyPrice[j]);
								float p2 = Float.parseFloat(everyPrice[j + 1]);
								if (j == 0) {
									dataList.add(p1);
									dataList.add(p2 - p1);
								} else {
									dataList.add(p2 - p1);
								}
							}
						}
					}
				}
			}
		}
	}

	private void getMaxCValue() {

		float max = 0.0f;
		float min = 0.0f;

		if (volumeList != null && volumeList.size() > 0) {
			for (int i = 0; i < volumeList.size(); i++) {
				float nextPrice = volumeList.get(i);
				max = Math.max(max, nextPrice);
				min = Math.min(min, nextPrice);
			}
			minValue2 = min;
			maxValus2 = max * weight;

			offset = maxValus2 - minValue2;
		}
	}

	/**
	 * 获取涨幅
	 */
	private void getZhangFuList() {
		if (model != null) {
			List<FiveDayEveryModel> stockfiveday = model.getStockfiveday();
			for (int i = 0; i < stockfiveday.size(); i++) {
				String[] split = stockfiveday.get(i).getStockzhangfu()
						.split(",");
				for (int j = 0; j < split.length; j++) {
					listZF.add(Double.parseDouble(split[j]));
				}
			}
		}
	}

	/**
	 * ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
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

	/**
	 * +++++++++++++++++++++++++++++++++++外部获取内部数据++++++++++++++++++++++++++++++
	 * +++++++++++++++
	 */
	/**
	 * 是否点击
	 * 
	 * @return
	 */
	public boolean getIsShow() {
		return showDetails;
	}

	/**
	 * 返回当前时间
	 * 
	 * @return
	 */
	public String getTime() {
		if (listTime.size() > 0) {

			if (dataOrder < listTime.size()) {
				return listTime.get(dataOrder);
			} else {
				return listTime.get(listTime.size() - 1);
			}
		}
		return "--";
	}

	/**
	 * 返回当前价
	 * 
	 * @return
	 */
	public float getPrice() {

		if (allList.size() > 0) {
			if (dataOrder < allList.size()) {
				return allList.get(dataOrder);
			} else {
				return allList.get(allList.size() - 1);
			}
		}
		return 0;
	}

	/**
	 * 返回涨幅
	 * 
	 * @return
	 */
	public double getZhangfu() {
		if (listZF.size() > 0) {
			if (dataOrder < listZF.size()) {
				return listZF.get(dataOrder);
			} else {
				return listZF.get(listZF.size() - 1);
			}
		}

		return 0;
	}

	/**
	 * 返回交易量
	 * 
	 * @return
	 */
	public long getVolumeNum() {
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
		if (evePriceList.size() > 0) {
			if (dataOrder < evePriceList.size()) {
				return evePriceList.get(dataOrder);
			} else {
				return evePriceList.get(evePriceList.size() - 1);
			}
		}
		return 0;
	}

	/**
	 * 5日内部接口
	 * 
	 * @author xiongfeng
	 * 
	 */
	public interface FiveMoveListener extends EventListener {
		public void doMove();
	}

}
