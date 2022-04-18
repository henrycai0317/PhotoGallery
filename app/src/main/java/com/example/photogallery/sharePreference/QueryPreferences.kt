package com.example.photogallery.sharePreference

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

private const val PREF_SEARCH_QUERY = "searchQuery"
//Single Pattern
object  QueryPreferences {

  fun getStoredQuery(context: Context):String {
      val prefs = PreferenceManager.getDefaultSharedPreferences(context)
      return prefs.getString(PREF_SEARCH_QUERY,"")!!
  }

  fun setStoredQuery(context: Context,query:String){
      //Use Android JetPack KTX
      PreferenceManager.getDefaultSharedPreferences(context)
          .edit{
              putString(PREF_SEARCH_QUERY,query)
          }

  }

}