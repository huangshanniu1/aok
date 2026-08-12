# 自动跑刀录制器（原创游戏）

这是一个 Android 端原型：
- 悬浮白色圆球：点击开始/停止录制。
- 录制把触摸动作分类为 tap / long / swipe，并保存到本地。
- 使用 Android AccessibilityService 的 GestureDescription 回放动作。
- 视觉检测采用 AccessibilityService 截图 + 可配置屏幕区域：搜索按钮区域使用亮度检测，高品质物资区域使用紫色 HSV 检测。
- GitHub Actions 自动编译 APK，无需电脑。

## 手机端打包
1. GitHub 新建空仓库。
2. 解压本 ZIP，把所有文件上传到仓库根目录。
3. 打开 Actions，运行 `Build APK`；首次 push 到 main/master 也会自动构建。
4. 在 Actions 对应运行记录底部的 Artifacts 下载 `AutoLootRecorder-debug-apk`。
5. Android 安装 APK 后，依次打开：
   - 无障碍 → 自动跑刀录制器 → 允许
   - “显示在其他应用上层” → 允许

## 原创游戏调试建议
先把“搜索按钮区域”和“高品质物资区域”限定到你的游戏 UI 区域，再逐步降低阈值。当前视觉原型没有联网，也不依赖服务器。

## 重要限制
Android 本身不允许普通应用透明地读取任意应用的原始触摸输入；本项目的录制模式使用全屏覆盖层捕获动作，然后立即用 AccessibilityService 重放，所以录制期间动作会被短暂拦截。若你的原创游戏源码可控，最稳定的方案是在游戏内部加入测试桥接（例如把按钮/物资状态暴露给自动化模块），再由本应用消费这些状态。
