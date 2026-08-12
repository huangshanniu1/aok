package com.originalgame.autoloot

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.TextView
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs
import kotlin.math.hypot
import java.util.concurrent.Executor

class AutoAccessibilityService : AccessibilityService() {
    companion object { var instance: AutoAccessibilityService? = null }
    private val handler=Handler(Looper.getMainLooper()); private var ball:TextView?=null; private var recorder:View?=null
    private val running=AtomicBoolean(false); private var actions=mutableListOf<Action>(); private var visionRunning=AtomicBoolean(false)
    private var buttonRegion=ScreenRegion(.72f,.75f,.99f,.99f); private var lootRegion=ScreenRegion(.10f,.15f,.90f,.85f)
    override fun onServiceConnected(){ instance=this; showBall() }
    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {}
    override fun onInterrupt(){}
    fun setRegions(button:ScreenRegion, loot:ScreenRegion){ buttonRegion=button; lootRegion=loot }
    fun startRecording(){ if(recorder!=null)return; actions.clear(); val wm=getSystemService(WINDOW_SERVICE) as WindowManager; val lp=WindowManager.LayoutParams(-1,-1,if(Build.VERSION.SDK_INT>=26)WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,PixelFormat.TRANSLUCENT); recorder=RecordOverlay(); wm.addView(recorder,lp) }
    fun stopRecording(save:Boolean=true){ recorder?.let{ (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(it)}; recorder=null; if(save) ActionStore.save(this,actions) }
    fun play(){ if(running.get())return; actions=ActionStore.load(this); running.set(true); playAt(0) }
    private fun playAt(i:Int){ if(!running.get()){return}; if(i>=actions.size){running.set(false);return}; val a=actions[i]; if(!a.enabled){playAt(i+1);return}; dispatch(a){ handler.postDelayed({playAt(i+1)},a.pauseMs) } }
    private fun dispatch(a:Action, done:()->Unit){ val p=Path(); p.moveTo(a.x,a.y); if(a.type=="swipe")p.lineTo(a.x2,a.y2); val duration=if(a.type=="long")maxOf(600,a.durationMs) else a.durationMs; val stroke=GestureDescription.StrokeDescription(p,0,duration); dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(),object:GestureResultCallback(){override fun onCompleted(g:GestureDescription){done()};override fun onCancelled(g:GestureDescription){handler.postDelayed(done,150)}},handler) }
    fun stop(){running.set(false); visionRunning.set(false)}
    fun startVision(){ if(visionRunning.get())return; visionRunning.set(true); visionTick() }
    private fun visionTick(){ if(!visionRunning.get())return; if(Build.VERSION.SDK_INT<30){ visionRunning.set(false); return }
        takeScreenshot(0, Executor { it.run() }, object:TakeScreenshotCallback(){ override fun onSuccess(result:ScreenshotResult){ val src=result.hardwareBuffer; val bmp=Bitmap.wrapHardwareBuffer(src,result.colorSpace); src.close(); if(bmp!=null){ val search=VisualRules.detectButton(bmp,buttonRegion); if(search.detected){ dispatch(Action("tap",search.cx*bmp.width,search.cy*bmp.height,0f,0f,80,100,true)){} } else { val loot=VisualRules.scan(bmp,lootRegion); if(loot.detected){ dispatch(Action("tap",loot.cx*bmp.width,loot.cy*bmp.height,0f,0f,80,160,true)){} } } bmp.recycle() }; handler.postDelayed({visionTick()},350) } override fun onFailure(errorCode:Int){handler.postDelayed({visionTick()},500)} }, handler) }
    fun getActions()=ActionStore.load(this)
    private fun showBall(){ if(ball!=null || !android.provider.Settings.canDrawOverlays(this))return; ball=TextView(this).apply{text="●";textSize=34f;setTextColor(0xffffffff);setPadding(8,0,8,0);setOnClickListener{ if(recorder==null)startRecording() else stopRecording(true) }}; val lp=WindowManager.LayoutParams(70,70,if(Build.VERSION.SDK_INT>=26)WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,PixelFormat.TRANSLUCENT); lp.gravity=Gravity.TOP or Gravity.END; lp.x=20;lp.y=240;(getSystemService(WINDOW_SERVICE) as WindowManager).addView(ball,lp) }
    inner class RecordOverlay: View(this@AutoAccessibilityService){ var downX=0f;var downY=0f;var downT=0L; override fun onTouchEvent(e:android.view.MotionEvent):Boolean{when(e.actionMasked){MotionEvent.ACTION_DOWN->{downX=e.rawX;downY=e.rawY;downT=System.currentTimeMillis();return true};MotionEvent.ACTION_UP->{val x=e.rawX;val y=e.rawY;val dt=System.currentTimeMillis()-downT;val dist=hypot((x-downX).toDouble(),(y-downY).toDouble());val type=if(dist>70)"swipe" else if(dt>=500)"long" else "tap";actions+=Action(type,downX,downY,x,y,dt,120,true); dispatch(Action(type,downX,downY,x,y,dt,120,true)){}; return true}};return true}}
    override fun onDestroy(){stopRecording(false);ball?.let{try{(getSystemService(WINDOW_SERVICE) as WindowManager).removeView(it)}catch(_:Exception){}};ball=null;instance=null;super.onDestroy()}
}
