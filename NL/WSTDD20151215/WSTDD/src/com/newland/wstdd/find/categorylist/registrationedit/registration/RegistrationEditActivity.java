package com.newland.wstdd.find.categorylist.registrationedit.registration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.newland.wstdd.R;
import com.newland.wstdd.common.base.BaseFragmentActivity;
import com.newland.wstdd.common.bean.TddActivity;
import com.newland.wstdd.common.common.AppContext;
import com.newland.wstdd.common.resultlisterer.OnPostListenerInterface;
import com.newland.wstdd.common.tools.StringUtil;
import com.newland.wstdd.common.tools.UiHelper;
import com.newland.wstdd.common.widget.PengTextView;
import com.newland.wstdd.find.categorylist.registrationedit.beanrequest.AdultInfo;
import com.newland.wstdd.find.categorylist.registrationedit.beanrequest.CancelRegistrationReq;
import com.newland.wstdd.find.categorylist.registrationedit.beanrequest.MainSignAttr;
import com.newland.wstdd.find.categorylist.registrationedit.beanrequest.SubmitRegistrationReq;
import com.newland.wstdd.find.categorylist.registrationedit.beanresponse.CancelRegistrationRes;
import com.newland.wstdd.find.categorylist.registrationedit.beanresponse.EditRegistrationRes;
import com.newland.wstdd.find.categorylist.registrationedit.beanresponse.SubmitRegistrationRes;
import com.newland.wstdd.find.categorylist.registrationedit.handle.CancelRegistrationHandle;
import com.newland.wstdd.find.categorylist.registrationedit.handle.SubmitRegistrationHandle;
import com.newland.wstdd.login.RetMsg;
import com.newland.wstdd.netutils.BaseMessageMgr;
import com.newland.wstdd.netutils.HandleNetMessageMgr;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

/**
 * 报名信息编辑
 * 
 * @author Administrator
 * 
 */
public class RegistrationEditActivity extends BaseFragmentActivity implements OnPostListenerInterface , IWXAPIEventHandler{
	Intent intent;
	private String mainSignAttrs;// 必填信息项目 是一个以逗号隔开的字符串
	TddActivity tddActivity;// 得到前面一个界面传递过来的活动对象 是为了获取必填项
	// 随行人员 ListView相关信息
	SxRegistrationEditListViews sxListViews;
	SxRegistrationEditAdapter sxAdapter;
	List<SxRegistrationEditAdapterData> sxAdapterDatas = new ArrayList<SxRegistrationEditAdapterData>();
	// 本人信息
	private List<MainSignAttr> mineAdapterDatas = new ArrayList<MainSignAttr>();
	RegistrationEditAdapter mineEditAdapter;// 我自己的报名信息的适配器
	SxRegistrationEditListViews mineEditListViews;
	// private List<String> mineAdapterDatas = new ArrayList<String>();
	// 添加随行人员
	TextView addTextView;// 添加随行人员 带有监听事件
	TextView registrationActivityIcon,registrationActivityTitle;//活动报名中的活动类型图标  跟   活动标题
	// 暂时的测试 服务器返回的信息
	EditRegistrationRes submitRegistrationRes;
	SubmitRegistrationHandle handler = new SubmitRegistrationHandle(this);

	CancelRegistrationRes cancelRegistrationRes;
	CancelRegistrationHandle handlerCancel = new CancelRegistrationHandle(this);
	/**
	 * 分享相关的
	 */
	private PopupWindow popupWindow;// 分享窗口
	// 微信
	private static final String appid = "wx1b84c30d9f380c89";// 微信的appid
	private IWXAPI wxApi;// 微信的API
	// QQ
	private Tencent mTencent;
	private static final String APP_ID = "1104957952";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_registration_edit);
		intent = getIntent();
		Bundle bundle = intent.getExtras();
		tddActivity = (TddActivity) bundle.getSerializable("tddActivity");
		initTitle();// 初始化标题
		initView();// 初始化控件
		initMustSelect();// 获取必填项目
//		 test();
		/**
		 * 分享
		 */
		// QQ
		final Context ctxContext = this.getApplicationContext();
		mTencent = Tencent.createInstance(APP_ID, ctxContext);
		mHandler = new Handler();
		// weixin
		wxApi = WXAPIFactory.createWXAPI(this, appid);
		wxApi.registerApp(appid);
		refreshCancelReg();// 取消报名
	}

	// 显示出必填的项目
	private void initMustSelect() {
		// TODO Auto-generated method stub
		if (tddActivity != null) {
			mainSignAttrs = tddActivity.getSignAttr();
			if (mainSignAttrs != null && !"".equals(mainSignAttrs)) {
				String[] strs = mainSignAttrs.split(",");
				// 把String数组输入list
				for (String substr : strs) {
					MainSignAttr mainSignAttr = new MainSignAttr();
					mainSignAttr.setName(substr);
					if ("姓名".equals(substr)) {
						mainSignAttr.setValue(tddActivity.getUserName());
					} else if ("手机".equals(substr)) {
						mainSignAttr.setValue(tddActivity.getUserMobilePhone());
					} else {
						mainSignAttr.setValue(null);
					}
					// mainSignAttr.setValue(null);
					mineAdapterDatas.add(mainSignAttr);
				}
				mineEditAdapter.setRegistrationData(mineAdapterDatas);
				mineEditListViews.setAdapter(mineEditAdapter);
				mineEditAdapter.notifyDataSetChanged();
			} else {
				MainSignAttr mainSignAttr = new MainSignAttr();
				mainSignAttr.setName("姓名");
				mainSignAttr.setValue(tddActivity.getUserName());
				MainSignAttr mainSignAttr1 = new MainSignAttr();
				mainSignAttr1.setName("手机");
				mainSignAttr1.setValue(tddActivity.getUserMobilePhone());
				mineAdapterDatas.add(mainSignAttr);
				mineAdapterDatas.add(mainSignAttr1);
				mineEditAdapter.setRegistrationData(mineAdapterDatas);
				mineEditListViews.setAdapter(mineEditAdapter);
				mineEditAdapter.notifyDataSetChanged();
			}
		}

	}

	/**
	 * 设置标题
	 * 
	 */
	private void initTitle() {
		ImageView leftBtn = (ImageView) findViewById(R.id.head_left_iv);
		TextView centerTitle = (TextView) findViewById(R.id.head_center_title);
		centerTitle.setText("编辑报名信息");
		TextView rightTv = (TextView) findViewById(R.id.head_right_tv);
		ImageButton rightBtn = (ImageButton) findViewById(R.id.head_right_btn);
		leftBtn.setVisibility(View.VISIBLE);	
		rightTv.setVisibility(View.VISIBLE);
		rightTv.setTextColor(getResources().getColor(R.color.text_red));
		rightTv.setText("提交");
		rightBtn.setVisibility(View.GONE);
		rightBtn.setImageDrawable(getResources().getDrawable(R.drawable.find));
		leftBtn.setOnClickListener(this);
		rightTv.setOnClickListener(this);
	}

	private void test() {
		// TODO Auto-generated method stub
		for (int i = 0; i < 2; i++) {
			SxRegistrationEditAdapterData data = new SxRegistrationEditAdapterData();
			data.setName("李山川");
			data.setPhone("18750736798");
			List<Map<String, String>> maplist = new ArrayList<Map<String, String>>();
			Map<String, String> map1 = new HashMap<String, String>();
			map1.put("姓名", "李山川" + i);
			Map<String, String> map3 = new HashMap<String, String>();
			map3.put("手机", "18750736798");
			maplist.add(map1);
			maplist.add(map3);
			data.setMap(maplist);
			data.setShowRl1(true);
			data.setShowRl2(false);
			data.setShowListView(false);
			data.setInParent(i);
			sxAdapterDatas.add(data);
		}
		/**
		 * 这里只是用来测试的，下面的语句需要在真实数据进行填写
		 */
		sxAdapter.setRegistrationData(sxAdapterDatas);
		sxListViews.setAdapter(sxAdapter);
		sxAdapter.notifyDataSetChanged();
	}

	public void initView() {
		// TODO Auto-generated method stub
		registrationActivityIcon = (TextView) findViewById(R.id.activity_mine_personalcenter_icon_tv);
		registrationActivityTitle = (TextView) findViewById(R.id.activity_mine_personalcenter_title_tv);
		registrationActivityIcon.setText(StringUtil.intType2Str(tddActivity.getActivityType()));
		registrationActivityTitle.setText(tddActivity.getActivityTitle());
		sxListViews = (SxRegistrationEditListViews) findViewById(R.id.registration_sx_listview);
		mineEditListViews = (SxRegistrationEditListViews) findViewById(R.id.registration_listview);
		sxAdapter = new SxRegistrationEditAdapter(this, sxAdapterDatas);
		sxListViews.setAdapter(sxAdapter);
		mineEditAdapter = new RegistrationEditAdapter(this, mineAdapterDatas);
		addTextView = (TextView) findViewById(R.id.registration_add_people);
		addTextView.setOnClickListener(this);
	}

	// 监听事件
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		switch (v.getId()) {
		case R.id.registration_add_people:
			// 进行判断，只有添加完最后一个才可以进行下一个的判断
			if (sxAdapterDatas.size() > 0) {
				if (sxAdapterDatas.get(sxAdapterDatas.size() - 1).getName() == null || sxAdapterDatas.get(sxAdapterDatas.size() - 1).getPhone() == null
						|| "".equals(sxAdapterDatas.get(sxAdapterDatas.size() - 1).getName()) || "".equals(sxAdapterDatas.get(sxAdapterDatas.size() - 1).getPhone())) {
					UiHelper.ShowOneToast(this, "随行人员信息未完成，无法添加11111");
				} else {

					SxRegistrationEditAdapterData data = new SxRegistrationEditAdapterData();
					data.setName(null);
					data.setPhone(null);
					List<Map<String, String>> maplist = new ArrayList<Map<String, String>>();
					List<Map<String, String>> tempInputMaplist = new ArrayList<Map<String, String>>();
					List<Map<String, String>> tempLastMaplist = new ArrayList<Map<String, String>>();
					if (tddActivity != null) {
						mainSignAttrs = tddActivity.getSignAttr();
						if (mainSignAttrs != null && !"".equals(mainSignAttrs)) {
							String[] strs = mainSignAttrs.split(",");
							// 把String数组输入list
							for (String substr : strs) {
								Map<String, String> map = new HashMap<String, String>();
								map.put(substr, null);
								maplist.add(map);
							}
							for (String substr : strs) {
								Map<String, String> map = new HashMap<String, String>();
								map.put(substr, null);
								tempInputMaplist.add(map);
							}
							for (String substr : strs) {
								Map<String, String> map = new HashMap<String, String>();
								map.put(substr, null);
								tempLastMaplist.add(map);
							}
						}
						
						
						
					} else {
						UiHelper.ShowOneToast(RegistrationEditActivity.this, "活动信息为空，无法添加随行人员???");
					}
					data.setMap(maplist);
					data.setInputTempList(tempInputMaplist);
					data.setLastTempList(tempLastMaplist);
					data.setShowRl1(false);
					data.setShowRl2(true);
					data.setShowListView(true);
					data.setInParent(sxAdapterDatas.size());
					sxAdapterDatas.add(data);
					sxAdapter.setRegistrationData(sxAdapterDatas);
					sxAdapter.notifyDataSetChanged();
				}
			} else {
				// 首次添加随行人员的时候，列表是为0开始的
				SxRegistrationEditAdapterData data = new SxRegistrationEditAdapterData();
				data.setName(null);
				data.setPhone(null);
				List<Map<String, String>> maplist = new ArrayList<Map<String, String>>();
				List<Map<String, String>> tempInputMaplist = new ArrayList<Map<String, String>>();
				List<Map<String, String>> tempLastMaplist = new ArrayList<Map<String, String>>();
				if (tddActivity != null) {
					mainSignAttrs = tddActivity.getSignAttr();
					if (mainSignAttrs != null && !"".equals(mainSignAttrs)) {
						String[] strs = mainSignAttrs.split(",");
						// 把String数组输入list
						for (String substr : strs) {
							Map<String, String> map = new HashMap<String, String>();
							map.put(substr, null);
							maplist.add(map);
						}
						for (String substr : strs) {
							Map<String, String> map = new HashMap<String, String>();
							map.put(substr, null);
							tempInputMaplist.add(map);
						}
						for (String substr : strs) {
							Map<String, String> map = new HashMap<String, String>();
							map.put(substr, null);
							tempLastMaplist.add(map);
						}
					}
				} else {
					UiHelper.ShowOneToast(RegistrationEditActivity.this, "活动信息为空，无法添加随行人员");
				}
				data.setMap(maplist);
				data.setInputTempList(tempInputMaplist);
				data.setLastTempList(tempLastMaplist);
				data.setShowRl1(false);
				data.setShowRl2(true);
				data.setShowListView(true);
				data.setInParent(0);// 首次添加随行人员的时候，
				sxAdapterDatas.add(data);
				sxAdapter.setRegistrationData(sxAdapterDatas);
				sxAdapter.notifyDataSetChanged();

			}
			break;
		case R.id.head_right_tv:
			
			refreshSumit();
			break;
		default:
			break;
		}

	}

	@Override
	protected void processMessage(Message msg) {

	}

	@Override
	public void refresh() {

	}

	@Override
	public void OnHandleResultListener(Object obj, int responseId) {
		try {
			if (dialog != null) {
				dialog.dismiss();
			}
			switch (responseId) {
			case SubmitRegistrationHandle.SUBMIT_REGISTRATION:
				submitRegistrationRes = (EditRegistrationRes) obj;
				if (submitRegistrationRes != null) {
					String mess = null;
					mess = submitRegistrationRes.getGetResMess();
					/**
					 * 返回成功之后进行分享
					 */
					getPopWindow();
					
					
					
					
					
					
				}
				break;

			default:
				break;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override
	public void OnFailResultListener(String mess) {if (dialog != null) {dialog.dismiss();}
		// TODO Auto-generated method stub

	}

	/**
	 * 请求服务器信息  提交报名
	 */
	private void refreshSumit() {
		super.refresh();
		try {
			new Thread() {
				public void run() {
					// 需要发送一个request的对象进行请求
					SubmitRegistrationReq reqInfo = new SubmitRegistrationReq();
					// 主报名信息
					reqInfo.setMainSignAttr(mineAdapterDatas);
					// 随行人员报名信息
					List<AdultInfo> adultInfos = new ArrayList<AdultInfo>();
					for (int i = 0; i < sxAdapterDatas.size(); i++) {
						// 添加到adultInfos
						AdultInfo adultInfo = new AdultInfo();
						adultInfo.setAdultPersonType(2);
						adultInfo.setAdultUserName(sxAdapter.getRegistrationData().get(i).getName());
						List<MainSignAttr> mainSignAttrs = new ArrayList<MainSignAttr>();
						List<Map<String, String>> values = sxAdapter.getRegistrationData().get(i).getMap();
						for (Map<String, String> map : values) {
							for (Entry<String, String> entry : map.entrySet()) {
								Object key = entry.getKey();
								Object val = entry.getValue();
								MainSignAttr mainSignAttr = new MainSignAttr();
								if (!"".equals((String) key) && key != null) {
									mainSignAttr.setName((String) key);
								}
								if (!"".equals((String) val) && val != null) {
									mainSignAttr.setValue((String) val);
								}
								mainSignAttrs.add(mainSignAttr);
							}
						}
						adultInfo.setAdultSignAttr(mainSignAttrs);
						adultInfos.add(adultInfo);
					}
					reqInfo.setAdultInfos(adultInfos);
					// 人员类型
					reqInfo.setPersonType(1);
					BaseMessageMgr mgr = new HandleNetMessageMgr();
					RetMsg<SubmitRegistrationRes> ret = mgr.getSubmitRegistrationInfo(reqInfo, tddActivity.getActivityId());// 泛型类，
					Message message = new Message();
					message.what = SubmitRegistrationHandle.SUBMIT_REGISTRATION;// 设置死
					// 访问服务器成功 1 否则访问服务器失败
					if (ret.getCode() == 1) {
						submitRegistrationRes = new EditRegistrationRes();
						submitRegistrationRes.setGetResMess(StringUtil.noNull(ret.getMsg()));
						message.obj = submitRegistrationRes;
					} else {
						message.obj = ret.getMsg();
					}
					handler.sendMessage(message);
				};
			}.start();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	/**
	 * 取消报名
	 */
	private void refreshCancelReg() {
		super.refresh();
		try {
			new Thread() {
				public void run() {
					// 需要发送一个request的对象进行请求
					CancelRegistrationReq reqInfo = new CancelRegistrationReq();
					reqInfo.setActivityId("activityID");
					BaseMessageMgr mgr = new HandleNetMessageMgr();
					RetMsg<CancelRegistrationRes> ret = mgr.getCancelRegistrationInfo(reqInfo);// 泛型类，
					Message message = new Message();
					message.what = CancelRegistrationHandle.CANCEL_REGISTRATION;// 设置死
					// 访问服务器成功 1 否则访问服务器失败
					if (ret.getCode() == 1) {
						cancelRegistrationRes = new CancelRegistrationRes();
						cancelRegistrationRes.setGetResMess(StringUtil.noNull(ret.getMsg()));
						message.obj = cancelRegistrationRes;
					} else {
						message.obj = ret.getMsg();
					}
					handlerCancel.sendMessage(message);
				};
			}.start();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	
	
	
	/**
	 * 分享相关的操作
	 */
	
	private void getPopWindow() {
		if (popupWindow != null) {
			popupWindow.dismiss();
			return;
		} else {
			initPopupWindow();
		}
	}
	
	/**
	 * 分享窗口初始化
	 */
	private void initPopupWindow() {
		View popupWindow_view = this.getLayoutInflater().inflate(
				R.layout.popwindow_registration_shares, null, false);
		popupWindow = new PopupWindow(popupWindow_view, AppContext
				.getAppContext().getScreenWidth() * 4 / 5,
				LayoutParams.WRAP_CONTENT, true);
		ImageView cancelImageView = (ImageView) popupWindow_view
				.findViewById(R.id.originate_chair_popwindow_delete);
		cancelImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				popupWindow.dismiss();
				Intent intent = new Intent();
				intent.setAction("Refresh_FindChairDetailActivity");
				sendBroadcast(intent);
				finish();
				
				
				
			}
		});
		popupWindow.setOutsideTouchable(false);
		PengTextView weixinFriend, weixinZone, qqFriend;// 弹出窗口三个控件
		weixinFriend = (PengTextView) popupWindow_view
				.findViewById(R.id.activity_find_chairdetail_pop_weixin);
		weixinZone = (PengTextView) popupWindow_view
				.findViewById(R.id.activity_find_chairdetail_pop_weixin_zone);
		qqFriend = (PengTextView) popupWindow_view
				.findViewById(R.id.activity_find_chairdetail_pop_qq);
		// 微信好友分享
		weixinFriend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				friend(v);
			}
		});

		// 微信朋友圈分享
		weixinZone.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				friendline(v);
			}
		});

		// qq好友分享
		qqFriend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onClickShareToQQ();
			}
		});

		// 设置透明度
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.alpha = 0.3f;
		getWindow().setAttributes(lp);
		popupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				WindowManager.LayoutParams lp = getWindow().getAttributes();
				lp.alpha = 1f;
				getWindow().setAttributes(lp);
			}
		});
		// 显示窗口 位置
		popupWindow.showAtLocation(this.findViewById(R.id.layout),
				Gravity.CENTER, 0, 0);//
		popupWindow.setOutsideTouchable(false);

	}
	/**
	 * 分享相关的方法
	 * @param v
	 */
	public void friend(View v) {
		share(0);
	}

	public void friendline(View v) {
		share(1);
	}

	private void share(int flag) {
		downloadWeiXinImg(flag);

	}
	
	// 微信分享需要 先去下载封面的图片，然后才会分享出去
	private void downloadWeiXinImg(final int flag) {
		// TODO Auto-generated method stub
		if (tddActivity.getShareContent() != null
				&& tddActivity.getShareImg() != null
				&& tddActivity.getShareUrl() != null) {

			ImageLoader.getInstance().loadImage(tddActivity.getShareImg(),
					new ImageLoadingListener() {

						@Override
						public void onLoadingStarted(String arg0, View arg1) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onLoadingFailed(String arg0, View arg1,
								FailReason arg2) {
							// TODO Auto-generated method stub
							// 下载失败
							WXWebpageObject webpage = new WXWebpageObject();
							webpage.webpageUrl = tddActivity.getShareUrl();
							WXMediaMessage msg = new WXMediaMessage(webpage);
							msg.title = tddActivity.getActivityTitle();
							msg.description = tddActivity
									.getActivityDescription();
							// 根据ImgUrl下载下来一张图片，弄出bitmap格式
							// 这里替换一张自己工程里的图片资源
							Bitmap thumb = BitmapFactory.decodeResource(
									getResources(), R.drawable.ic_launcher);
							msg.setThumbImage(thumb);
							SendMessageToWX.Req req = new SendMessageToWX.Req();
							req.transaction = buildTransaction("webpage");
							req.message = msg;
							req.scene = flag == 0 ? SendMessageToWX.Req.WXSceneSession
									: SendMessageToWX.Req.WXSceneTimeline;
							boolean fla = wxApi.sendReq(req);
							System.out.println("fla=" + fla);
						}

						@Override
						public void onLoadingComplete(String arg0, View arg1,
								Bitmap bitmap) {
							// TODO Auto-generated method stub
							// 表示下载成功了
							WXWebpageObject webpage = new WXWebpageObject();
							webpage.webpageUrl = tddActivity.getShareUrl();
							WXMediaMessage msg = new WXMediaMessage(webpage);
							msg.title = tddActivity.getActivityTitle();
							msg.description = tddActivity
									.getActivityDescription();
							// 根据ImgUrl下载下来一张图片，弄出bitmap格式
							// 这里替换一张自己工程里的图片资源
							Bitmap thumb = bitmap;
							msg.setThumbImage(thumb);
							SendMessageToWX.Req req = new SendMessageToWX.Req();
							req.transaction = buildTransaction("webpage");
							req.message = msg;
							req.scene = flag == 0 ? SendMessageToWX.Req.WXSceneSession
									: SendMessageToWX.Req.WXSceneTimeline;
							boolean fla = wxApi.sendReq(req);
							System.out.println("fla=" + fla);
						}

						@Override
						public void onLoadingCancelled(String arg0, View arg1) {
							// TODO Auto-generated method stub

						}
					});
		} else {
			UiHelper.ShowOneToast(RegistrationEditActivity.this,
					"第三方分享的内容不能为空！！！");
			finish();
		}
	}
	
	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis())
				: type + System.currentTimeMillis();
	}

	@Override
	public void onReq(BaseReq arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResp(BaseResp arg0) {
		// TODO Auto-generated method stub
		finish();
	}

	private void onClickShareToQQ() {
		Bundle b = getShareBundle();
		if (b != null) {
			shareParams = b;
			Thread thread = new Thread(shareThread);
			thread.start();
		}
	}

	private Bundle getShareBundle() {
		Bundle bundle = new Bundle();
		bundle.putString("title", tddActivity.getActivityTitle());
		bundle.putString("imageUrl", tddActivity.getShareImg());
		bundle.putString("targetUrl", tddActivity.getShareUrl());
		bundle.putString("summary", tddActivity.getActivityDescription());
		bundle.putString("site", "1104957952");
		bundle.putString("appName", "我是TDD");
		return bundle;
	}

	Bundle shareParams = null;

	Handler shareHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
		}
	};

	// 线程类，该类使用匿名内部类的方式进行声明
	Runnable shareThread = new Runnable() {

		public void run() {
			doShareToQQ(shareParams);
			Message msg = shareHandler.obtainMessage();

			// 将Message对象加入到消息队列当中
			shareHandler.sendMessage(msg);

		}
	};

	private void doShareToQQ(Bundle params) {
		mTencent.shareToQQ(RegistrationEditActivity.this, params,
				new BaseUiListener() {
					protected void doComplete(JSONObject values) {
						showResult("shareToQQ:", "onComplete");
					}

					@Override
					public void onError(UiError e) {
						showResult("shareToQQ:", "分享失败");
					}

					@Override
					public void onCancel() {
						showResult("shareToQQ", "onCancel");
					}
				});
	}

	private class BaseUiListener implements IUiListener {

		// @Override
		// public void onComplete(JSONObject response) {
		// // mBaseMessageText.setText("onComplete:");
		// // mMessageText.setText(response.toString());
		// doComplete(response);
		// }

		protected void doComplete(Object values) {

		}

		@Override
		public void onError(UiError e) {
			showResult("onError:", "code:" + e.errorCode + ", msg:"
					+ e.errorMessage + ", detail:" + e.errorDetail);
		}

		@Override
		public void onCancel() {
			showResult("onCancel", "");
		}

		@Override
		public void onComplete(Object arg0) {
			// TODO Auto-generated method stub
			doComplete(arg0);
		}
	}

	private Handler mHandler;

	// qq分享的结果处理
	private void showResult(final String base, final String msg) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				UiHelper.ShowOneToast(RegistrationEditActivity.this, msg);
				popupWindow.dismiss();
				finish();
			}
		});
	}

	
	
	
	
	
}
