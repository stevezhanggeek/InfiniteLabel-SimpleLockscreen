package receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.infiniteLabelSimpleLockscreen.PromptActivity;
import com.infiniteLabelSimpleLockscreen.NaturalActivity;

import java.util.Calendar;

public class lockScreenReceiver extends BroadcastReceiver  {
    public static boolean wasScreenOn = true;
    public int numPromptSession = 2;
    public int numMinuteInterval = 20;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent11;
        Calendar calendar = Calendar.getInstance();
        int minute = calendar.get(Calendar.MINUTE);
        System.out.println("minute " + minute);
        System.out.println("numPromptSession " + numPromptSession);

        if (numPromptSession * numMinuteInterval <= minute && minute < (numPromptSession+1) * numMinuteInterval) {
            intent11 = new Intent(context, PromptActivity.class);
            System.out.println("PromptActivity");
            numPromptSession++;
            if (numPromptSession > 2) numPromptSession = 0;
        } else {
            intent11 = new Intent(context, NaturalActivity.class);
            System.out.println("NaturalActivity");
        }

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            wasScreenOn=false;
            intent11.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent11);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            wasScreenOn=true;
            intent11.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        else if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            intent11.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent11);
        }
    }
}
