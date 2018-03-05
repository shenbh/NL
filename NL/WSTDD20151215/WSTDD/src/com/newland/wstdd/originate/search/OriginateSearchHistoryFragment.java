package com.newland.wstdd.originate.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.newland.wstdd.R;
import com.newland.wstdd.common.SharePreferenceUtil.SharedPreferencesRefreshUtil;
import com.newland.wstdd.common.base.BaseFragment;
import com.newland.wstdd.common.common.AppContext;
import com.newland.wstdd.common.resultlisterer.OnPostListenerInterface;
import com.newland.wstdd.common.tools.StringUtil;
import com.newland.wstdd.originate.search.OriginateSearchAdapter.onItemClickListener;

/**搜索历史列表界面 Fragment
 * @author Administrator
 * 2015-12-10
 */
@SuppressLint("ValidFragment")
public class OriginateSearchHistoryFragment extends BaseFragment implements OnPostListenerInterface, OnClickListener {
	private TextView moriginate_interestcontent_tv;// 搜索感兴趣的内容
	private TextView moriginate_history_tv;// 历史记录
	private ListView moriginate_history_lv;// 历史记录列表
	private TextView moriginate_cleanhistory_tv;// 清除历史记录
	private OriginateSearchAdapter originateSearchAdapter;

	private Context context;
	private AppContext appContext;

	SharedPreferencesRefreshUtil sharedPreferencesUtil = new SharedPreferencesRefreshUtil(context);
	List<HashMap<String, String>> datasList;

	/** 标签 */
	List<String> tags = new ArrayList<String>();// 标签
	private TextView[][] tv_tags = new TextView[2][4];
	private LinearLayout linearlayout1;// 第一排4个tags数据
	private LinearLayout linearlayout2;// 第二排4个tags数据

	/** 标签 */


	@SuppressLint("ValidFragment")
	public OriginateSearchHistoryFragment(Context context) {
		this.context = context;
	}

	private void initView(View view) {
		moriginate_interestcontent_tv = (TextView) view.findViewById(R.id.originate_interestcontent_tv);
		moriginate_history_tv = (TextView) view.findViewById(R.id.originate_history_tv);
		moriginate_history_lv = (ListView) view.findViewById(R.id.originate_history_lv);
		moriginate_cleanhistory_tv = (TextView) view.findViewById(R.id.originate_cleanhistory_tv);
		datasList = new ArrayList<HashMap<String, String>>();
		datasList = sharedPreferencesUtil.getInfo(context, "searchhistory");

		originateSearchAdapter = new OriginateSearchAdapter(context, datasList);
		moriginate_history_lv.setAdapter(originateSearchAdapter);
		originateSearchAdapter.setOnItemClickListener(new onItemClickListener() {
			
			@Override
			public void onItemClick(View v, int position) {
				EditText searchEdt = (EditText)getActivity().findViewById(R.id.originate_search_edt);

				searchEdt.setText(((TextView)v).getText().toString());
				getActivity().findViewById(R.id.originate_search_tv).performClick();
			}
		});

		moriginate_cleanhistory_tv.setOnClickListener(this);

		tv_tags[0][0] = (TextView) view.findViewById(R.id.tags11_tv);
		tv_tags[0][1] = (TextView) view.findViewById(R.id.tags12_tv);
		tv_tags[0][2] = (TextView) view.findViewById(R.id.tags13_tv);
		tv_tags[0][3] = (TextView) view.findViewById(R.id.tags14_tv);
		tv_tags[1][0] = (TextView) view.findViewById(R.id.tags21_tv);
		tv_tags[1][1] = (TextView) view.findViewById(R.id.tags22_tv);
		tv_tags[1][2] = (TextView) view.findViewById(R.id.tags23_tv);
		tv_tags[1][3] = (TextView) view.findViewById(R.id.tags24_tv);
		linearlayout1 = (LinearLayout) view.findViewById(R.id.linearlayout1);
		linearlayout2 = (LinearLayout) view.findViewById(R.id.linearlayout2);
		//设置点击监听事件
		for (int i = 0; i < 8; i++) {
			if (i<4) {
				tv_tags[0][i].setOnClickListener(this);
			}else {
				tv_tags[1][i-4].setOnClickListener(this);
			}
		}
		setTagsData(tags);
	}

	/**
	 * 设置标签内容
	 * 
	 */
	private void setTagsData(List<String> tags) {
		int size = tags.size();
		if (size <= 4) {
			linearlayout2.setVisibility(View.GONE);
			for (int i = 0; i < size; i++) {
				tv_tags[0][i].setText(tags.get(i));
				tv_tags[0][i].setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.text_roundstyle));
				if (StringUtil.isEmpty(tv_tags[0][i].getText().toString())) {
					tv_tags[0][i].setVisibility(View.INVISIBLE);
				}
			}
		} else {
			linearlayout2.setVisibility(View.VISIBLE);
			for (int i = 0; i < size; i++) {
				if (i < 4) {
					tv_tags[0][i].setText(tags.get(i));
					tv_tags[0][i].setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.text_roundstyle));
				}else {
					tv_tags[1][i-4].setText(tags.get(i));
					tv_tags[1][i-4].setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.text_roundstyle));
					if (StringUtil.isEmpty(tv_tags[1][i-4].getText().toString())) {
						tv_tags[1][i-4].setVisibility(View.INVISIBLE);
					}
				}
			}
		}

	}

	@Override
	public void onClick(View v) {
		EditText searchEdt = (EditText)getActivity().findViewById(R.id.originate_search_edt);
		switch (v.getId()) {
		case R.id.originate_cleanhistory_tv:
			sharedPreferencesUtil.cleanInfo(context);
			originateSearchAdapter.getList().clear();
			originateSearchAdapter.setList(originateSearchAdapter.getList());
			originateSearchAdapter.notifyDataSetChanged();
			break;
		case R.id.tags11_tv:
			searchEdt.setText(tv_tags[0][0].getText().toString());
			getActivity().findViewById(R.id.originate_search_tv).performClick();
			break;
		case R.id.tags12_tv:
			searchEdt.setText(tv_tags[0][1].getText().toString());
			getActivity().findViewById(R.id.originate_search_tv).performClick();
			break;
		case R.id.tags13_tv:
			searchEdt.setText(tv_tags[0][2].getText().toString());
			getActivity().findViewById(R.id.originate_search_tv).performClick();
			break;
		case R.id.tags14_tv:
			searchEdt.setText(tv_tags[0][3].getText().toString());
			getActivity().findViewById(R.id.originate_search_tv).performClick();
			break;
		case R.id.tags21_tv:
			searchEdt.setText(tv_tags[1][0].getText().toString());
			getActivity().findViewById(R.id.originate_search_tv).performClick();
			break;
		case R.id.tags22_tv:
			searchEdt.setText(tv_tags[1][1].getText().toString());
			getActivity().findViewById(R.id.originate_search_tv).performClick();
			break;
		case R.id.tags23_tv:
			searchEdt.setText(tv_tags[1][2].getText().toString());
			getActivity().findViewById(R.id.originate_search_tv).performClick();
			break;
		case R.id.tags24_tv:
			searchEdt.setText(tv_tags[1][3].getText().toString());
			getActivity().findViewById(R.id.originate_search_tv).performClick();
			break;
		default:
			break;
		}
	}

	@Override
	public void OnHandleResultListener(Object obj, int responseId) {

	}

	@Override
	public void OnFailResultListener(String mess) {
		if (dialog != null) {
			dialog.dismiss();
		}

	}

	@Override
	protected View createAndInitView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_originate_searchhistory, container, false);
		appContext = AppContext.getAppContext();
		tags = appContext.getTags();
		initView(view);
		return view;
	}

	@Override
	public void refresh() {

	}

}
