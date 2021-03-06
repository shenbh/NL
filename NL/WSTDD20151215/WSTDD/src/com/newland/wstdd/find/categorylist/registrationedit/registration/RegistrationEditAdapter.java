package com.newland.wstdd.find.categorylist.registrationedit.registration;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.R.integer;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.newland.wstdd.R;
import com.newland.wstdd.common.tools.UiHelper;
import com.newland.wstdd.find.categorylist.registrationedit.beanrequest.MainSignAttr;

/**
 * 发现-listview 动态生成的 子适配器
 * 
 * @author H81 2015-11-6
 * 
 */
public class RegistrationEditAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private Context context;
	List<MainSignAttr> registrationData;
	private int focusPosition = -1;
	public RegistrationEditAdapter(Context context,
			List<MainSignAttr> registrationData) {
		this.context = context;
		this.registrationData = registrationData;
		mInflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return registrationData == null ? 0 : registrationData.size();
	}

	@Override
	public MainSignAttr getItem(int position) {
		if (registrationData.get(position) != null && registrationData.size() != 0) {
			return registrationData.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;

		if (convertView == null) {
	
			// 根据自定义的Item布局加载布局
			convertView = mInflater.inflate(R.layout.registration_edit_listview_childitem,parent, false);
			holder = new ViewHolder();
			holder.editText=(EditText) convertView.findViewById(R.id.registration_item_child_editext);
			holder.textView=(TextView) convertView.findViewById(R.id.registration_item_child_textview);
			// 将设置好的布局保存到缓存中，并将其设置在Tag里，以便后面方便取出Tag
			convertView.setTag(holder);
			
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.textView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				UiHelper.ShowOneToast(context, holder.textView.getText().toString());
			}
		});
	
	
//		Iterator iter = registrationData.get(parentPosition).getMap().get(position).entrySet().iterator();
//		while (iter.hasNext()) {
//		Map.Entry entry = (Map.Entry) iter.next();
//		Object key = entry.getKey();
//		Object val = entry.getValue();
//		holder.editText.setText((String)val);
//		holder.textView.setText((String) key);
//		}
		MainSignAttr data = getItem(position);
		
			holder.textView.setText(data.getName());
			holder.editText.setText(data.getValue());

		holder.editText.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					focusPosition = position;
				} else {
					focusPosition = -1;
				}
			}
		});
		holder.editText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (position == focusPosition) {
					if(s!=null&&!"".equals(s.toString())){
						getItem(position).setValue(s.toString());
					}
						
				}
			}
		});
		
		return convertView;
	}

	// ViewHolder静态类
	 class ViewHolder {
		 EditText editText;// 动态生成
		 TextView textView;//标签
	}

	public List<MainSignAttr> getRegistrationData() {
		return registrationData;
	}

	public void setRegistrationData(List<MainSignAttr> registrationData) {
		this.registrationData = registrationData;
	}

	
	 


	
	
}
