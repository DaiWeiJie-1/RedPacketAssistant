package redpacket.dwj.com.redpackettest;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * Created by Administrator on 2016/8/14.
 */
public class NotificationCore {
    public static void showNotification(Context context){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setAutoCancel(true);
        builder.setContentTitle("RedPacket");
        builder.setContentText("this is my redpacket");
        builder.setSmallIcon(R.drawable.alert);
        builder.setTicker("this is my redpacket");

        Intent it = new Intent(context,AActivity.class);
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,it,0);
        android.app.Notification notification = builder.build();
        notification.contentIntent = pendingIntent;
        notificationManager.notify(1001,notification);
    }
}
