package receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.infiniteLabelSimpleLockscreen.LockscreenActivity;

import java.util.Calendar;

public class lockScreenReceiver extends BroadcastReceiver  {
    public boolean finishTaskAtTime0 = false;
    public boolean finishTaskAtTime20 = false;
    public boolean finishTaskAtTime40 = false;
    public static boolean wasScreenOn = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent newIntent;
        Calendar calendar = Calendar.getInstance();
        int minute = calendar.get(Calendar.MINUTE);
        System.out.println("minute " + minute);

        // Refresh when user complete all work
        if (finishTaskAtTime0 && finishTaskAtTime20 && finishTaskAtTime40) {
            finishTaskAtTime0 = false;
            finishTaskAtTime20 = false;
            finishTaskAtTime40 = false;
        }

        if (0 <= minute && minute < 20 && !finishTaskAtTime0) {
            finishTaskAtTime0 = true;
            newIntent = new Intent(context, LockscreenActivity.class);
            newIntent.putExtra("naturalOrPrompt", "prompt");
            System.out.println("PromptActivityAtTime0");
        } else if (20 <= minute && minute < 40 && !finishTaskAtTime20) {
            finishTaskAtTime20 = true;
            newIntent = new Intent(context, LockscreenActivity.class);
            newIntent.putExtra("naturalOrPrompt", "prompt");
            System.out.println("PromptActivityAtTime20");
        } else if (40 <= minute && minute < 60 && !finishTaskAtTime40) {
            finishTaskAtTime40 = true;
            newIntent = new Intent(context, LockscreenActivity.class);
            newIntent.putExtra("naturalOrPrompt", "prompt");
            System.out.println("PromptActivityAtTime40");
        } else {
            newIntent = new Intent(context, LockscreenActivity.class);
            newIntent.putExtra("naturalOrPrompt", "natural");
            System.out.println("NaturalActivity");
        }

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            wasScreenOn=false;
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(newIntent);
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            wasScreenOn=true;
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        else if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(newIntent);
        }
    }
}
