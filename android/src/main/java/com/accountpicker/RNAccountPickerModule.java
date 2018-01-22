
package com.accountpicker;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;

import android.R.string;
import android.content.Intent;
import android.app.Activity;
import android.accounts.AccountManager;
import com.google.android.gms.common.AccountPicker;

public class RNAccountPickerModule extends ReactContextBaseJavaModule {
  private static final int ACCOUNT_PICKER_REQUEST = 12;
  private static final String E_ACT_NOT_EXISTS = "ACTIVITY_DOES_NOT_EXIST";
  private static final String E_USER_DID_CANCEL = "USER_DID_CANCEL";
  private static final String E_FAILED_TO_SHOW_PICKER = "FAILED_TO_SHOW_PICKER";
  private static final String E_NO_ACCOUNT_DATA = "NO_ACCOUNT_DATA";
  private static final String E_BAD_REQUEST = "BAD_REQUEST";
  
  private final ReactApplicationContext reactContext;
  private Promise pickerPromise;

  public RNAccountPickerModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    reactContext.addActivityEventListener(mActivityEventListener);
  }

  private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
      if (requestCode == ACCOUNT_PICKER_REQUEST) {
        if (pickerPromise != null) {
          if (resultCode == Activity.RESULT_CANCELED) {

            pickerPromise.reject(E_USER_DID_CANCEL, "Account picker was cancelled");

          } else if (resultCode == Activity.RESULT_OK) {

            if (intent != null) {
              String selectedAccount = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
              if (selectedAccount != null) {
                pickerPromise.resolve(selectedAccount);
              } else {
                pickerPromise.reject(E_NO_ACCOUNT_DATA, "No Account data found");
              }
            } else {
              pickerPromise.reject(E_NO_ACCOUNT_DATA, "Intent: No Account data found");
            }

          }

          pickerPromise = null;
        }
      }
    }
  };

  @Override
  public String getName() {
    return "RNAccountPicker";
  }

  @ReactMethod
  public void showAccounts(Promise promise) {
      Activity currentActivity = getCurrentActivity();

      if (currentActivity == null) {
          promise.reject(E_ACT_NOT_EXISTS, "Activity doesn't exist");
          return;
      }

      pickerPromise = promise;

      try {
        final Intent accountIntent = AccountPicker.newChooseAccountIntent(null, null, null, true, null, null, null, null);
        currentActivity.startActivityForResult(accountIntent, ACCOUNT_PICKER_REQUEST);
      } catch (Exception e) {
        pickerPromise.reject(E_FAILED_TO_SHOW_PICKER, e);
        pickerPromise = null;
      }
      
  }
}