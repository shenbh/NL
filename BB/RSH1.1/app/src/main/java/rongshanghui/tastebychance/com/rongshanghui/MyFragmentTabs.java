package rongshanghui.tastebychance.com.rongshanghui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.mobileim.YWAPI;
import com.alibaba.mobileim.YWIMKit;
import com.alibaba.mobileim.channel.util.WxLog;
import com.alibaba.mobileim.channel.util.YWLog;
import com.alibaba.mobileim.conversation.IYWConversationService;
import com.alibaba.mobileim.conversation.IYWConversationUnreadChangeListener;
import com.alibaba.mobileim.conversation.IYWMessageLifeCycleListener;
import com.alibaba.mobileim.conversation.IYWSendMessageToContactInBlackListListener;
import com.alibaba.mobileim.conversation.YWConversation;
import com.alibaba.mobileim.conversation.YWConversationType;
import com.alibaba.mobileim.conversation.YWMessage;
import com.alibaba.mobileim.conversation.YWMessageChannel;
import com.alibaba.mobileim.conversation.YWMessageType;
import com.alibaba.mobileim.gingko.model.tribe.YWTribe;
import com.alibaba.mobileim.gingko.model.tribe.YWTribeMember;
import com.alibaba.mobileim.gingko.presenter.tribe.IYWTribeChangeListener;
import com.alibaba.mobileim.login.IYWConnectionListener;
import com.alibaba.mobileim.login.YWLoginCode;
import com.alibaba.mobileim.login.YWLoginState;
import com.alibaba.mobileim.utility.IMNotificationUtils;
import com.alibaba.mobileim.utility.UserContext;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

import rongshanghui.tastebychance.com.rongshanghui.alibaba.alibaba.openIMUIDemo.LoginActivity;
import rongshanghui.tastebychance.com.rongshanghui.alibaba.openimui.demo.MoreFragment;
import rongshanghui.tastebychance.com.rongshanghui.alibaba.openimui.demo.TribeFragment;
import rongshanghui.tastebychance.com.rongshanghui.alibaba.openimui.sample.CustomConversationHelper;
import rongshanghui.tastebychance.com.rongshanghui.alibaba.openimui.sample.LoginSampleHelper;
import rongshanghui.tastebychance.com.rongshanghui.alibaba.openimui.tribe.TribeConstants;
import rongshanghui.tastebychance.com.rongshanghui.bean.EventToContactList;
import rongshanghui.tastebychance.com.rongshanghui.bean.EventToConversation;
import rongshanghui.tastebychance.com.rongshanghui.common.AppManager;
import rongshanghui.tastebychance.com.rongshanghui.home.HomeFragment;
import rongshanghui.tastebychance.com.rongshanghui.mime.MineFragment;
import rongshanghui.tastebychance.com.rongshanghui.news.NewsFragment;
import rongshanghui.tastebychance.com.rongshanghui.util.Constants;
import rongshanghui.tastebychance.com.rongshanghui.util.EventBusUtils;
import rongshanghui.tastebychance.com.rongshanghui.util.PackageManagerUtils;

/**
 * Created by mayongge on 15-9-22.
 */
public class MyFragmentTabs extends FragmentActivity {

    public static final String TAG = "FragmentTabs";
    public static final String LOGIN_SUCCESS = "loginSuccess";

    public static final String TAB_MESSAGE = "message";
    public static final String TAB_CONTACT = "contact";
    public static final String TAB_TRIBE = "tribe";
    public static final String TAB_MORE = "more";

    private TextView mMessageTab;
    private TextView mContactTab;
    private TextView mTribeTab;
    private TextView mMoreTab;
    private TextView mUnread;

    private Drawable mMessagePressed;
    private Drawable mMessageNormal;
    private Drawable mFriendPressed;
    private Drawable mFriendNormal;
    private Drawable mTribePressed;
    private Drawable mTribeNormal;
    private Drawable mMorePressed;
    private Drawable mMoreNormal;

    private FragmentTabHost mTabHost;
    private YWIMKit mIMKit;
    private IYWConversationService mConversationService;
    private IYWConversationUnreadChangeListener mConversationUnreadChangeListener;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    private IYWTribeChangeListener mTribeChangedListener;

    private IYWMessageLifeCycleListener mMessageLifeCycleListener;
    private IYWSendMessageToContactInBlackListListener mSendMessageToContactInBlackListListener;
    private IYWConnectionListener mConnectionListener;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppManager.getAppManager().addActivity(this);
        mContext = this;
        mIMKit = LoginSampleHelper.getInstance().getIMKit();
        if (mIMKit == null) {
            return;
        }
        mConversationService = mIMKit.getConversationService();
        initListeners();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.demo_fragment_tabs);

        // create our manager instance after the content view is set
        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        // enable status bar tint
        tintManager.setStatusBarTintEnabled(true);
        // enable navigation bar tint
        tintManager.setNavigationBarTintEnabled(true);
        // set a custom tint color for all system bars
//        tintManager.setTintColor(Color.parseColor("#990000FF"));
        tintManager.setTintColor(getResources().getColor(R.color.colorPrimaryDark));

        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);

        resetTab();

        mUnread = (TextView) findViewById(R.id.unread);
        mTabHost.setOnTabChangedListener(listener);
        listener.onTabChanged(TAB_MESSAGE);
    }

    private void resetTab() {
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        Bundle fragmentBundle = new Bundle();
        fragmentBundle.putSerializable(UserContext.EXTRA_USER_CONTEXT_KEY, mIMKit.getUserContext());

        View indicator = getIndicatorView(TAB_MESSAGE);
        mTabHost.addTab(mTabHost.newTabSpec(TAB_MESSAGE).setIndicator(indicator), HomeFragment.class, fragmentBundle);

        indicator = getIndicatorView(TAB_CONTACT);
        mTabHost.addTab(mTabHost.newTabSpec(TAB_CONTACT).setIndicator(indicator), mIMKit.getConversationFragmentClass(), fragmentBundle);

        indicator = getIndicatorView(TAB_TRIBE);
        mTabHost.addTab(mTabHost.newTabSpec(TAB_TRIBE).setIndicator(indicator), NewsFragment.class, fragmentBundle);

        indicator = getIndicatorView(TAB_MORE);
        mTabHost.addTab(mTabHost.newTabSpec(TAB_MORE).setIndicator(indicator), MineFragment.class, fragmentBundle);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void intentToContact(@NonNull EventToContactList eventToContactList) {
        if (null != mTabHost) {
            mTabHost.clearAllTabs();
        }
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);

        Bundle fragmentBundle = new Bundle();
        fragmentBundle.putSerializable(UserContext.EXTRA_USER_CONTEXT_KEY, mIMKit.getUserContext());

        View indicator = getIndicatorView(TAB_MESSAGE);
        mTabHost.addTab(mTabHost.newTabSpec(TAB_MESSAGE).setIndicator(indicator), HomeFragment.class, fragmentBundle);

        indicator = getIndicatorView(TAB_CONTACT);
        mTabHost.addTab(mTabHost.newTabSpec(TAB_CONTACT).setIndicator(indicator), mIMKit.getContactsFragmentClass(), fragmentBundle);

        indicator = getIndicatorView(TAB_TRIBE);
        mTabHost.addTab(mTabHost.newTabSpec(TAB_TRIBE).setIndicator(indicator), NewsFragment.class, fragmentBundle);

        indicator = getIndicatorView(TAB_MORE);
        mTabHost.addTab(mTabHost.newTabSpec(TAB_MORE).setIndicator(indicator), MineFragment.class, fragmentBundle);

        mTabHost.setCurrentTab(1);
    }

  /*  @Subscribe(threadMode = ThreadMode.MAIN)
    public void intentToConversation(@NonNull EventToConversation eventToConversation) {
        if (null != mTabHost){
            mTabHost.clearAllTabs();
        }
        resetTab();
        mTabHost.setCurrentTab(1);
    }*/

    public static final String SYSTEM_TRIBE_CONVERSATION = "sysTribe";
    public static final String SYSTEM_FRIEND_REQ_CONVERSATION = "sysfrdreq";
    public static final String RELATED_ACCOUNT = "relatedAccount";

    /**
     * 自定义会话示例展示系统通知的示例
     */
    private void initCustomConversation() {
        CustomConversationHelper.addCustomConversation(SYSTEM_FRIEND_REQ_CONVERSATION, null);
    }

    /**
     * 初始化相关监听
     */
    private void initListeners() {
        //初始化并添加会话变更监听
        initConversationServiceAndListener();
        //初始化自定义会话
        initCustomConversation();
        //设置发送消息生命周期监听
        setMessageLifeCycleListener();
        //设置发送消息给黑名单中的联系人监听
        setSendMessageToContactInBlackListListener();
        //添加IM连接状态监听
        addConnectionListener();
    }

    TabHost.OnTabChangeListener listener = new TabHost.OnTabChangeListener() {
        @Override
        public void onTabChanged(String tabId) {
            if (TAB_MESSAGE.equals(tabId)) {
                setMessageText(true);
                setContactText(false);
                setTribeText(false);
                setMoreText(false);
                return;
            }
            if (TAB_CONTACT.equals(tabId)) {
                setMessageText(false);
                setContactText(true);
                setTribeText(false);
                setMoreText(false);
                return;
            }
            if (TAB_TRIBE.equals(tabId)) {
                setMessageText(false);
                setContactText(false);
                setTribeText(true);
                setMoreText(false);
                return;
            }
            if (TAB_MORE.equals(tabId)) {
                setMessageText(false);
                setContactText(false);
                setTribeText(false);
                setMoreText(true);
                return;
            }
        }
    };

    private View getIndicatorView(String tab) {
        View tabView = View.inflate(this, R.layout.demo_tab_item, null);
        TextView indicator = (TextView) tabView.findViewById(R.id.tab_text);
        Drawable drawable;

        if (tab.equals(TAB_MESSAGE)) {
            indicator.setText("首页");
            drawable = getResources().getDrawable(R.drawable.tab_home_unselected);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            indicator.setCompoundDrawables(null, drawable, null, null);
            mMessageTab = indicator;
        } else if (tab.equals(TAB_CONTACT)) {
            indicator.setText("名片夹");
            drawable = getResources().getDrawable(R.drawable.tab_addresslist_unselected);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            indicator.setCompoundDrawables(null, drawable, null, null);
            mContactTab = indicator;
        } else if (tab.equals(TAB_TRIBE)) {
            indicator.setText("消息");
            drawable = getResources().getDrawable(R.drawable.tab_news_unselected);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            indicator.setCompoundDrawables(null, drawable, null, null);
            mTribeTab = indicator;
        } else if (tab.equals(TAB_MORE)) {
            indicator.setText("我的");
            drawable = getResources().getDrawable(R.drawable.tab_person_unselected);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            indicator.setCompoundDrawables(null, drawable, null, null);
            mMoreTab = indicator;
        }
        return tabView;
    }

    private void initConversationServiceAndListener() {
        mConversationUnreadChangeListener = new IYWConversationUnreadChangeListener() {

            //当未读数发生变化时会回调该方法，开发者可以在该方法中更新未读数
            @Override
            public void onUnreadChange() {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        LoginSampleHelper loginHelper = LoginSampleHelper.getInstance();
                        final YWIMKit imKit = loginHelper.getIMKit();
                        mConversationService = imKit.getConversationService();
                        //获取当前登录用户的所有未读数
                        int unReadCount = mConversationService.getAllUnreadCount();
                        //设置桌面角标的未读数
                        mIMKit.setShortcutBadger(unReadCount);
                        if (unReadCount > 0) {
                            mUnread.setVisibility(View.VISIBLE);
                            if (unReadCount < 100) {
                                mUnread.setText(unReadCount + "");
                            } else {
                                mUnread.setText("99+");
                            }
                        } else {
                            mUnread.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        };
        mConversationService.addTotalUnreadChangeListener(mConversationUnreadChangeListener);
    }


    private void setMessageLifeCycleListener() {
        mMessageLifeCycleListener = new IYWMessageLifeCycleListener() {
            /**
             * 发送消息前回调
             * @param conversation 当前消息所在会话
             * @param message      当前将要发送的消息
             * @return 需要发送的消息，若为null，则表示不发送消息
             */
            @Override
            public YWMessage onMessageLifeBeforeSend(YWConversation conversation, YWMessage message) {
                //todo 以下代码仅仅是示例，开发者无需按照以下方式设置，应该根据自己的需求对消息进行修改
                String cvsType = "单聊";
                if (conversation.getConversationType() == YWConversationType.Tribe) {
                    cvsType = "群聊：";
                }
                String msgType = "文本消息";
                if (message.getSubType() == YWMessage.SUB_MSG_TYPE.IM_IMAGE) {
                    msgType = "图片消息";
                } else if (message.getSubType() == YWMessage.SUB_MSG_TYPE.IM_GEO) {
                    msgType = "地理位置消息";
                } else if (message.getSubType() == YWMessage.SUB_MSG_TYPE.IM_AUDIO) {
                    msgType = "语音消息";
                } else if (message.getSubType() == YWMessage.SUB_MSG_TYPE.IM_P2P_CUS || message.getSubType() == YWMessage.SUB_MSG_TYPE.IM_TRIBE_CUS) {
                    msgType = "自定义消息";
                }

                //TODO 设置APNS Push，如果开发者需要对APNS Push进行定制可以调用message.setPushInfo(YWPushInfo)方法进行设置，如果不需要APNS Push定制则不需要调用message.setPushInfo(YWPushInfo)方法
                //TODO Demo默认发送消息不需要APNS Push定制,所以注释掉以下两行代码
//                YWPushInfo pushInfo = new YWPushInfo(1, cvsType + msgType, "dingdong", "我是自定义数据");
//                message.setPushInfo(pushInfo);

                //根据消息类型对消息进行修改，切记这里只是示例，具体怎样对消息进行修改开发者可以根据自己的需求进行处理
                if (message.getSubType() == YWMessage.SUB_MSG_TYPE.IM_TEXT) {
                    String content = message.getContent();
                    if (content.equals("55")) {
                        message.setContent("我修改了消息内容, 原始内容：55");
                        return message;
                    } else if (content.equals("66")) {
                        YWMessage newMsg = YWMessageChannel.createTextMessage("我创建了一条新消息, 原始消息内容：66");
                        return newMsg;
                    } else if (content.equals("77")) {
                        IMNotificationUtils.getInstance().showToast(MyFragmentTabs.this, "不发送该消息，消息内容为：77");
                        return null;
                    }
                }
                return message;
            }

            /**
             * 发送消息结束后回调
             * @param message   当前发送的消息
             * @param sendState 消息发送状态，具体状态请参考{@link YWMessageType.SendState}
             */
            @Override
            public void onMessageLifeFinishSend(YWMessage message, YWMessageType.SendState sendState) {
//                IMNotificationUtils.getInstance().showToast(FragmentTabs.this, sendState.toString());
            }
        };
        mConversationService.setMessageLifeCycleListener(mMessageLifeCycleListener);
    }

    //设置连接状态的监听
    private void addConnectionListener() {
        mConnectionListener = new IYWConnectionListener() {
            @Override
            public void onDisconnect(int code, String info) {
                if (code == YWLoginCode.LOGON_FAIL_KICKOFF) {
                    //在其它终端登录，当前用户被踢下线
                    Toast.makeText(MyApplication.getContext(), "被踢下线", Toast.LENGTH_LONG).show();
                    YWLog.i("LoginSampleHelper", "被踢下线");
                    LoginSampleHelper.getInstance().setAutoLoginState(YWLoginState.idle);
                    finish();
                    Intent intent = new Intent(MyApplication.getContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MyApplication.getContext().startActivity(intent);
                }
            }

            @Override
            public void onReConnecting() {

            }

            @Override
            public void onReConnected() {

            }
        };
        mIMKit.getIMCore().addConnectionListener(mConnectionListener);
    }

    private void setSendMessageToContactInBlackListListener() {
        mSendMessageToContactInBlackListListener = new IYWSendMessageToContactInBlackListListener() {
            /**
             * 是否发送消息给黑名单中的联系人，当用户发送消息给黑名单中的联系人时我们会回调该接口
             * @param conversation 当前发送消息的会话
             * @param message      要发送的消息
             * @return true：发送  false：不发送
             */
            @Override
            public boolean sendMessageToContactInBlackList(YWConversation conversation, YWMessage message) {
                //TODO 开发者可用根据自己的需求决定是否要发送该消息，SDK默认不发送
                return true;
            }
        };
        mConversationService.setSendMessageToContactInBlackListListener(mSendMessageToContactInBlackListListener);
    }

    private void setMessageText(boolean isSelected) {
        Drawable drawable = null;
        if (isSelected) {
            mMessageTab.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            if (mMessagePressed == null) {
                mMessagePressed = getResources().getDrawable(R.drawable.tab_home_selected);
            }
            drawable = mMessagePressed;
        } else {
            mMessageTab.setTextColor(getResources().getColor(R.color.font_blue));
            if (mMessageNormal == null) {
                mMessageNormal = getResources().getDrawable(R.drawable.tab_home_unselected);
            }
            drawable = mMessageNormal;
        }
        if (drawable != null) {
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            mMessageTab.setCompoundDrawables(null, drawable, null, null);
        }
    }


    private void setContactText(boolean isSelected) {
        Drawable drawable = null;
        if (isSelected) {
            mContactTab.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            if (mFriendPressed == null) {
                mFriendPressed = getResources().getDrawable(R.drawable.tab_addresslist_selected);
            }
            drawable = mFriendPressed;
        } else {
            mContactTab.setTextColor(getResources().getColor(R.color.font_blue));
            if (mFriendNormal == null) {
                mFriendNormal = getResources().getDrawable(R.drawable.tab_addresslist_unselected);
            }
            drawable = mFriendNormal;
        }
        if (null != drawable) {// 此处出现过NP问题，加保护
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            mContactTab.setCompoundDrawables(null, drawable, null, null);
        }

    }

    private void setTribeText(boolean isSelected) {
        Drawable drawable = null;
        if (isSelected) {
            mTribeTab.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            if (mTribePressed == null) {
                mTribePressed = getResources().getDrawable(R.drawable.tab_news_selected);
            }
            drawable = mTribePressed;
        } else {
            mTribeTab.setTextColor(getResources().getColor(R.color.font_blue));
            if (mTribeNormal == null) {
                mTribeNormal = getResources().getDrawable(R.drawable.tab_news_unselected);
            }
            drawable = mTribeNormal;
        }
        if (null != drawable) {// 此处出现过NP问题，加保护
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            mTribeTab.setCompoundDrawables(null, drawable, null, null);
        }

    }

    private void setMoreText(boolean isSelected) {
        Drawable drawable = null;
        if (isSelected) {
            mMoreTab.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            if (mMorePressed == null) {
                mMorePressed = getResources().getDrawable(R.drawable.tab_person_selected);
            }
            drawable = mMorePressed;
        } else {
            mMoreTab.setTextColor(getResources().getColor(R.color.font_blue));
            if (mMoreNormal == null) {
                mMoreNormal = getResources().getDrawable(R.drawable.tab_person_unselected);
            }
            drawable = mMoreNormal;
        }
        if (null != drawable) {// 此处出现过NP问题，加保护
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            mMoreTab.setCompoundDrawables(null, drawable, null, null);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        YWLog.i(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();

        //resume时需要检查全局未读消息数并做处理，因为离开此界面时删除了全局消息监听器
        mConversationUnreadChangeListener.onUnreadChange();

        Intent intent = getIntent();
        if (intent != null && intent.getStringExtra(LOGIN_SUCCESS) != null) {
            listener.onTabChanged(TAB_MESSAGE);
            getIntent().removeExtra(LOGIN_SUCCESS);
        }
        if (YWAPI.getLoginAccountList().size() >= 2) {
            CustomConversationHelper.addCustomConversation(RELATED_ACCOUNT, null);
        }
        YWLog.i(TAG, "onResume");
    }

    @Override
    protected void onStart() {
        if (Constants.IS_DEVELOPING) {
            System.out.println(PackageManagerUtils.getRunningActivityName(this) + "----------------------------------------------------" + getTaskId());
        }

        EventBusUtils.register(this);
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        AppManager.getAppManager().finishActivity(this);
        EventBusUtils.unregister(this);
        YWLog.i(TAG, "onDestroy");
        super.onDestroy();

        if (mMessageNormal != null) {
            mMessageNormal.setCallback(null);
        }
        if (mMessagePressed != null) {
            mMessagePressed.setCallback(null);
        }
        if (mFriendNormal != null) {
            mFriendNormal.setCallback(null);
        }
        if (mFriendPressed != null) {
            mFriendPressed.setCallback(null);
        }

        if (mTribeNormal != null) {
            mTribeNormal.setCallback(null);
        }
        if (mTribePressed != null) {
            mTribePressed.setCallback(null);
        }
        if (mMoreNormal != null) {
            mMoreNormal.setCallback(null);
        }
        if (mMorePressed != null) {
            mMorePressed.setCallback(null);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        long tribeId = intent.getLongExtra(TribeConstants.TRIBE_ID, 0);
        if (tribeId != 0) {
            setIntent(intent);
            mTabHost.setCurrentTab(1);
        }

        String tribeOp = intent.getStringExtra(TribeConstants.TRIBE_OP);
        if (!TextUtils.isEmpty(tribeOp)) {
            setIntent(intent);
            mTabHost.setCurrentTab(2);
        }
        YWLog.i(TAG, "onNewIntent");
    }
}
