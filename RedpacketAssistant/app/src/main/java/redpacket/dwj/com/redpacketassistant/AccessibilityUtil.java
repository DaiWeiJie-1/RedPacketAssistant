package redpacket.dwj.com.redpacketassistant;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Created by Administrator on 2017/1/8.
 */

public class AccessibilityUtil {
    private static final String TAG = AccessibilityUtil.class.getSimpleName();

    private static volatile AccessibilityUtil mInstance;
    private static AccessibilityService mService;

    private AccessibilityUtil(){};

    public static AccessibilityUtil getInstance(){
        if(mInstance == null){
            synchronized (AccessibilityUtil.class){
                mInstance = new AccessibilityUtil();
            }
        }
        return mInstance;
    }

    public void init(AccessibilityService service){
        mService = service;
    }

    /**
     * 对应的node是指定的class
     * @param node
     * @param cls
     * @return
     */
    public boolean isTargetViewType(AccessibilityNodeInfo node, Class<?> cls){
        if(node == null || cls == null){
            return false;
        }

        Log.d(TAG,"####node class = " + node.getClassName() + "; cls = " + cls.getName());

        if(cls.getName().equals(node.getClassName())){
            return true;
        }else{
            return false;
        }
    }

    /**
     * home键
     */
    public void performHomeBtn(){
        if(mService != null){
            mService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
        }
    }

    /**
     * 返回键
     */
    public void performBackBtn(){
        if(mService != null){
            mService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
        }
    }

    /**
     * 找到对应text的view，从下方往上找
     * @param rootNode
     * @param textContent
     * @return
     */
    public AccessibilityNodeInfo searchViewInvertOrderByText(AccessibilityNodeInfo rootNode,String textContent){
        if(rootNode == null){
            return null;
        }

        Log.d(TAG,rootNode.getText() != null ? rootNode.getText().toString() : "");

        if(rootNode.getText() != null && textContent.equals(rootNode.getText().toString())){
            Log.d(TAG,"find textContext node !");
            return rootNode;
        }

        if(rootNode.getChildCount() == 0){
            Log.d(TAG,"searchViewByText getChildCount = 0!");
            return null;
        }else{
            //从最下找起
            for(int i = rootNode.getChildCount() -1 ; i >=0 ; i --){
                AccessibilityNodeInfo child = rootNode.getChild(i);
                AccessibilityNodeInfo resultNode = searchViewInvertOrderByText(child, textContent);
                if(resultNode != null){
                    Log.d(TAG,"return searchViewByText!");
                    return resultNode;
                }
            }
            return null;
        }
    }


    /**
     * 找到对应包含text的view，从下方往上找
     * @param rootNode
     * @param textContent
     * @return
     */
    public AccessibilityNodeInfo searchViewInvertContainsText(AccessibilityNodeInfo rootNode,String textContent){
        if(rootNode == null){
            return null;
        }

        Log.d(TAG,rootNode.getText() != null ? rootNode.getText().toString() : "");

        if(rootNode.getText() != null && rootNode.getText().toString().contains(textContent)){
            Log.d(TAG,"find textContext node !");
            return rootNode;
        }

        if(rootNode.getChildCount() == 0){
            Log.d(TAG,"searchViewByText getChildCount = 0!");
            return null;
        }else{
            //从最下找起
            for(int i = rootNode.getChildCount() -1 ; i >=0 ; i --){
                AccessibilityNodeInfo child = rootNode.getChild(i);
                AccessibilityNodeInfo resultNode = searchViewInvertContainsText(child, textContent);
                if(resultNode != null){
                    Log.d(TAG,"return searchViewByText!");
                    return resultNode;
                }
            }
            return null;
        }
    }


    /**
     * 找到对应描述的view，从上往下找起
     * @param rootNode
     * @param description
     * @return
     */
    public AccessibilityNodeInfo searchViewPositiveSeqByDescription(AccessibilityNodeInfo rootNode,String description){
        if(rootNode == null){
            return null;
        }


        if(rootNode!= null && rootNode.getContentDescription() != null && description.equals(rootNode.getContentDescription().toString())){
            Log.d(TAG,"find desc node !");
            return rootNode;
        }

        if(rootNode.getChildCount() == 0){
            Log.d(TAG,"getChildCount = 0!");
            return null;
        }else{
            for(int i = 0; i < rootNode.getChildCount(); i ++){
                AccessibilityNodeInfo child = rootNode.getChild(i);
                AccessibilityNodeInfo resultNode = searchViewPositiveSeqByDescription(child, description);
                if(resultNode != null){
                    Log.d(TAG,"return resultNode!");
                    return resultNode;
                }
            }
            return null;
        }
    }

    /**
     * 找到对应Class的view，从上往下找起
     * @param rootNode
     * @param cls
     * @return
     */
    public AccessibilityNodeInfo searchViewPositiveSeqByClass(AccessibilityNodeInfo rootNode,Class<?> cls){
        if(rootNode == null){
            return null;
        }

        if(rootNode!= null && rootNode.getClassName() != null && cls.getName().equals(rootNode.getClassName().toString())){
            Log.d(TAG,"find class node !");
            return rootNode;
        }

        if(rootNode.getChildCount() == 0){
            Log.d(TAG,"getChildCount = 0!");
            return null;
        }else{
            for(int i = 0; i < rootNode.getChildCount(); i ++){
                AccessibilityNodeInfo child = rootNode.getChild(i);
                AccessibilityNodeInfo resultNode = searchViewPositiveSeqByClass(child, cls);
                if(resultNode != null){
                    Log.d(TAG,"return resultNode!");
                    return resultNode;
                }
            }
            return null;
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void foreachViews(AccessibilityNodeInfo rootNode){
        if(rootNode.getChildCount() == 0){
            Log.d(TAG,"node info: final view ** class name = " + rootNode.getClassName() + ", id = " +  rootNode.getViewIdResourceName() + ",text = " + rootNode.getText()
                    + "desc = " + rootNode.getContentDescription() + "; window id = " + rootNode.getWindowId());
            return;
        }else{
            Log.d(TAG,"node info: class name = " + rootNode.getClassName() + ", id = " +  rootNode.getViewIdResourceName() + ",text = " + rootNode.getText());
            for(int i = 0 ; i < rootNode.getChildCount(); i ++){
                AccessibilityNodeInfo child = rootNode.getChild(i);
                foreachViews(child);
            }
        }
    }

    /**
     * 模拟点击事件,如果该view不可点则找到它可点击的parent
     * @param nodeInfo
     */
    public void performClick(AccessibilityNodeInfo nodeInfo){
        if(nodeInfo != null){
            if(nodeInfo.isClickable()){
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.d(TAG,"click node!!");
            }else{
                Log.d(TAG,"node is not clickable");
                while (nodeInfo.getParent() != null){
                    AccessibilityNodeInfo parent = nodeInfo.getParent();
                    if(parent == null){
                        break;
                    }else{
                        if(parent.isClickable()){
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            Log.d(TAG,"node parent is clickable");
                            break;
                        }else{
                            nodeInfo = parent;
                            Log.d(TAG,"parent replace node");
                        }
                    }
                }
            }
        }
    }
}
