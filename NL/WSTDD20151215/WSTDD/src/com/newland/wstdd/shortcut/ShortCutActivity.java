package com.newland.wstdd.shortcut;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.newland.wstdd.R;
import com.newland.wstdd.common.base.BaseFragmentActivity;
import com.newland.wstdd.common.tools.UiHelper;
import com.newland.wstdd.common.widget.PengTextView;

/**
 * 快捷界面
 * 
 * @author H81 2015-11-4
 * 
 */
public class ShortCutActivity extends BaseFragmentActivity implements OnClickListener {
	private static final int PICTURE = 1; // 选择图片
	private EditText mActivity_shortcut_contentedt;// 输入的内容
	private PengTextView mActivity_shortcut_remainwords_tv;// 还可输入150字
	private ImageView mActivity_shortcut_choosepicture_iv;// 选择图片
	private String phtotFilename;// 图片路径
	private String photoname;
	private int inputNum = 150;// 限制输入的字数

	// 标题栏
	private TextView mHead_left_tv;
	private TextView mHead_right_tv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shortcut_main);
		setTitle();
		initView();
	}

	/**
	 * 设置标题
	 */
	private void setTitle() {
		mHead_left_tv = (TextView) findViewById(R.id.head_left_tv);
		mHead_right_tv = (TextView) findViewById(R.id.head_right_tv);
		TextView mHead_center_title = (TextView) findViewById(R.id.head_center_title);
		initView();
		if (mHead_center_title != null)
			mHead_center_title.setText("快捷发布");
		if (mHead_left_tv != null) {// 返回
			mHead_left_tv.setVisibility(View.VISIBLE);
			mHead_left_tv.setText("取消");
			mHead_left_tv.setOnClickListener(this);
		}
		if (mHead_right_tv != null) {
			mHead_right_tv.setVisibility(View.VISIBLE);
			mHead_right_tv.setText("发布");
			mHead_right_tv.setOnClickListener(this);
		}

		mHead_left_tv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mHead_right_tv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				UiHelper.ShowOneToast(ShortCutActivity.this, "发布");
			}
		});
	}

	public void initView() {
		mActivity_shortcut_contentedt = (EditText) findViewById(R.id.activity_shortcut_contentedt);
		mActivity_shortcut_remainwords_tv = (PengTextView) findViewById(R.id.activity_shortcut_remainwords_tv);
		mActivity_shortcut_choosepicture_iv = (ImageView) findViewById(R.id.activity_shortcut_choosepicture_iv);
		mActivity_shortcut_choosepicture_iv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showImgFileChooser();
			}
		});

		// 限制输入的个数
		mActivity_shortcut_contentedt.addTextChangedListener(new TextWatcher() {
			private boolean isOutOfBounds = false;
			int end;

			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.length() > inputNum) {
					isOutOfBounds = true;
				} else {
					mActivity_shortcut_remainwords_tv.setText(s.length() + "/150");
					isOutOfBounds = false;
				}
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void afterTextChanged(Editable s) {
				if (isOutOfBounds) {
					UiHelper.ShowOneToast(ShortCutActivity.this, "字符超过了");
					if (s.length() > inputNum) {
						s.delete(inputNum, s.length());
						end = inputNum;
					} else if (s.length() > 20 && s.length() <= inputNum) {
						s.delete(20, s.length());
						end = 20;
					}
					end = s.length();
					mActivity_shortcut_contentedt.setSelection(end);// 设置光标在最后
					mActivity_shortcut_remainwords_tv.setText(s.length() + "/150");
				}
			}
		});
	}

	/** 调用文件选择软件来选择文件 **/
	private void showImgFileChooser() {
		Intent picture = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(picture, PICTURE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == PICTURE) {
				Uri selectedImage = data.getData();
				String[] filePathColumns = { MediaStore.Images.Media.DATA };
				Cursor c = this.getContentResolver().query(selectedImage, filePathColumns, null, null, null);
				c.moveToFirst();
				int columnIndex = c.getColumnIndex(filePathColumns[0]);
				String picturePath = c.getString(columnIndex);
				c.close();
				// 获取图片并显示
				Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
				mActivity_shortcut_choosepicture_iv.setImageBitmap(bitmap);
			}
		}
	}

	@Override
	protected void processMessage(Message msg) {

	}

	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		
	}

}
