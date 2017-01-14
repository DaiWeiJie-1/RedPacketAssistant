package redpacket.dwj.com.redpacketassistant;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2016/8/14.
 */
public class RedpacketAssistantService extends AccessibilityService{
    private static final String TAG = RedpacketAssistantService.class.getSimpleName();
    /**
     * 对话详情界面
     */
    private static final String TALK_DETAIL_ACTIVITY_NAME = "com.tencent.mm.ui.LauncherUI";

    /**
     * 红包界面
     */
    private static final String REDPACKET_ACTIVITY_NAME = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";

    /**
     * View描述
     * 对话详情界面中有返回imageview,description为"返回"
     */
    private static final String VIEW_DESCRIPTION_BACK_TALK_DETAIL_ACTIVITY = "返回";

    /**
     * View textContent
     * 对话详情界面中的领取红包view
     */
    private static final String VIEW_TEXT_REDPACKET_TALK_DETAIL_ACTIVITY = "领取红包";

    /**
     * 红包界面判断未开红包(定向红包)
     */
    private static final String VIEW_TEXT_UNOPEND_REDPACKET = "给你发了一个红包";

    private static final String VIEW_TEXT_RAND_UNOPNED_REDPACKET = "金额随机";


    /**
     * 红包金额详情页面
     */
    private static final String VIEW_TEXT_READPACKET_DETAIL = "红包详情";

    private AccessibilityUtil mUtil;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        mUtil = AccessibilityUtil.getInstance();
        mUtil.init(this);
        Log.d("redpacket-service","onServiceConnected : " + new Date().toString());
    }




    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        Log.d("redpacket-service","onAccessibilityEvent start : " + new Date().toString());
        Log.d(TAG,"onAccessibilityEvent,eventType = " + accessibilityEvent.getEventType());
        int eventType = accessibilityEvent.getEventType();
        switch (eventType){
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                Log.d(TAG,"TYPE_NOTIFICATION_STATE_CHANGED");
                handleNotificationJumpToWechat(accessibilityEvent);
                break;

            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
                Log.d(TAG,"TYPE_WINDOWS_CHANGED");

                break;

            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                Log.d(TAG,"TYPE_WINDOW_STATE_CHANGED,event class = " + accessibilityEvent.getClassName());
                boolean isTalkActivity = isTalkDetailActivity(accessibilityEvent);
                Log.d(TAG,"is Talkactivity = " + isTalkActivity);

                boolean isReaPacketActivity = isRedpacketActivity(accessibilityEvent);
                if(isReaPacketActivity){
                    boolean isUnopendRedpacket = isUnOpendRedPacket(getRootInActiveWindow());
                    if(isUnopendRedpacket){
                        Log.d(TAG,"This is unopend redpacket !!!!!!!!!!!!");
                        openRedPacket();
//                        mUtil.performHomeBtn();
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        boolean redpacketDetailWindow = isRedpacketDetailWindow(getRootInActiveWindow());
                        if(redpacketDetailWindow){
                            Log.d(TAG,"This is redDetail !!!!!!!!!!!!");
                            findRedpacketSum(getRootInActiveWindow());
                        }else{
                            Log.d(TAG,"This  not is redDetail !!!!!!!!!!!!");
                        }
                    }
                }

                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                Log.d(TAG,"TYPE_WINDOW_CONTENT_CHANGED");
                boolean talkActivity = isTalkDetailActivity(accessibilityEvent);
                Log.d(TAG,"TYPE_WINDOW_CONTENT_CHANGED is Talkactivity = " + talkActivity);

                AccessibilityNodeInfo nodeInfo = mUtil.searchViewInvertOrderByText(getRootInActiveWindow(), "领取红包");
                if(nodeInfo != null){
                    mUtil.performClick(nodeInfo);
                }
                break;

            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                //有可能聊天界面刷出红包
//                handlerCurrentListViewScrolled(accessibilityEvent);
                break;

            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                Log.d(TAG,"TYPE_VIEW_CLICKED");
//                AccessibilityNodeInfo source = accessibilityEvent.getSource();
//                if(source != null){
//                    Log.d("Redpacket","TYPE_VIEW_CLICKED : source = " + source.toString());
//                }
                break;
        }
        Log.d("redpacket-service","onAccessibilityEvent end : " + new Date().toString());
    }

    @Override
    public void onInterrupt() {
        Log.d("redpacket-service","onInterrupt : " + new Date().toString());
    }

    /**
     * 处理微信红包通知ticket,并跳转到微信
     * @param event
     */
    private void handleNotificationJumpToWechat(AccessibilityEvent event){
        List<CharSequence> texts = event.getText();
        if(!texts.isEmpty()){
            for(CharSequence text : texts){
                Log.d(TAG,"notification text : " + text + "; notifi time = " + System.currentTimeMillis());
                String context = text.toString();
                if(context.contains("[微信红包]")){
                    if(event.getParcelableData() != null && event.getParcelableData() instanceof Notification){
                        Notification nt = (Notification) event.getParcelableData();
                        PendingIntent pendingIntent = nt.contentIntent;
                        try {
                            pendingIntent.send();
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }else {
            Log.d(TAG,"notification text is null");
        }
    }

    /**
     * 判断是否是对话界面
     * @param event
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean isTalkDetailActivity(AccessibilityEvent event){
        if(event != null && TALK_DETAIL_ACTIVITY_NAME.equals(event.getClassName().toString())){
            Log.d(TAG,"class name = " + TALK_DETAIL_ACTIVITY_NAME);
            AccessibilityNodeInfo activeWindow = getRootInActiveWindow();
            AccessibilityNodeInfo nodeInfo = mUtil.searchViewPositiveSeqByDescription(activeWindow, VIEW_DESCRIPTION_BACK_TALK_DETAIL_ACTIVITY);
            if(nodeInfo != null && mUtil.isTargetViewType(nodeInfo, ImageView.class)){
                return true;
            }

        }else{
            Log.d(TAG,"class name is not talk detail, class name = " + event.getClassName().toString());
        }
        return false;
    }

    private boolean isRedpacketActivity(AccessibilityEvent event){
        if(event != null && REDPACKET_ACTIVITY_NAME.equals(event.getClassName().toString())){
            return true;
        }else{
            Log.d(TAG,"class node is not red packet, class name = " + event.getClassName().toString());
            return false;
        }
    }

    /**
     * 红包是否开启
     * @param rootNode
     * @return
     */
    private boolean isUnOpendRedPacket(AccessibilityNodeInfo rootNode){
        if(rootNode != null){
            AccessibilityNodeInfo nodeInfo = mUtil.searchViewInvertOrderByText(rootNode, VIEW_TEXT_UNOPEND_REDPACKET);
            if(nodeInfo != null){
                return true;
            }else{

                AccessibilityNodeInfo randNodeInfo = mUtil.searchViewInvertContainsText(rootNode, VIEW_TEXT_RAND_UNOPNED_REDPACKET);
                if(randNodeInfo != null){
                    return true;
                }else{
                    return false;
                }
            }
        }
        return false;
    }

    private boolean isRedpacketDetailWindow(AccessibilityNodeInfo roootNode){
        if(roootNode != null){
            AccessibilityNodeInfo nodeInfo = mUtil.searchViewInvertOrderByText(roootNode, VIEW_TEXT_READPACKET_DETAIL);
            if(nodeInfo != null){
                return true;
            }
        }
        return false;
    }

    private void openRedPacket(){
        AccessibilityNodeInfo nodeInfo = mUtil.searchViewPositiveSeqByClass(getRootInActiveWindow(), Button.class);
        if(nodeInfo != null){
            mUtil.performClick(nodeInfo);
        }
    }

    private String findRedpacketSum(AccessibilityNodeInfo rootNode){
        if(rootNode == null){
            return null;
        }

        Log.d(TAG,rootNode.getText() != null ? rootNode.getText().toString() : "");

        if(rootNode.getText() != null && rootNode.getText() != null && Pattern.matches("[0-9]+.[0-9]+",rootNode.getText().toString())){
            Log.d(TAG,"find sum ! " + rootNode.getText().toString());
            return rootNode.getText().toString();
        }

        if(rootNode.getChildCount() == 0){
            Log.d(TAG,"findRedpacketSum getChildCount = 0!");
            return null;
        }else{
            //从最下找起
            for(int i = rootNode.getChildCount() -1 ; i >=0 ; i --){
                AccessibilityNodeInfo child = rootNode.getChild(i);
                String sum = findRedpacketSum(child);
                if(sum != null){
                    Log.d(TAG,"return sum!");
                    return sum;
                }
            }
            return null;
        }
    }



    private void findRedPacketView(AccessibilityEvent event){
        AccessibilityNodeInfo activeWindow = getRootInActiveWindow();
    }


    private void handlerCurrentListViewScrolled(AccessibilityEvent event){
        if(event.getClassName().toString().equals("android.widget.ListView")){
            AccessibilityNodeInfo receiveRedPacketNode =  findReceiveRedpacket();
            if(receiveRedPacketNode != null){
                if(receiveRedPacketNode.isClickable()){
                    receiveRedPacketNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }

                if(receiveRedPacketNode.getParent() != null){
                    if(receiveRedPacketNode.getParent().isClickable()){
                        //进入拆红包界面
                        receiveRedPacketNode.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
            }
        }else if(event.getClassName().toString().equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI")){
            openRedPacket();
        }
    }

    private void handlerWindowStateChanged(AccessibilityEvent event){
        if(event.getClassName().equals("com.tencent.mm.ui.LauncherUI")/*从通知栏或者直接进入具体对话启动界面*/
                || event.getClassName().toString().equals("android.widget.ListView")){
            AccessibilityNodeInfo receiveRedPacketNode =  findReceiveRedpacket();
            intoOpenRedPacketActivity(receiveRedPacketNode);
        }else if(event.getClassName().toString().equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI")){
            openRedPacket();
        }
    }

    private AccessibilityNodeInfo findReceiveRedpacket(){
        AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();
        return recurFindRedPacket(rootNodeInfo);
    }

    private void intoOpenRedPacketActivity(AccessibilityNodeInfo nodeInfo){
        if(nodeInfo != null){
            if(nodeInfo.isClickable()){
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }

            if(nodeInfo.getParent() != null){
                if(nodeInfo.getParent().isClickable()){
                    nodeInfo.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
    }


    private int tempListPosition = -100;
    private int openedInfoPosition = -100;
    private AccessibilityNodeInfo recurFindRedPacket(AccessibilityNodeInfo nodeInfo){
        if(nodeInfo.getChildCount() == 0){
            if(nodeInfo.getText() != null){
                if(nodeInfo.getText().toString().contains("你领取了")){
                    openedInfoPosition = tempListPosition;
                    return null;
                }else if(nodeInfo.getText().toString().equals("领取红包")){
                    if(openedInfoPosition == (tempListPosition + 1)){
                        return null;
                    }else{
                        return nodeInfo;
                    }
                }
            }
        }else {
            for(int i = nodeInfo.getChildCount()-1; i >= 0; i --){
                if(nodeInfo.getClassName().equals("android.widget.ListView")){
                    tempListPosition = i;
                }
                if(nodeInfo.getChild(i) != null){
                     AccessibilityNodeInfo info = recurFindRedPacket(nodeInfo.getChild(i));
                    if(info != null){
                        Log.d("recurfind","find red packet and break");
                        return info;
                    }
                }
            }
        }
        return null;
    }

//    private void openRedPacket(){
//        AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
//        AccessibilityNodeInfo openRedPacketBtn =  foreachViewForOpenBtn(rootInActiveWindow);
////        if(openRedPacketBtn != null){
////            openRedPacketBtn.performAction(AccessibilityNodeInfo.ACTION_CLICK);
////        }
//    }

    private AccessibilityNodeInfo foreachViewForOpenBtn(AccessibilityNodeInfo nodeInfo){
        if(nodeInfo == null){
            return null;
        }
        Rect rect = new Rect();
        nodeInfo.getBoundsInScreen(rect);

        if(rect.left >= 300 && rect.right <= 800 && rect.top >= 900 && rect.bottom <= 1400 && nodeInfo.getClassName().equals("android.widget.Button")){
           return nodeInfo;
        }else{
            for(int i =0; i < nodeInfo.getChildCount(); i ++){
                if(nodeInfo.getChild(i) != null){
                    AccessibilityNodeInfo node = foreachViewForOpenBtn(nodeInfo.getChild(i));
                    if(node != null){
                        return node;
                    }
                }else{
                    Log.d("foreachView","rect = " + rect.toString());
                }
            }
        }
        return null;
    }

//    int tempPos = -1;
//    int pos = -1;
//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
//    private void foreachViews(AccessibilityNodeInfo nodeInfo){
//        Log.d("foreach","node info : className = " + nodeInfo.getClassName()
//                + "; text = " + nodeInfo.getText() + " ; contentDesc = " + nodeInfo.getContentDescription()
//                + ";viewId =  " + nodeInfo.getViewIdResourceName() + "; windowId" + nodeInfo.getWindowId());
//        if(nodeInfo.getText() != null && nodeInfo.getText().toString().contains("你领取了")){
//            Log.d("foreach","parent node info : className = " + nodeInfo.getParent().getClassName()
//                    + "; text = " + nodeInfo.getParent().getText());
//            Log.d("foreach","parent parent node info : className = " + nodeInfo.getParent().getParent().getClassName()
//                    + "; text = " + nodeInfo.getParent().getParent().getText());
//            Log.d("foreach","tempPos = " + tempPos);
//            pos = tempPos;
//        }
//
//        for(int i = nodeInfo.getChildCount()-1; i >= 0; i --){
//            if(i == (pos - 1)){
//                continue;
//            }
//
//            if(nodeInfo.getClassName().equals("android.widget.ListView")){
//                tempPos = i;
//            }
//
//            if(nodeInfo.getChild(i) != null){
//               foreachViews(nodeInfo.getChild(i));
//
//            }
//        }
//    }









}
