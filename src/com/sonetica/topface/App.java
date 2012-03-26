package com.sonetica.topface;

import com.sonetica.topface.services.NotificationService;
import com.sonetica.topface.ui.Recycle;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.Device;
import android.app.Application;
//import org.acra.ACRA;
//import org.acra.annotation.ReportsCrashes;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

/*
 *    Контекст приложения
 */
//@ReportsCrashes(formKey="dGxzMXhjeWNiei15RWM0TzJxUzR3c1E6MQ")
public class App extends Application {
  // Data
  public static boolean init;
  private static Messenger mNotificationService;
  private static ServiceConnection mServiceConnection = new ServiceConnection() {
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
        Debug.log(this,"onServiceConnected:"+e);
      }
    }
    @Override
    public void onServiceDisconnected(ComponentName name) {
      try {
        mNotificationService = null;
      } catch (Exception e) {
        Debug.log(this,"onServiceDisconnected:"+e);
      }
    }
  };
  // Constants
  public static final String TAG = "TopFace";
  //---------------------------------------------------------------------------
  @Override
  public void onCreate() {
    super.onCreate();
    
    Debug.log(this,"+onCreate");
    
    // App initialization
    init = Global.init(getApplicationContext());
    init = Data.init(getApplicationContext());
    init = Device.init(getApplicationContext());
    init = Recycle.init(getApplicationContext());
    
    startService(new Intent(getApplicationContext(),NotificationService.class));
    
    bindService(new Intent(this,NotificationService.class),mServiceConnection,Context.BIND_AUTO_CREATE);
    //ACRA.init(this);
  }
  //---------------------------------------------------------------------------
  public static void bind(Context context) {
    try {
      if(mNotificationService != null)
        mNotificationService.send(Message.obtain(null, NotificationService.MSG_BIND,0,0));
    } catch (RemoteException e) {}
  }
  //---------------------------------------------------------------------------
  public static void unbind() {
    try {
      if(mNotificationService!=null)
        mNotificationService.send(Message.obtain(null, NotificationService.MSG_UNBIND, 0, 0));
    } catch(RemoteException e) {}
  }
  //---------------------------------------------------------------------------
  public static void delete() {
    try {
      if(mNotificationService!=null)
        mNotificationService.send(Message.obtain(null, NotificationService.MSG_DELETE, 0, 0));
    } catch(RemoteException e) {}
  }
  //---------------------------------------------------------------------------
}
