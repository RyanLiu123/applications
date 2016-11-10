
package com.huaqin.autocycletest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.huaqin.autocycletest.test.sd.sdPreCopy;
import com.huaqin.autocycletest.util.AutoCycleTestUtils;
import com.huaqin.autocycletest.util.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class ControlCenterActivity extends Activity implements OnItemClickListener {

    private static String TAG = "ControlCenterActivity";
    private static int[][] mFullTestItem = AutoCycleTestUtils.TOTAL_ITEM;
    private static HashMap<Integer, int[]> mTestMap = new HashMap<Integer, int[]>();
    private static HashMap<Integer, Boolean> mListItemStatus = new HashMap<Integer, Boolean>();
    static {
        for (int i = 0; i < AutoCycleTestUtils.LIST_ITEM_ALL.length; i++) {
            mListItemStatus.put(i, true);
        }
    }

    private ListView mMainList = null;
    private ArrayList<Integer> mchecked_list = new ArrayList<Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_center);
        AutoCycleTestConfig.getInstance().initConfig(getApplicationContext());
        initMainView();
        mTestMap.clear();
        (new sdPreCopy()).execute(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        (new sdPreCopy()).execute(false);
        super.onDestroy();
    }

    private void initMainView() {
        mMainList = (ListView) findViewById(R.id.main_list);
        mMainList.setAdapter(new ArrayAdapter<Object>(this, R.layout.list_item,
                getResources().getStringArray(R.array.main_list_item)));
        mMainList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (view instanceof CheckedTextView) {
            if (!mListItemStatus.get(position))
                return;
            boolean checked = ((CheckedTextView) view).isChecked();
            ((CheckedTextView) view).setChecked(!checked);
            setClickedItemAndStatus(position, !checked);
        }
    }

    private void setClickedItemAndStatus(int position, boolean checked) {
        if (position == AutoCycleTestUtils.LIST_ITEM_6) {
            // skip
        } else {
            if (mTestMap.get(position) == null) {
                LogUtils.i(TAG, "add position " + position + " to testArrays!");
                mTestMap.put(position, mFullTestItem[position]);
                for (int i = 0; i < mFullTestItem[position].length; i++) {
                    LogUtils.i(TAG, "mFullTestItem[" + position + "][" + i + "] = "
                            + mFullTestItem[position][i]);
                }
            } else {
                LogUtils.i(TAG, "remove position = " + position);
                mTestMap.remove(position);
            }
        }
        if (position == AutoCycleTestUtils.LIST_ITEM_6) {
            if (checked) {
                for (int m = 0; m < mMainList.getChildCount() - 1; m++) {
                    if (m != AutoCycleTestUtils.LIST_ITEM_6) {
                        View v = mMainList.getChildAt(m);
                        if (v instanceof CheckedTextView) {
                            changeItemstatus(v, false, false);
                            mListItemStatus.put(m, false);
                            mTestMap.put(m, mFullTestItem[m]);
                        }
                    }
                }
                AutoCycleTestConfig.getInstance().setCycleTest(true);
            } else {
                for (int m = 0; m < mMainList.getChildCount() - 1; m++) {
                    View v = mMainList.getChildAt(m);
                    if (v instanceof CheckedTextView) {
                        changeItemstatus(v, false, true);
                        mListItemStatus.put(m, true);
                        mTestMap.remove(m);
                    }
                }
                AutoCycleTestConfig.getInstance().setCycleTest(false);
            }
        } else {
            if (checked) {
                mListItemStatus.put(AutoCycleTestUtils.LIST_ITEM_6, false);
                changeItemstatus(null, false, false);
            } else {
                boolean hasChecked = false;
                for (int m = 0; m < mMainList.getChildCount() - 1; m++) {
                    View v = mMainList.getChildAt(m);
                    if (v instanceof CheckedTextView) {
                        if (((CheckedTextView) v).isChecked()) {
                            hasChecked = true;
                            break;
                        }
                    }
                }
                if (hasChecked) {
                    mListItemStatus.put(AutoCycleTestUtils.LIST_ITEM_6, false);
                    changeItemstatus(null, false, false);
                } else {
                    mListItemStatus.put(AutoCycleTestUtils.LIST_ITEM_6, true);
                    changeItemstatus(null, false, true);
                }
            }
        }
    }

    private void changeItemstatus(View v, boolean checkable, boolean enable) {
        if (v == null) {
            v = mMainList.getChildAt(AutoCycleTestUtils.LIST_ITEM_6);
        }
        if (v instanceof CheckedTextView) {
            ((CheckedTextView) v).setChecked(checkable);
            v.setEnabled(enable);
        }
    }

    private int checkItemNumber() {
        int num = 0;
        mchecked_list.clear();
        for (int i = 0; i < AutoCycleTestUtils.LIST_ITEM_ALL.length; i++) {
            if (mTestMap.get(i) != null) {
                num++;
                mchecked_list.add(i);
            }
        }
        LogUtils.i(TAG, "checkItem num = " + num);
        return num;
    }

    public void handlerStart(View v) {
        LogUtils.i(TAG, "handlerStart click !!!");
        if (checkItemNumber() > 0) {
            AutoCycleTestConfig.getInstance().cleanResult();
            AutoCycleTestConfig.getInstance().saveTestItem(mTestMap);
            AutoCycleTestConfig.getInstance().setCheckLits(mchecked_list);
            Intent intent = new Intent(this, AutoCycleTestTesting.class);
            startActivity(intent);
        }
    }

    public void handlerSetting(View v) {
        LogUtils.i(TAG, "handlerSetting click !!!");
        Intent intent = new Intent(this, AutoCycleTestSetting.class);
        startActivity(intent);
    }
}
