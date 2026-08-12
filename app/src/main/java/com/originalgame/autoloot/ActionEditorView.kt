package com.originalgame.autoloot

import android.content.Context
import android.graphics.*
import android.view.*
import kotlin.math.hypot

class ActionEditorView(ctx:Context, val actions:MutableList<Action>, val onChanged:()->Unit):View(ctx){
    private val p=Paint(Paint.ANTI_ALIAS_FLAG); var selected=-1; var lastX=0f;var lastY=0f
    override fun onDraw(c:Canvas){super.onDraw(c);c.drawColor(Color.rgb(14,15,18));p.textSize=22f;p.color=Color.LTGRAY;c.drawText("拖动白色圆点编辑动作",24f,40f,p);p.textSize=14f
        actions.forEachIndexed{idx,a-> if(!a.enabled)return@forEachIndexed;val x=a.x.coerceIn(0f,width.toFloat());val y=a.y.coerceIn(60f,height.toFloat());p.color=Color.WHITE;c.drawCircle(x,y,14f,p);p.color=Color.DKGRAY;c.drawText("${idx+1}",x-5,y+5,p)}
    }
    override fun onTouchEvent(e:MotionEvent):Boolean{when(e.actionMasked){MotionEvent.ACTION_DOWN->{selected=hit(e.x,e.y);lastX=e.x;lastY=e.y;return true};MotionEvent.ACTION_MOVE->{if(selected>=0){actions[selected].x=e.x;actions[selected].y=e.y;onChanged();invalidate()};return true};MotionEvent.ACTION_UP->{selected=-1;return true}};return true}
    private fun hit(x:Float,y:Float):Int{var best=-1;var d=40.0;for((i,a) in actions.withIndex()){val dd=hypot((a.x-x).toDouble(),(a.y-y).toDouble());if(dd<d){d=dd;best=i}};return best}
}
