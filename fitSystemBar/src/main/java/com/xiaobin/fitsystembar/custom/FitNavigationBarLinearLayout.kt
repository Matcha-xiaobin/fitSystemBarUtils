package com.xiaobin.fitsystembar.custom

import android.content.Context
import android.util.AttributeSet
import com.xiaobin.fitsystembar.base.FitSystemBarLinearLayout

/**
 * 设置padding为系统栏的高度，但paddingTop方向的padding不变
 */
class FitNavigationBarLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FitSystemBarLinearLayout(context, attrs, defStyleAttr) {

    init {
        paddingTopSystemWindowInsets = false
    }

}