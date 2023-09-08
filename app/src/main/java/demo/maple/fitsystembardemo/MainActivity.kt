package demo.maple.fitsystembardemo

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import com.xiaobin.fitsystembar.FitSystemBarHelper
import com.xiaobin.fitsystembar.FitSystemBarHelper.Companion.fitSystemBarHelper
import com.xiaobin.fitsystembar.showNavigationBar
import com.xiaobin.fitsystembar.showStatusBar
import com.xiaobin.fitsystembar.showSystemBar
import demo.maple.fitsystembardemo.databinding.ActivityMainBinding

/**
 * AndroidManifest.xml中给activity配置了：
 * android:windowSoftInputMode="adjustResize"
 */
class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 任意View实现FitSystemBar效果
        // 中间内容区域上下两边不需要填充，因为上面有标题栏，下面有输入框栏
        FitSystemBarHelper.attachView(
            binding.content,
            start = true,
            top = false,
            end = true,
            bottom = false
        )
        //第二种写法, 这种写法支持动态改变，但改变后的生效时间由系统分发WindowInsets的时机决定
        //如果需要立即生效，请在改变值后调用 fitSystemHelperForContent.requestApplyInsets()
        FitSystemBarHelper.attachView(binding.content) { orientation ->
            when (orientation) {
                FitSystemBarHelper.Orientation.Start -> true
                FitSystemBarHelper.Orientation.Top -> false
                FitSystemBarHelper.Orientation.End -> true
                FitSystemBarHelper.Orientation.Bottom -> false
            }
        }
        binding.btnSystemBarShow.setOnClickListener {
            showSystemBar(true)
        }
        binding.btnSystemBarHide.setOnClickListener {
            showSystemBar(false)
        }
        binding.btnStatusBarShow.setOnClickListener {
            window.showStatusBar(true)
        }
        binding.btnStatusBarHide.setOnClickListener {
            window.showStatusBar(false)
        }
        binding.btnNaviBarShow.setOnClickListener {
            window.showNavigationBar(true)
        }
        binding.btnNaviBarHide.setOnClickListener {
            window.showNavigationBar(false)
        }
        binding.btnSystemCutOut.setOnClickListener {
            cutOutSet(true)
        }
        binding.btnSystemNoCutOut.setOnClickListener {
            cutOutSet(false)
        }
        binding.btnSmoothPadding.setOnClickListener {
            smoothPadding(true)
        }
        binding.btnNoSmoothPadding.setOnClickListener {
            smoothPadding(false)
        }
    }

    private fun smoothPadding(enable: Boolean) {
        binding.titleBar.smoothPadding(enable)
        binding.navigationBar.smoothPadding(enable)
        binding.content.fitSystemBarHelper()?.smoothPadding = enable
    }

    private fun cutOutSet(enable: Boolean) {
        binding.titleBar.safeCutOutPadding(enable)
        binding.navigationBar.safeCutOutPadding(enable)
        binding.content.fitSystemBarHelper()?.safeCutOutPadding = enable
    }

    private fun showSystemBar(show: Boolean) {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (show) {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        } else {
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    override fun onResume() {
        super.onResume()
        //沉浸式状态栏
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //状态栏设置透明
            window.statusBarColor = Color.TRANSPARENT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        insetsController.isAppearanceLightNavigationBars = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.navigationBarColor = Color.TRANSPARENT
        } else {
            window.navigationBarColor = Color.BLACK
        }
    }
}