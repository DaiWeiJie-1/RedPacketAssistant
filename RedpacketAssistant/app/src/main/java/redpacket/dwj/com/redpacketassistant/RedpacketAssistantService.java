package redpacket.dwj.com.redpacketassistant;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Administrator on 2016/8/14.
 */
public class RedpacketAssistantService extends AccessibilityService{
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        Log.d("Redpacket","onAccessibilityEvent");
        int eventType = accessibilityEvent.getEventType();
        switch (eventType){
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                Log.d("Redpacket","TYPE_NOTIFICATION_STATE_CHANGED");
                handleNotification(accessibilityEvent);
                break;

            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
                Log.d("Redpacket","TYPE_WINDOWS_CHANGED");
                break;

            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                Log.d("Redpacket","TYPE_WINDOW_STATE_CHANGED");
                handlerWindowStateChanged(accessibilityEvent);
                break;

            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                Log.d("Redpacket","TYPE_VIEW_CLICKED");
                AccessibilityNodeInfo source = accessibilityEvent.getSource();
                if(source != null){
                    Log.d("Redpacket","TYPE_VIEW_CLICKED : source = " + source.toString());
                }
                break;
        }
    }

    @Override
    public void onInterrupt() {

    }

    private void handleNotification(AccessibilityEvent event){
        List<CharSequence> texts = event.getText();
        if(!texts.isEmpty()){
            for(CharSequence text : texts){
                Log.d("Redpacket","notification text : " + text);
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
            Log.d("Redpacket","notification text is null");
        }
    }

    private void handlerWindowStateChanged(AccessibilityEvent event){
        if(event.getClassName().equals("com.tencent.mm.ui.LauncherUI")){
            AccessibilityNodeInfo receiveRedPacketNode =  findReceiveRedpacket();
            if(receiveRedPacketNode != null){
                if(receiveRedPacketNode.isClickable()){
                    receiveRedPacketNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }

                if(receiveRedPacketNode.getParent() != null){
                    if(receiveRedPacketNode.getParent().isClickable()){
                        receiveRedPacketNode.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }

                    if(receiveRedPacketNode.getParent().getParent() != null){
                        if(receiveRedPacketNode.getParent().getParent().isClickable()){
                            receiveRedPacketNode.getParent().getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }

                }
            }
        }else if(event.getClassName().toString().equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI")){
            openRedPacket();
        }
    }

    private AccessibilityNodeInfo findReceiveRedpacket(){
        AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();
        return recurFind(rootNodeInfo);
    }

    private AccessibilityNodeInfo recurFind(AccessibilityNodeInfo nodeInfo){
        if(nodeInfo.getChildCount() == 0){
            if(nodeInfo.getText() != null){
                if(nodeInfo.getText().toString().equals("领取红包")){
                    Log.d("recurfind","find 领取红包");
                    return nodeInfo;
                }
            }
        }else {
            for(int i = 0; i < nodeInfo.getChildCount(); i ++){
                if(nodeInfo.getChild(i) != null){
                     AccessibilityNodeInfo info = recurFind(nodeInfo.getChild(i));
                    if(info != null){
                        Log.d("recurfind","find red packet and break");
                        return info;
                    }
                }
            }
        }
        return null;
    }

    private void openRedPacket(){
        AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
        foreachView(rootInActiveWindow);
    }

    private void foreachView(AccessibilityNodeInfo nodeInfo){
        Rect rect = new Rect();
        nodeInfo.getBoundsInScreen(rect);

        if(rect.left == 405 && rect.top ==1049){
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
        Log.d("foreachView","rect = " + rect.toString());
        if(nodeInfo.getChildCount() == 0){
            return;
        }else{
            for(int i =0; i < nodeInfo.getChildCount(); i ++){
                if(nodeInfo.getChild(i) != null){
                    foreachView(nodeInfo.getChild(i));
                }else{
                    Log.d("foreachView","rect = " + rect.toString());
                }
            }
        }
    }
}
