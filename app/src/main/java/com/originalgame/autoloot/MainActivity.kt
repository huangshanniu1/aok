package com.originalgame.autoloot

import android.content.*
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity: AppCompatActivity(){
    private var visualFlag=false
    private lateinit var list:LinearLayout
    override fun onCreate(b:Bundle?){super.onCreate(b); build()}
    private fun build(){ val root=LinearLayout(this);root.orientation=LinearLayout.VERTICAL;root.setPadding(24,24,24,24);root.setBackgroundColor(Color.rgb(18,19,23));
        val title=TextView(this);title.text="自动跑刀 · 手势录制器";title.textSize=24f;title.setTextColor(Color.WHITE);root.addView(title)
        val sub=TextView(this);sub.text="原创游戏专用：录制 → 编辑 → 视觉条件 → 自动执行";sub.setTextColor(0xffb7bac3.toInt());sub.setPadding(0,6,0,18);root.addView(sub)
        root.addView(btn("① 开启无障碍服务"){startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))})
        root.addView(btn("② 开启悬浮窗权限"){startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))})
        root.addView(btn("● 开始录制 / 停止录制"){ service()?.let{ if(!Settings.canDrawOverlays(this)){toast("请先开启悬浮窗权限");return@let}; if(visualFlag){it.stop();visualFlag=false}; it.startRecording() } ?: toast("请先开启无障碍服务") })
        root.addView(btn("▶ 回放已保存动作"){service()?.play() ?: toast("请先开启无障碍服务")})
        root.addView(btn("👁 自动视觉跑刀 / 停止"){service()?.let{ if(visualFlag) {it.stop(); visualFlag=false} else {it.startVision(); visualFlag=true} }})
        root.addView(btn("■ 停止自动执行"){service()?.stop(); visualFlag=false})
        root.addView(btn("✎ 编辑动作"){showEditor()})
        root.addView(btn("👁 视觉规则设置"){showRules()})
        val hint=TextView(this);hint.text="录制时：白色小圆点代表动作；点击/长按/滑动自动分类。编辑器可拖动坐标、改持续时间、间隔和启用状态。视觉识别默认按紫色像素与搜索区域亮度判断，可自行调整区域。";hint.setTextColor(0xffd0d2d8.toInt());hint.setPadding(0,18,0,0);root.addView(hint)
        setContentView(root)
    }
    private fun service()=AutoAccessibilityService.instance
    private fun btn(t:String,f:()->Unit)=Button(this).apply{text=t;setOnClickListener{f()}}
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_SHORT).show()
    private fun showEditor(){
        val a=ActionStore.load(this); val root=LinearLayout(this);root.orientation=LinearLayout.VERTICAL;root.setBackgroundColor(0xff0e0f12.toInt());
        val canvas=ActionEditorView(this,a){ActionStore.save(this,a)};root.addView(canvas,LinearLayout.LayoutParams(-1,0,1f));
        val bar=LinearLayout(this);bar.orientation=LinearLayout.HORIZONTAL;
        val addTap=Button(this);addTap.text="+点击";addTap.setOnClickListener{a+=Action("tap",canvas.width/2f,canvas.height/2f,0f,0f,80,120,true);ActionStore.save(this,a);canvas.invalidate()};bar.addView(addTap);
        val addLong=Button(this);addLong.text="+长按";addLong.setOnClickListener{a+=Action("long",canvas.width/2f,canvas.height/2f,0f,0f,800,120,true);ActionStore.save(this,a);canvas.invalidate()};bar.addView(addLong);
        val addSwipe=Button(this);addSwipe.text="+滑动";addSwipe.setOnClickListener{a+=Action("swipe",canvas.width*.35f,canvas.height*.6f,canvas.width*.7f,canvas.height*.6f,450,120,true);ActionStore.save(this,a);canvas.invalidate()};bar.addView(addSwipe);
        val back=Button(this);back.text="返回";back.setOnClickListener{build()};bar.addView(back);root.addView(bar);setContentView(root)
    }
    private fun showRules(){ val ll=LinearLayout(this);ll.orientation=LinearLayout.VERTICAL;ll.setPadding(24,24,24,24);ll.setBackgroundColor(0xff121317.toInt());val title=TextView(this);title.text="视觉规则（归一化 0~1）";title.textSize=22f;title.setTextColor(Color.WHITE);ll.addView(title)
        val inputs=mutableListOf<EditText>(); fun field(label:String,value:String):EditText{val t=TextView(this);t.text=label;t.setTextColor(Color.WHITE);ll.addView(t);val e=EditText(this);e.setText(value);e.setTextColor(Color.WHITE);ll.addView(e);inputs+=e;return e}
        field("搜索按钮区域 l,t,r,b","0.72,0.75,0.99,0.99"); field("高品质物资区域 l,t,r,b","0.10,0.15,0.90,0.85");field("紫色 Hue 最小值","265");field("紫色 Hue 最大值","325");field("饱和度下限","0.34");field("亮度下限","0.35");val save=Button(this);save.text="保存规则（示例配置会先写入本地）";save.setOnClickListener{toast("规则字段已准备；运行服务后会采用默认区域。源码中可继续扩展 OCR/模板匹配")};ll.addView(save);setContentView(ll)}
}
