package com.fliqlo.clock;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Fliqlo 翻页时钟的全屏 WebView 壳。
 *
 * 只做四件事：
 *   1. 页面从 assets 本地加载，全程不联网，启动没有网络等待；
 *   2. 真全屏 + 不熄屏，且不给 WebView 加任何内边距，让 CSS 视口严格等于屏幕；
 *   3. 关掉 WebView 自带的「按内容宽度缩放」和「跟随系统字体放大」，
 *      这两项是导致页面整体偏移 / 数字撑出卡片的主因；
 *   4. 窗口底色先铺黑，避免启动瞬间的白闪。
 */
public class MainActivity extends Activity {

    private static final String PAGE_URL = "file:///android_asset/index.html";

    private WebView web;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        web = new WebView(this);
        web.setBackgroundColor(Color.BLACK);
        web.setOverScrollMode(View.OVER_SCROLL_NEVER);
        web.setVerticalScrollBarEnabled(false);
        web.setHorizontalScrollBarEnabled(false);
        web.setLongClickable(false);
        web.setOnLongClickListener(v -> true);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);        // 页面把缩放/亮度/显秒记在 localStorage
        s.setUseWideViewPort(true);          // 尊重页面自己的 <meta name="viewport">
        s.setLoadWithOverviewMode(false);    // 关键：不让 WebView 再按内容宽度缩一次
        s.setTextZoom(100);                  // 关键：不吃系统字体缩放（Flyme / MIUI / EMUI 会放大）
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setAllowFileAccess(true);
        s.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        web.setWebViewClient(new WebViewClient());
        setContentView(web);

        goFullscreen();
        web.loadUrl(PAGE_URL);
    }

    /** 隐藏状态栏与导航栏；用户从边缘划出系统栏时不改变布局尺寸。 */
    private void goFullscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                controller.hide(WindowInsets.Type.statusBars()
                        | WindowInsets.Type.navigationBars());
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        // 刘海 / 挖孔：内容延伸到整个屏幕，居中才准
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            goFullscreen();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (web != null) {
            web.onResume();
        }
        goFullscreen();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (web != null) {
            // 切后台就停掉定时器：回来时会重新按系统时间对齐，顺便省电
            web.onPause();
        }
    }

    @Override
    public void onBackPressed() {
        // 全屏时钟：吞掉返回键，避免躺着看时误触退出。要离开请按 Home。
    }

    @Override
    protected void onDestroy() {
        if (web != null) {
            web.destroy();
            web = null;
        }
        super.onDestroy();
    }
}
