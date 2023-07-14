package com.xiaobin.fitsystembar.base

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import com.xiaobin.fitsystembar.R
import com.xiaobin.fitsystembar.FitSystemBarHelper
import com.xiaobin.fitsystembar.listener.OnLayoutRectChangeListener

/**
 * 设置paddingTop为顶部系统栏的高度，其它方向的padding不变
 */
open class FitSystemBarLinearLayout @JvmOverloads constructor(
    context: Context,
    val attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    protected val TAG by lazy {
        this.javaClass.simpleName
    }

    protected var paddingTopSystemWindowInsets: Boolean? = null
    protected var paddingBottomSystemWindowInsets: Boolean? = null
    protected var paddingStartSystemWindowInsets: Boolean? = null
    protected var paddingEndSystemWindowInsets: Boolean? = null

    private var onLayoutRectChangeListener = mutableListOf<OnLayoutRectChangeListener>()

    private val helper by lazy {
        FitSystemBarHelper(this, TAG) {
            when (it) {
                FitSystemBarHelper.Orientation.Start -> shouldApplyWindowInsetPadding(
                    paddingStartSystemWindowInsets
                )

                FitSystemBarHelper.Orientation.Top -> shouldApplyWindowInsetPadding(
                    paddingTopSystemWindowInsets
                )

                FitSystemBarHelper.Orientation.End -> shouldApplyWindowInsetPadding(
                    paddingEndSystemWindowInsets
                )

                FitSystemBarHelper.Orientation.Bottom -> shouldApplyWindowInsetPadding(
                    paddingBottomSystemWindowInsets
                )
            }
        }
    }

    fun safeCutOutPadding(enable: Boolean) {
        helper.safeCutOutPadding = enable
    }

    fun smoothPadding(enable: Boolean) {
        helper.smoothPadding = enable
    }

    fun enableDebugLogger(enable: Boolean) {
        helper.debug = enable
    }

    fun updateSystemPaddingInsets(start: Boolean, top: Boolean, end: Boolean, bottom: Boolean) {
        paddingTopSystemWindowInsets = top
        paddingBottomSystemWindowInsets = bottom
        paddingStartSystemWindowInsets = start
        paddingEndSystemWindowInsets = end
        ViewCompat.requestApplyInsets(this)
    }

    init {
        fitsSystemWindows = true
        attrs?.let {
            val a = getContext().obtainStyledAttributes(attrs, R.styleable.FitSystemBarLinearLayout)
            if (a.hasValue(R.styleable.FitSystemBarLinearLayout_fs_fitSystemTop)) {
                paddingTopSystemWindowInsets =
                    a.getBoolean(R.styleable.FitSystemBarLinearLayout_fs_fitSystemTop, true)
            }
            if (a.hasValue(R.styleable.FitSystemBarLinearLayout_fs_fitSystemBottom)) {
                paddingBottomSystemWindowInsets =
                    a.getBoolean(R.styleable.FitSystemBarLinearLayout_fs_fitSystemBottom, true)
            }
            if (a.hasValue(R.styleable.FitSystemBarLinearLayout_fs_fitSystemStart)) {
                paddingStartSystemWindowInsets =
                    a.getBoolean(R.styleable.FitSystemBarLinearLayout_fs_fitSystemStart, true)
            }
            if (a.hasValue(R.styleable.FitSystemBarLinearLayout_fs_fitSystemEnd)) {
                paddingEndSystemWindowInsets =
                    a.getBoolean(R.styleable.FitSystemBarLinearLayout_fs_fitSystemEnd, true)
            }
            if (a.hasValue(R.styleable.FitSystemBarLinearLayout_fs_safeCutOutPadding)) {
                helper.safeCutOutPadding =
                    a.getBoolean(R.styleable.FitSystemBarLinearLayout_fs_safeCutOutPadding, true)
            }
            if (a.hasValue(R.styleable.FitSystemBarLinearLayout_fs_debug)) {
                helper.debug =
                    a.getBoolean(R.styleable.FitSystemBarLinearLayout_fs_debug, false)
            }
            a.recycle()
        }
        helper.attach()
    }

    fun addOnLayoutRectChangeListener(listener: OnLayoutRectChangeListener) {
        if (!onLayoutRectChangeListener.contains(listener)) {
            onLayoutRectChangeListener.add(listener)
        }
    }

    fun removeOnLayoutRectChangeListener(listener: OnLayoutRectChangeListener) {
        if (onLayoutRectChangeListener.contains(listener)) {
            onLayoutRectChangeListener.remove(listener)
        }
    }

    /**
     * Whether the top or bottom of this view should be padded in to avoid the system window insets.
     *
     * If the `paddingInsetFlag` is set, that value will take precedent. Otherwise,
     * fitsSystemWindow will be used.
     */
    private fun shouldApplyWindowInsetPadding(paddingInsetFlag: Boolean?): Boolean {
        return paddingInsetFlag ?: ViewCompat.getFitsSystemWindows(this)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        onLayoutRectChangeListener.forEach {
            it.onLayoutChange(changed, left, top, right, bottom)
        }
    }
}