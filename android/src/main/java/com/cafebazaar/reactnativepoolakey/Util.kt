package com.cafebazaar.reactnativepoolakey

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import kotlin.reflect.full.memberProperties

object Util {
  fun getWritableMapOf(obj: Any): WritableMap {
    val writableMap = Arguments.createMap()
    val objClass = obj::class
    for(property in objClass.memberProperties) {
      val key = property.name
      val value = property.getter.call(obj)
      when(value) {
        // add other types in needed to be returned to JS thread
        is Int -> writableMap.putInt(key, value)
        is String -> writableMap.putString(key, value)
        is Enum<*> -> writableMap.putString(key, value.toString())
        is Boolean -> writableMap.putBoolean(key, value)
        is Long -> writableMap.putLong(key, value)
        else -> {
          if(value == null) continue;
          val childWritableMap = getWritableMapOf(value)
          writableMap.putMap(key, childWritableMap)
        }
      } // end when
    } // end for
    return writableMap
  } // end getWritableMapOf
}
