package com.originalgame.autoloot

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object ActionStore {
    private const val PREF = "actions"
    private const val KEY = "list"
    fun save(ctx: Context, actions: List<Action>) {
        val a = JSONArray(); actions.forEach { x ->
            a.put(JSONObject().apply { put("type",x.type); put("x",x.x); put("y",x.y); put("x2",x.x2); put("y2",x.y2); put("durationMs",x.durationMs); put("pauseMs",x.pauseMs); put("enabled",x.enabled) })
        }
        ctx.getSharedPreferences(PREF,0).edit().putString(KEY,a.toString()).apply()
    }
    fun load(ctx: Context): MutableList<Action> {
        val s=ctx.getSharedPreferences(PREF,0).getString(KEY,"[]") ?: "[]"; val a=JSONArray(s); val out=mutableListOf<Action>()
        for(i in 0 until a.length()){ val o=a.getJSONObject(i); out += Action(o.optString("type","tap"),o.optDouble("x",0.0).toFloat(),o.optDouble("y",0.0).toFloat(),o.optDouble("x2",0.0).toFloat(),o.optDouble("y2",0.0).toFloat(),o.optLong("durationMs",80),o.optLong("pauseMs",120),o.optBoolean("enabled",true)) }
        return out
    }
}
