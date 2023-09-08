package com.xiaobin.fitsystembar

import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

/**
 * 系统UI沉浸式工具箱
 * 需要依赖Androidx：
 * androidx.core:core-ktx:1.5.0 或者更高
 *
 * @author xiaobin
 * @data 2022/6/30 00:00
 */

/**
 * 附带一个工具
 * 允许或禁止 在当前app界面 截屏 和 录屏
 */
fun Window.disabledScreenShot(disabled: Boolean = true) {
    if (disabled) {
        //设置禁止截屏、录屏标志
        addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    } else {
        //清除禁止截屏、录屏标志
        clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}

/**
 * 显示或隐藏软键盘，但是必须界面上有可以输入的控件并且可以获取到焦点才会生效
 */
fun Window.showIme(view: View = decorView, show: Boolean = true) {
    val insetsController = WindowCompat.getInsetsController(this, view)
    if (show) {
        insetsController.show(WindowInsetsCompat.Type.ime())
    } else {
        insetsController.hide(WindowInsetsCompat.Type.ime())
    }
}

/**
 * 显示或隐藏 captionBar
 */
fun Window.showCaptionBar(view: View = decorView, show: Boolean = true) {
    val insetsController = WindowCompat.getInsetsController(this, view)
    if (show) {
        insetsController.show(WindowInsetsCompat.Type.captionBar())
    } else {
        insetsController.hide(WindowInsetsCompat.Type.captionBar())
    }
}

/**
 * 控制系统状态栏和导航栏显隐
 * @param behavior BEHAVIOR_SHOW_BARS_BY_TOUCH 任何操作都将会重新将隐藏了的系统栏显示出来
 *                 BEHAVIOR_SHOW_BARS_BY_SWIPE 边缘滑动向内滑动会重新将隐藏了的系统栏显示出来
 *                 BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE 边缘滑动向内滑动会重新将隐藏了的系统栏显示出来，
 *                                                       但过一会儿会重新自动隐藏
 */
fun Window.showSystemBar(
    show: Boolean,
    behavior: Int = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
) {
    val insetsController = WindowCompat.getInsetsController(this, decorView)
    insetsController.systemBarsBehavior = behavior
    if (show) {
        insetsController.show(WindowInsetsCompat.Type.systemBars())
    } else {
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
}

/**
 * 控制状态栏显隐
 * @param behavior 参考 showSystemBar 的描述
 */
fun Window.showStatusBar(
    show: Boolean,
    behavior: Int = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
) {
    val insetsController = WindowCompat.getInsetsController(this, decorView)
    insetsController.systemBarsBehavior = behavior
    if (show) {
        insetsController.show(WindowInsetsCompat.Type.statusBars())
    } else {
        insetsController.hide(WindowInsetsCompat.Type.statusBars())
    }
}

/**
 * 控制导航栏显隐
 * @param behavior 参考 showSystemBar 的描述
 */
fun Window.showNavigationBar(
    show: Boolean,
    behavior: Int = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
) {
    val insetsController = WindowCompat.getInsetsController(this, decorView)
    insetsController.systemBarsBehavior = behavior
    if (show) {
        insetsController.show(WindowInsetsCompat.Type.navigationBars())
    } else {
        insetsController.hide(WindowInsetsCompat.Type.navigationBars())
    }
}

/**
 * 状态栏以及虚拟导航栏图标主题
 * @param lightStatusBar 状态栏 true: 浅色背景  false: 深色背景
 * @param lightNavigationBar 导航栏 true: 浅色背景  false: 深色背景
 */
fun Window.setSystemBarTheme(
    lightStatusBar: Boolean,
    lightNavigationBar: Boolean = lightStatusBar
) {
    //低于6.0的不做处理了
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        //先开启全屏模式
        WindowCompat.setDecorFitsSystemWindows(this, false)
        //状态栏相关，仅android 6.0+生效
        setStatusBarTheme(lightStatusBar)
        //虚拟导航键相关，仅android 8.0+生效
        setNavigationBarTheme(lightNavigationBar)
    }
}

/**
 * 状态栏图标主题
 * 仅在Android6.0以上才官方支持状态栏深色浅色转换
 *
 * @param mask 是否增加遮罩 (正常情况下，状态栏图标是白色，遮罩将会使其变为黑色)
 */
fun Window.setStatusBarTheme(mask: Boolean) {
    val insetsController = WindowCompat.getInsetsController(this, decorView)
    insetsController.isAppearanceLightStatusBars = mask
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        //状态栏设置透明
        statusBarColor = Color.TRANSPARENT
    }
}

/**
 * 导航栏图标主题
 * 仅在Android8.0以上才官方支持导航栏深色浅色转换
 *
 * @param window 需要设置的窗口
 * @param mask 是否增加遮罩 (正常情况下，导航栏图标是白色，遮罩将会使其变为黑色，但国内UI似乎和谷歌原生处理方式不一致)
 */
fun Window.setNavigationBarTheme(mask: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        isNavigationBarContrastEnforced = false
    }
    val insetsController = WindowCompat.getInsetsController(this, decorView)
    insetsController.isAppearanceLightNavigationBars = mask
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        navigationBarColor = Color.TRANSPARENT
    } else {
        navigationBarColor = Color.BLACK
    }
}

/**
 * 获取状态栏高度，未测试
 */
fun Window.getStatusBarHeight(): Int {
    if (ViewCompat.getFitsSystemWindows(decorView)) {
        return decorView.paddingTop
    }
    return ViewCompat.getRootWindowInsets(decorView)
        ?.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.statusBars())
        ?.top
        ?: -1
}

/**
 * 判断颜色是否为浅色
 *
 * @param color
 * @return
 */
fun Int.isLightColor(): Boolean {
    val darkness =
        1 - (0.299 * Color.red(this) + 0.587 * Color.green(this) + 0.114 * Color.blue(this)) / 255
    return darkness < 0.5
}