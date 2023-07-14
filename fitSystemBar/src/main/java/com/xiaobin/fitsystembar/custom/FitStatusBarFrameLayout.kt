package com.xiaobin.fitsystembar.custom

import android.content.Context
import android.util.AttributeSet
import com.xiaobin.fitsystembar.base.FitSystemBarFrameLayout

/**
 * 设置paddingTop为顶部系统栏的高度，其它方向的padding不变
 */
class FitStatusBarFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FitSystemBarFrameLayout(context, attrs, defStyleAttr) {

    init {
        paddingBottomSystemWindowInsets = false
    }
}