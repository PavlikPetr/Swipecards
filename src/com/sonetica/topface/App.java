package com.sonetica.topface;

import com.sonetica.topface.services.NotificationService;
import com.sonetica.topface.services.StatisticService;
import com.sonetica.topface.ui.Recycle;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.Device;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
//import org.acra.ACRA;
//import org.acra.annotation.ReportsCrashes;

/*
 *    Контекст приложения
 */
//@ReportsCrashes(formKey="dGxzMXhjeWNiei15RWM0TzJxUzR3c1E6MQ")
public class App extends Application implements ServiceConnection {
  // Data
  public static boolean init;
  private Messenger mNotificationService;
  // Constants
  public static final String TAG = "TopFace";
  //---------------------------------------------------------------------------
  @Override
  public void onCreate() {
    super.onCreate();
    Debug.log("App","++onCreate");
    
    //ACRA.init(this);
    
    /*
    ActivityManager manager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
    for(RunningAppProcessInfo app : manager.getRunningAppProcesses())
      if(app.processName.equals("com.sonetica.topface:notification"))
        return;
        
    // Data.SSID рассинхрон между процессами
       * 
    */
    
    // App initialization
    init = Global.init(getApplicationContext());
    init = Data.init(getApplicationContext());
    init = Device.init(getApplicationContext());
    init = Recycle.init(getApplicationContext());
    
    startService(new Intent(getApplicationContext(),StatisticService.class));
    startService(new Intent(getApplicationContext(),NotificationService.class));
    bindService(new Intent(this,NotificationService.class),this,Context.BIND_AUTO_CREATE);

  }
  //---------------------------------------------------------------------------
  public void bind(Context context) {
    try {
      if(mNotificationService != null) {
        mNotificationService.send(Message.obtain(null, NotificationService.MSG_BIND,0,0));
        Debug.log("App","bind");
      }
    } catch (RemoteException e) {
      Debug.log("App","bind notification:"+e);
    }
  }
  //---------------------------------------------------------------------------
  public void unbind() {
    try {
      if(mNotificationService != null) {
        mNotificationService.send(Message.obtain(null, NotificationService.MSG_UNBIND, 0, 0));
        Debug.log("App","unbind");
      }
    } catch(RemoteException e) {
      Debug.log("App","unbind notification:"+e);
    }
  }
  //---------------------------------------------------------------------------
  public void delete() {
    try {
      if(mNotificationService != null)
        mNotificationService.send(Message.obtain(null, NotificationService.MSG_DELETE, 0, 0));
    } catch(RemoteException e) {
      Debug.log("App","delete notification:"+e);
    }
  }
  //---------------------------------------------------------------------------
  @Override
  public void onServiceConnected(ComponentName name,IBinder service) {
    try {
      if(mNotificationService != null) {
        mNotificationService.send(Message.obtain(null, NotificationService.MSG_BIND,0,0));
      } else {
        mNotificationService = new Messenger(service);
        if(mNotificationService != null)
          mNotificationService.send(Message.obtain(null, NotificationService.MSG_BIND,0,0));
      }
    } catch (Exception e) {
      Debug.log("App","onServiceConnected:"+e);
    }
  }
  //---------------------------------------------------------------------------
  @Override
  public void onServiceDisconnected(ComponentName name) {
    try {
      mNotificationService = null;
    } catch (Exception e) {
      Debug.log("App","onServiceDisconnected:"+e);
    }
  }
  //---------------------------------------------------------------------------
  @Override
  protected void finalize() throws Throwable {
    Debug.log("App","finalize");
    super.finalize();
  }
  //---------------------------------------------------------------------------
}
