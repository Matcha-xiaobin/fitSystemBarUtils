# FitSystemBarUtils

沉浸式状态栏，一个自动处理WindowInsets和cutOut的工具，一行代码 给任意view绑定。
你还在苦恼布局中背景图需要上到状态栏或者虚拟导航键下面，却写不好其他控件的布局吗？
你还在获取状态栏高度的方式去给布局设置一个paddingTop吗？
快来试试看这个吧。
功能非常简单，没几行代码。

可以实现软键盘弹起时，平滑的推起底部的输入框哦！(比微信还丝滑)

## Demo录屏gif:
<img src="gif/screen.gif" width="320px" >

## 在项目中引用

### Gradle

Step 1. Add it in your root build.gradle at the end of repositories

    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }

Step 2. Add the dependency

[![](https://jitpack.io/v/Matcha-xiaobin/fitSystemBarUtils.svg)](https://jitpack.io/#Matcha-xiaobin/fitSystemBarUtils)

    dependencies {
        implementation 'com.github.Matcha-xiaobin:fitSystemBarUtils:1.0.3'
    }

### 目前BUG：
突然发现在Android 10系统上出现了系统栏明明隐藏了，高度却没有变成0的问题，android 11+似乎没问题。
目前已修复，并且禁用了低于android 11的动画效果，实际上ViewCompat中帮我们实现了低于android 11的设备的动画效果，但是存在问题。

### 在代码中使用：
这个工具本质是把系统分发的insets属性拿出来用了一下，可以参考下google的 BottomNavigationView 底部导航 的代码，里面也用到了这个原理。
注意：如果你不知道这个时干啥用的，建议下载demo看看实际效果是不是你需要的效果
    
任意View实现FitSystemBar效果
中间内容区域上下两边不需要填充，因为上面有标题栏，下面有输入框栏

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

获取这个View已绑定的FitSystemBarHelper对象
    
    view.fitSystemBarHelper()

取消绑定

    view.fitSystemBarHelper()?.destory()

### 使用DataBinding在xml中使用
每个属性的默认值为false
是否处理Start方向的

    fitStart="@{true}" 

是否处理上方的

    fitTop="@{true}" 

是否处理End方向的

    fitEnd="@{true}" 

是否处理下方的

    fitBottom="@{true}" 

### 自定义View
自带了3个：
FitNavigationBarFrameLayout 用作根布局最底下的不需要处理顶部状态栏区域的填充的 FrameLayout
FitNavigationBarLinearLayout 用作根布局最底下的不需要处理顶部状态栏区域的填充的 LinearLayout
FitStatusBarLinearLayout 用作根布局最上方的不需要处理底部导航栏区域的填充的 LinearLayout
直接参考即可，甚至可以直接拷贝一模一样，将继承的目标换成你需要的View即可
