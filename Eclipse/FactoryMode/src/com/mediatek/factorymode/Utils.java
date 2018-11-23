package com.mediatek.factorymode;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;

public class Utils
{
  public static void SetPreferences(Context paramContext, SharedPreferences paramSharedPreferences, int paramInt, String paramString)
  {
    String str = paramContext.getResources().getString(paramInt);
    SharedPreferences.Editor localEditor = paramSharedPreferences.edit();
    localEditor.putString(str, paramString);
    localEditor.commit();
  }
}
