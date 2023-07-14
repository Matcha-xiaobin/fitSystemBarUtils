package com.xiaobin.fitsystembar

import android.os.Build
import android.os.Build.VERSION_CODES
import android.util.Log
import android.view.View
import android.view.View.OnAttachStateChangeListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.BindingAdapter
import kotlin.math.max

/**
 * 处理系统栏的遮挡问题
 * 原理是通过监听insets属性的变化得知系统栏的可见的高度
 * 同时这里也处理了挖孔屏的问题，但只是简单处理
 * 如果屏幕包含挖孔，且safeCutOutPadding没有设为false，则有挖孔的那条边，无论系统栏是否可见，
 * 都会被附加一个挖孔高度的padding
 * 当系统栏可见并且有挖孔时，padding取最大的
 *
 * 注意: 绑定的view在xml中请勿使用paddingLeft、paddingRight，请使用paddingStart、paddingEnd
 *
 * Tips: 系统栏包含 底部虚拟导航栏 和 顶部状态栏 以及 软键盘
 *       平滑填充指系统栏在显示和隐藏的动画执行过程中，实时填充
 * @author matcha_xiaobin
 */
class FitSystemBarHelper(
    private val view: View,
    private val TAG: String = "FitSystemBarHelper",
    private val callBack: (orientation: Orientation) -> Boolean
) {

    internal lateinit var initialPadding: RelativePadding
        private set

    //当前是否正在平滑填充中
    var inSmoothingPadding = false
        private set

    //是否需要处理挖孔屏，刘海屏
    var safeCutOutPadding: Boolean = true
        set(value) {
            field = value
            if (ViewCompat.isAttachedToWindow(view)) {
                ViewCompat.requestApplyInsets(view)
            }
        }

    //是否需要平滑填充，仅在android R及以上生效，默认开启
    var smoothPadding: Boolean = true

    //是否需要打印日志
    var debug = false

    enum class Orientation {
        Start, Top, End, Bottom
    }

    companion object {

        /**
         * 获取这个view已绑定的FitSystemBarHelper对象
         */
        fun View.fitSystemBarHelper(): FitSystemBarHelper? {
            val helper = this.getTag(R.id.fit_system_helper)
            if (helper is FitSystemBarHelper) return helper
            return null
        }

        /**
         * 绑定到View
         * @param view 指定View
         * @param callBack 用于判断是否需要某条边的padding，会非常频繁的调用，所以不要在里面做需要耗时的操作，以免卡UI线程
         */
        fun attachView(view: View, callBack: (orientation: Orientation) -> Boolean) =
            FitSystemBarHelper(view, callBack = callBack)

        /**
         * 绑定到View
         * @param view 指定View
         * @param start 是否需要paddingStart
         * @param top 是否需要paddingTop
         * @param end 是否需要paddingEnd
         * @param bottom 是否需要paddingBottom
         */
        fun attachView(
            view: View,
            start: Boolean = false,
            top: Boolean = false,
            end: Boolean = false,
            bottom: Boolean = false
        ) = attachView(view) {
            when (it) {
                Orientation.Start -> start
                Orientation.Top -> top
                Orientation.End -> end
                Orientation.Bottom -> bottom
            }
        }

        /**
         * 绑定到View
         * @param start 是否需要paddingStart
         * @param top 是否需要paddingTop
         * @param end 是否需要paddingEnd
         * @param bottom 是否需要paddingBottom
         */
        @BindingAdapter(
            value = ["fitStart", "fitTop", "fitEnd", "fitBottom"],
            requireAll = false
        )
        fun View.fitSystemBar(
            start: Boolean = false,
            top: Boolean = false,
            end: Boolean = false,
            bottom: Boolean = false
        ) {
            attachView(this) {
                when (it) {
                    Orientation.Start -> start
                    Orientation.Top -> top
                    Orientation.End -> end
                    Orientation.Bottom -> bottom
                }
            }
        }

    }

    private var attachStateChangeListener: OnAttachStateChangeListener =
        object : OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                ViewCompat.requestApplyInsets(v)
            }

            override fun onViewDetachedFromWindow(v: View) {}
        }

    init {
        attach()
    }

    fun requestApplyInsets() {
        if (ViewCompat.isAttachedToWindow(view)) {
            ViewCompat.requestApplyInsets(view)
        }
    }

    /**
     * 添加绑定
     */
    fun attach() {
        //一个view只让绑定一个helper，如果多个绑定同一个view，则取消之前的
        val helper = view.getTag(R.id.fit_system_helper)
        if (helper == this) return
        initialPadding = if (helper is FitSystemBarHelper) {
            helper.destory()
            //获取原始padding的快照
            RelativePadding(helper.initialPadding)
        } else {
            //创建原始padding的快照
            RelativePadding(
                ViewCompat.getPaddingStart(view),
                view.paddingTop,
                ViewCompat.getPaddingEnd(view),
                view.paddingBottom
            )
        }
        view.setTag(R.id.fit_system_helper, this@FitSystemBarHelper)
        view.fitsSystemWindows = true
        //不带平滑变化的
        ViewCompat.setOnApplyWindowInsetsListener(view) { view: View, insets: WindowInsetsCompat ->
            //当正在平滑填充时，不做处理，否则你会看到这个view在反复弹跳
            if (inSmoothingPadding) return@setOnApplyWindowInsetsListener insets
            //即使开启了平滑填充，这个也必须存在，否则，一些情况下会导致没有填充而被系统栏遮挡
            formatInsets(insets, RelativePadding(initialPadding))
            insets
        }
        //带平滑变化的
        ViewCompat.setWindowInsetsAnimationCallback(
            view,
            object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE) {

                override fun onProgress(
                    insets: WindowInsetsCompat,
                    runningAnimations: List<WindowInsetsAnimationCompat>
                ): WindowInsetsCompat {
                    if (smoothPadding) {
                        formatInsets(insets, RelativePadding(initialPadding))
                    }
                    return insets
                }

                override fun onEnd(animation: WindowInsetsAnimationCompat) {
                    inSmoothingPadding = false
                    super.onEnd(animation)
                }

                override fun onPrepare(animation: WindowInsetsAnimationCompat) {
                    inSmoothingPadding = smoothPadding
                    super.onPrepare(animation)
                }
            })

        if (ViewCompat.isAttachedToWindow(view)) {
            ViewCompat.requestApplyInsets(view)
        } else {
            view.addOnAttachStateChangeListener(attachStateChangeListener)
        }
    }

    /**
     * 针对不同版本处理Insets
     */
    private fun formatInsets(
        insetsCompat: WindowInsetsCompat,
        initialPadding: RelativePadding
    ) {
        var cutoutPaddingLeft = 0
        var cutoutPaddingTop = 0
        var cutoutPaddingRight = 0
        var cutoutPaddingBottom = 0

        val systemWindowInsetLeft: Int
        val systemWindowInsetTop: Int
        val systemWindowInsetRight: Int
        val systemWindowInsetBottom: Int

        if (Build.VERSION.SDK_INT > VERSION_CODES.P) {
            if (safeCutOutPadding) {
                insetsCompat.displayCutout?.let {
                    cutoutPaddingTop = it.safeInsetTop
                    cutoutPaddingLeft = it.safeInsetLeft
                    cutoutPaddingRight = it.safeInsetRight
                    cutoutPaddingBottom = it.safeInsetBottom
                }
            }
        }
        if (Build.VERSION.SDK_INT > VERSION_CODES.R) {
            val systemBars = insetsCompat.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insetsCompat.getInsets(WindowInsetsCompat.Type.ime())
            val maxInsets = androidx.core.graphics.Insets.max(ime, systemBars)
            systemWindowInsetLeft = systemBars.left
            systemWindowInsetRight = systemBars.right
            systemWindowInsetTop = systemBars.top
            systemWindowInsetBottom = maxInsets.bottom
        } else {
            systemWindowInsetLeft = insetsCompat.systemWindowInsetLeft
            systemWindowInsetRight = insetsCompat.systemWindowInsetRight
            systemWindowInsetTop = insetsCompat.systemWindowInsetTop
            systemWindowInsetBottom = insetsCompat.systemWindowInsetBottom
        }

        if (callBack.invoke(Orientation.Top)) {
            initialPadding.top += max(systemWindowInsetTop, cutoutPaddingTop)
        }
        if (callBack.invoke(Orientation.Bottom)) {
            initialPadding.bottom += max(systemWindowInsetBottom, cutoutPaddingBottom)
        }

        val isRtl =
            ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL
        if (callBack.invoke(Orientation.Start)) {
            initialPadding.start += if (isRtl) {
                max(systemWindowInsetRight, cutoutPaddingRight)
            } else {
                max(systemWindowInsetLeft, cutoutPaddingLeft)
            }
        }
        if (callBack.invoke(Orientation.End)) {
            initialPadding.end += if (isRtl) {
                max(systemWindowInsetLeft, cutoutPaddingLeft)
            } else {
                max(systemWindowInsetRight, cutoutPaddingRight)
            }
        }
        initialPadding.applyToView(view)
        if (debug) {
            Log.d(
                TAG, "处理Insets后的padding: " +
                        "Start: " + initialPadding.start + ", " +
                        "Top: " + initialPadding.top + ", " +
                        "End: " + initialPadding.end + ", " +
                        "Bottom: " + initialPadding.bottom + "\n" +
                        "Insets的padding: " +
                        "Left: " + systemWindowInsetLeft + ", " +
                        "Top: " + systemWindowInsetTop + ", " +
                        "Right: " + systemWindowInsetRight + ", " +
                        "Bottom: " + systemWindowInsetBottom
            )
        }
    }

    fun destory() {
        //取消所有监听
        view.removeOnAttachStateChangeListener(attachStateChangeListener)
        ViewCompat.setOnApplyWindowInsetsListener(view, null)
        ViewCompat.setWindowInsetsAnimationCallback(view, null)
        //恢复原始padding
        initialPadding.applyToView(view)
        //移除tag
        view.setTag(R.id.fit_system_helper, null)
    }

    internal class RelativePadding {
        var start: Int
        var top: Int
        var end: Int
        var bottom: Int

        constructor(start: Int, top: Int, end: Int, bottom: Int) {
            this.start = start
            this.top = top
            this.end = end
            this.bottom = bottom
        }

        constructor(other: RelativePadding) {
            start = other.start
            top = other.top
            end = other.end
            bottom = other.bottom
        }

        /**
         * Applies this relative padding to the view.
         */
        fun applyToView(view: View?) {
            ViewCompat.setPaddingRelative(view!!, start, top, end, bottom)
        }
    }
}