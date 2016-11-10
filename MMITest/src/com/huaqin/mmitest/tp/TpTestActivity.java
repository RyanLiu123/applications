/*
 * Copyright (C) 2016 liunianliang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaqin.mmitest.tp;

import java.util.ArrayList;
import java.util.List;

import com.huaqin.mmitest.util.LogUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * @author liunianliang
 * 
 */
@SuppressLint("WrongCall")
public class TpTestActivity extends Activity {

	private static String TAG = "TpTestActivity";

	private List<TouchPath> mSidelineTouchPathList;

	private Bitmap mBitmap;
	private Panel mView;

	private int isTestItem = 0;

	int hightPix;
	int widthPix;

	private boolean throughMiddle;

	private boolean bTested = false;

	private static final int MSG_TEST_PANEL_NEXT_ITEM = 100;
	private static final int MSG_TEST_PANEL_START = 101;
	private static final int MSG_TEST_PANEL_STOP = 102;

	@SuppressLint("HandlerLeak")
	private Handler mInHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_TEST_PANEL_START:
				for (isTestItem = 0; isTestItem < 8; isTestItem++) {
					mSidelineTouchPathList.get(isTestItem).needTest = true;
				}
				break;
			case MSG_TEST_PANEL_NEXT_ITEM:
				if (isTestItem > 7) {
					mInHandler.sendEmptyMessage(MSG_TEST_PANEL_STOP);
					break;
				}
				bTested = false;
				mSidelineTouchPathList.get(isTestItem).needTest = true;
				isTestItem++;
				mView.postInvalidate();
				break;
			case MSG_TEST_PANEL_STOP:
				isTestItem = 0;
				sendResult("pass");
				break;
			default:
				break;
			}
		}
	};

	/*
	 * 2015-10-29
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		DisplayMetrics mDisplayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		hightPix = mDisplayMetrics.heightPixels;
		widthPix = mDisplayMetrics.widthPixels;

		LogUtil.d(TAG, hightPix + " " + widthPix);

		createTestPath(widthPix, hightPix);

		initBackgroundBitmap(hightPix, widthPix);

		mView = new Panel(this);
		setContentView(mView);
		mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_GESTURE_ISOLATED);

		mInHandler.sendEmptyMessage(MSG_TEST_PANEL_START);
	}

	private void createTestPath(int widthPix, int hightPix) {
		float variableLength = 50;
		float x1, y1;
		float x2, y2;
		Path line = null;
		mSidelineTouchPathList = new ArrayList<TouchPath>();
		float mXtoX = (widthPix - 2 * variableLength) / 3;
		float mYtoY = (hightPix - 2 * variableLength) / 3;
		// modify by liunianliang for nine path touchpanel
		for (int i = 0; i < 8; i++) {
			if (i < 4) {
				line = new Path();
				x1 = 0 + i * mXtoX;
				y1 = 0;
				x2 = 2 * variableLength + i * mXtoX;
				y2 = hightPix;

				line.moveTo(variableLength + i * mXtoX, variableLength);
				line.lineTo(variableLength + i * mXtoX, hightPix
						- variableLength);
				line.close();
				mSidelineTouchPathList
						.add(new TouchPath(new RectF(x1, y1, x2, y2),
								new RectF(x1, y1, x2, y1 + 2 * variableLength),
								new RectF(x1, y2 - 2 * variableLength, x2, y2),
								i, line));
			} else {
				line = new Path();
				x1 = 0;
				y1 = (i - 4) * mYtoY;
				x2 = widthPix;
				y2 = 2 * variableLength + (i - 4) * mYtoY;

				line.moveTo(variableLength, variableLength + (i - 4) * mYtoY);
				line.lineTo(widthPix - variableLength, variableLength + (i - 4)
						* mYtoY);
				line.close();
				mSidelineTouchPathList
						.add(new TouchPath(new RectF(x1, y1, x2, y2),
								new RectF(x1, y1, x1 + 2 * variableLength, y2),
								new RectF(x2 - 2 * variableLength, y1, x2, y2),
								i, line));
			}
		}
	}

	private void initBackgroundBitmap(int hightPix, int widthPix) {
		mBitmap = Bitmap.createBitmap(widthPix, hightPix,
				Bitmap.Config.ARGB_8888);
	}

	private class Panel extends View {
		private Paint paint;

		private Paint mBitmapPaint;

		private Canvas canvas;

		private Path mPath;

		private float mX, mY = -1;

		private static final float TOUCH_TOLERANCE = 1;

		public Panel(Context context) {
			super(context);
			canvas = new Canvas();
			canvas.setBitmap(mBitmap);

			paint = new Paint(Paint.DITHER_FLAG);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(3);
			paint.setColor(Color.WHITE);
			paint.setAntiAlias(true);

			mBitmapPaint = new Paint(Paint.DITHER_FLAG);

		}

		@Override
		protected void onDraw(Canvas canvas) {

			canvas.drawColor(0xFF000000);
			canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

			if (mPath != null) {
				canvas.drawPath(mPath, paint);
			}

			for (TouchPath touchPath : mSidelineTouchPathList) {
				if (!touchPath.isTouchPass() && touchPath.needTest) {
					touchPath.onDraw(canvas);
				}
			}
		}

		private void dispatchTouchEventToTouchPath(float x, float y, int action) {
			if ((x < (widthPix / 2 + 100) && x > (widthPix / 2 - 100))
					|| (y < (hightPix / 2 + 100) && y > (hightPix / 2 - 100))) {
				throughMiddle = true;
			}
			boolean sidelinePass = true;
			int isTesting = -1;
			for (TouchPath touchPath : mSidelineTouchPathList) {
				if (!touchPath.isTouchPass() && touchPath.needTest) {

					if (MotionEvent.ACTION_DOWN == action) {
						if (touchPath.isDownInStartOREndPath(x, y)) {
							isTesting = 0;
						} else {
							touchPath.isStarted = false;
							touchPath.isEnded = false;
						}
					} else if (MotionEvent.ACTION_MOVE == action) {
						if (touchPath.isDownInStartOREndPath(x, y)
								|| touchPath.isInPath(x, y)) {
							isTesting++;

							if (touchPath.isStarted && touchPath.isEnded
									&& throughMiddle) {
								touchPath.setTouchPassed(true);
							}

						} else {
							touchPath.isStarted = false;
							touchPath.isEnded = false;
						}
					} else if (MotionEvent.ACTION_UP == action) {
						touchPath.isStarted = false;
						touchPath.isEnded = false;
						throughMiddle = false;
					}
				}
			}
			for (TouchPath touchPath : mSidelineTouchPathList) {
				if (!touchPath.isTouchPass() && touchPath.needTest) {
					sidelinePass = false;
				}
			}
			if (isTesting < 0) {
				mPath = null;
			}

			if (sidelinePass && !bTested) {
				bTested = true;
				mInHandler.sendEmptyMessage(MSG_TEST_PANEL_NEXT_ITEM);
			}
		}

		private void touch_start(float x, float y) {
			mPath.moveTo(x, y);
			mX = x;
			mY = y;
		}

		private void touch_move(float x, float y) {
			float dx = Math.abs(x - mX);
			float dy = Math.abs(mY - y);
			if ((dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE)
					&& (mPath != null)) {
				mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
				mX = x;
				mY = y;
			}
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {

			float x = event.getX();
			float y = event.getY();
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:

				mPath = new Path();
				touch_start(x, y);
				dispatchTouchEventToTouchPath(event.getX(), event.getY(),
						MotionEvent.ACTION_DOWN);
				break;

			case MotionEvent.ACTION_MOVE:
				touch_move(x, y);
				dispatchTouchEventToTouchPath(x, y, MotionEvent.ACTION_MOVE);
				break;
			case MotionEvent.ACTION_UP:

				dispatchTouchEventToTouchPath(x, y, MotionEvent.ACTION_UP);
				break;
			}
			invalidate();
			return true;
		}
	}

	class TouchPath {

		private List<PointF> mListPoint = new ArrayList<PointF>();
		private Region region;
		private boolean touchPass;
		private Paint paint;
		private Path line;
		private RectF startRect;
		private RectF endRect;
		public boolean isStarted = false;
		public boolean isEnded = false;
		public boolean needTest = false;

		TouchPath(RectF rect, RectF startRect, RectF endRect, int second,
				Path line) {
			paint = new Paint(Paint.DITHER_FLAG);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(3);
			paint.setColor(Color.RED);
			paint.setAntiAlias(true);

			region = new Region((int) rect.left, (int) rect.top,
					(int) rect.right, (int) rect.bottom);

			this.startRect = startRect;
			this.endRect = endRect;
			this.line = line;
		}

		TouchPath(List<PointF> listPoint, RectF startRect, RectF endRect,
				int second, Path line) {
			paint = new Paint(Paint.DITHER_FLAG);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(3);
			paint.setColor(Color.RED);
			paint.setAntiAlias(true);
			region = new Region();

			if (listPoint != null) {
				mListPoint.addAll(listPoint);
			}
			region = null;
			this.startRect = startRect;
			this.endRect = endRect;
			this.line = line;
		}

		private void onDraw(Canvas canvas, Paint paint) {
			if (!touchPass) {
				canvas.drawPath(line, paint);
			}
		}

		public void onDraw(Canvas canvas) {
			onDraw(canvas, paint);
		}

		public boolean isInPath(float x, float y) {
			if (region == null) {
				return isPolygonContainPoint(x, y);
			}
			return region.contains((int) x, (int) y);
		}

		public boolean isDownInStartOREndPath(float x, float y) {
			if (startRect.contains(x, y)) {
				isStarted = true;
			}
			if (endRect.contains(x, y)) {
				isEnded = true;
			}
			return (startRect.contains(x, y) || endRect.contains(x, y));
		}

		private boolean isPolygonContainPoint(float x, float y) {
			int nCross = 0;
			for (int i = 0; i < mListPoint.size(); i++) {
				PointF p1 = mListPoint.get(i);
				PointF p2 = mListPoint.get((i + 1) % mListPoint.size());
				if (p1.y == p2.y)
					continue;
				if (y < Math.min(p1.y, p2.y))
					continue;
				if (y >= Math.max(p1.y, p2.y))
					continue;
				double x1 = (double) (y - p1.y) * (double) (p2.x - p1.x)
						/ (double) (p2.y - p1.y) + p1.x;
				if (x1 > x)
					nCross++;
			}
			return (nCross % 2 == 1);
		}

		public void setTouchPassed(boolean touchPass) {
			this.touchPass = touchPass;
		}

		public boolean isTouchPass() {
			return touchPass;
		}
	}

	private void sendResult(String result) {
		Intent intent = new Intent("com.mmi.helper.response");
		intent.putExtra("type", "tp_test");
		intent.putExtra("value", result);
		sendBroadcast(intent);
		LogUtil.d(TAG, "result = "+result);
		finish();
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	sendResult("fail");
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }
}
