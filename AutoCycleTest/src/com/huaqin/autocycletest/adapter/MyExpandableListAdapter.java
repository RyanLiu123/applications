/**
 * 2016-4-7
 * MyExpandableListAdapter.java
 * TODO testing adapter
 * liunianliang
 */

package com.huaqin.autocycletest.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.TextView;

import com.huaqin.autocycletest.AutoCycleTestConfig;
import com.huaqin.autocycletest.R;

/**
 * @author liunianliang
 */
public class MyExpandableListAdapter extends BaseExpandableListAdapter implements
        ExpandableListAdapter {

    private static String TAG = "MyExpandableListAdapter";

    private Context mContext;
    private LayoutInflater mInflater;
    private String result = "";

    public MyExpandableListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        result = mContext.getString(R.string.caseresult_none);
    }

    @Override
    public int getGroupCount() {
        return AutoCycleTestConfig.getInstance().getTestingAdapterGroupNames().size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        int count = AutoCycleTestConfig
                .getInstance()
                .getTestMap()
                .
                get(AutoCycleTestConfig.getInstance().getTestingAdapterGroupChilds()
                        .get(groupPosition)).length;
        return count;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return AutoCycleTestConfig.getInstance().getTestingAdapterGroupNames().get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return AutoCycleTestConfig.getInstance().getAllItemTest()[AutoCycleTestConfig
                .getInstance()
                .getTestMap()
                .
                get(AutoCycleTestConfig.getInstance().getTestingAdapterGroupChilds()
                        .get(groupPosition))[childPosition]];
    }

    private String getResult(int groupPosition, int childPosition) {
        try {
            int categoryId = AutoCycleTestConfig.getInstance().getTestingAdapterGroupChilds()
                    .get(groupPosition);
            String caseName = getChild(groupPosition, childPosition).toString();
            String caseresult = AutoCycleTestConfig.getInstance().getResultMap()
                    .get(categoryId + caseName);
            /*
             * LogUtils.i(TAG, "categoryId = " + categoryId + " caseName =" + caseName +
             * "  caseresult =" + caseresult);
             */
            if (caseresult != null) {
                return caseresult;
            }
        } catch (NullPointerException e) {
            return result;
        }
        return result;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
            ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.expand_group_layout, parent, false);
            viewHolder.name = (TextView) convertView.findViewById(R.id.text2);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.name.setText(getGroup(groupPosition).toString());
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
            View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.expand_child_layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) convertView.findViewById(R.id.item_name);
            viewHolder.result = (TextView) convertView.findViewById(R.id.item_result);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.name.setText(getChild(groupPosition, childPosition).toString());
        viewHolder.result.setText(getResult(groupPosition, childPosition));
        if (mContext.getString(R.string.caseresult_success).equals(viewHolder.result.getText())) {
            convertView.setBackgroundColor(Color.GREEN);
        } else if (mContext.getString(R.string.casestate_aborted).equals(viewHolder.result.getText()) ||
                mContext.getString(R.string.casestate_failed).equals(viewHolder.result.getText())) {
            convertView.setBackgroundColor(Color.RED);
        } else {
            convertView.setBackgroundColor(Color.WHITE);
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public long getCombinedChildId(long groupId, long childId) {
        return 0;
    }

    @Override
    public long getCombinedGroupId(long groupId) {
        return 0;
    }

    private static class ViewHolder
    {
        TextView name;
        TextView result;
    }

}
