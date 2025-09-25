package com.eam.rwtranslator.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import com.eam.rwtranslator.R;

/**
 * URL跳转工具类，用于复用URL打开逻辑
 */
public class UrlUtils {
    
    /**
     * 打开指定URL
     * @param context 上下文
     * @param url 要打开的URL
     */
    public static void openUrl(Context context, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse(url));
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, context.getString(R.string.setting_act_open_url_failed_message), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    /**
     * 打开GitHub仓库页面
     * @param context 上下文
     */
    public static void openGitHubRepo(Context context) {
        openUrl(context, "https://github.com/EAM-25/RWTranslator");
    }
    
    /**
     * 打开GitHub Release页面
     * @param context 上下文
     */
    public static void openGitHubReleases(Context context) {
        openUrl(context, "https://github.com/EAM-25/RWTranslator/releases");
    }
}