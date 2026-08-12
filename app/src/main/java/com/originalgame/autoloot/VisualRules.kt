package com.originalgame.autoloot

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.max
import kotlin.math.min

object VisualRules {
    data class Result(val detected:Boolean, val cx:Float, val cy:Float, val score:Float)
    fun scan(bitmap: Bitmap, region: ScreenRegion, minPixels:Int=8, hueMin:Float=265f, hueMax:Float=325f, satMin:Float=.34f, valMin:Float=.35f): Result {
        val l=(region.l.coerceIn(0f,1f)*bitmap.width).toInt(); val t=(region.t.coerceIn(0f,1f)*bitmap.height).toInt(); val r=(region.r.coerceIn(0f,1f)*bitmap.width).toInt(); val b=(region.b.coerceIn(0f,1f)*bitmap.height).toInt()
        var count=0; var sx=0L; var sy=0L; val step=max(1,(max(r-l,b-t)/220))
        for(y in t until max(t+1,b) step step) for(x in l until max(l+1,r) step step){
            val hsv=FloatArray(3); Color.colorToHSV(bitmap.getPixel(x.coerceIn(0,bitmap.width-1),y.coerceIn(0,bitmap.height-1)),hsv)
            if(hsv[0] >= hueMin && hsv[0] <= hueMax && hsv[1] >= satMin && hsv[2] >= valMin){ count++; sx+=x; sy+=y }
        }
        if(count<minPixels) return Result(false,0f,0f,0f)
        return Result(true,(sx/count.toFloat()/bitmap.width),(sy/count.toFloat()/bitmap.height),min(1f,count/250f))
    }
    fun detectButton(bitmap: Bitmap, region: ScreenRegion, minBright:Float=.58f, minPixels:Int=18): Result {
        val l=(region.l.coerceIn(0f,1f)*bitmap.width).toInt(); val t=(region.t.coerceIn(0f,1f)*bitmap.height).toInt(); val r=(region.r.coerceIn(0f,1f)*bitmap.width).toInt(); val b=(region.b.coerceIn(0f,1f)*bitmap.height).toInt()
        var count=0; var sx=0L; var sy=0L; val step=max(1,max(r-l,b-t)/180)
        for(y in t until max(t+1,b) step step) for(x in l until max(l+1,r) step step){
            val c=bitmap.getPixel(x.coerceIn(0,bitmap.width-1),y.coerceIn(0,bitmap.height-1)); val v=max(Color.red(c),max(Color.green(c),Color.blue(c)))/255f
            if(v>=minBright){count++;sx+=x;sy+=y}
        }
        if(count<minPixels) return Result(false,0f,0f,0f)
        return Result(true,(sx/count.toFloat()/bitmap.width),(sy/count.toFloat()/bitmap.height),min(1f,count/200f))
    }
}
