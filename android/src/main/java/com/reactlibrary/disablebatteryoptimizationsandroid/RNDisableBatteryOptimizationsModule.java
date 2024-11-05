package com.reactlibrary.disablebatteryoptimizationsandroid;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Promise;

import android.app.Activity;
import android.content.Intent;
import android.provider.Settings;
import android.os.PowerManager;
import android.net.Uri;
import android.os.Build;


public class RNDisableBatteryOptimizationsModule extends ReactContextBaseJavaModule implements ActivityEventListener {

  private final ReactApplicationContext reactContext;
    private static final int BATTERY_OPTIMIZATION_REQUEST_CODE = 1234;
  private Promise batteryPromise;

    public RNDisableBatteryOptimizationsModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        reactContext.addActivityEventListener(this); // Ajout de l'écouteur d'événements
    }
  @ReactMethod
  public void openBatteryModal(Promise promise) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          String packageName = reactContext.getPackageName();
          Intent intent = new Intent();
          intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
          intent.setData(Uri.parse("package:" + packageName));
          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          this.batteryPromise = promise;
  
          reactContext.startActivityForResult(intent, BATTERY_OPTIMIZATION_REQUEST_CODE, null);
      } else {
          promise.resolve(true);
      }
  }

  @ReactMethod
  public void isBatteryOptimizationEnabled(Promise promise) {
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        String packageName = reactContext.getPackageName();
        PowerManager pm = (PowerManager) reactContext.getSystemService(reactContext.POWER_SERVICE);
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
          promise.resolve(true);
          return;
        }
    }
    promise.resolve(false);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (requestCode == BATTERY_OPTIMIZATION_REQUEST_CODE) {
          if (batteryPromise != null) {
              // Vérifiez le résultat
              if (resultCode == Activity.RESULT_OK) {
                  batteryPromise.resolve(true);
              } else {
                  batteryPromise.resolve(false);
              }
              batteryPromise = null; // Réinitialisez la promesse
          }
      }
  }


  @ReactMethod
  public void enableBackgroundServicesDialogue() {
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      Intent myIntent = new Intent();
      String packageName =  reactContext.getPackageName();
      PowerManager pm = (PowerManager) reactContext.getSystemService(reactContext.POWER_SERVICE);
      if (pm.isIgnoringBatteryOptimizations(packageName))
        myIntent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
      else {
        myIntent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        myIntent.setData(Uri.parse("package:" + packageName));
      }
	  myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      reactContext.startActivity(myIntent);
    }
  }

  @Override
  public String getName() {
    return "RNDisableBatteryOptimizationsAndroid";
  }
}