package com.eam.rwtranslator.utils;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.Nullable;
import com.eam.rwtranslator.R;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Optional;

/**
 * 全局对话框工具类，负责加载、选择、提示等对话框的统一创建和管理。
 * 注意：如传入Activity context，建议在Activity销毁时调用dismissLoadingDialog()，防止内存泄漏。
 */
public class DialogUtils {
  private Context context;
  private AlertDialog loadingDialog;

  public void dismissLoadingDialog() {
    if (loadingDialog != null && loadingDialog.isShowing()) {
      loadingDialog.dismiss();
    }
    loadingDialog = null;
  }

  public DialogUtils(Context context) {
    this.context = context;
  }

  public AlertDialog createLoadingDialog() {
  return this.createLoadingDialog(context.getResources().getString(R.string.loading_message));
  }

  public AlertDialog createLoadingDialog(String loadingtext) {
     dismissLoadingDialog();   
    View dialogView = LayoutInflater.from(context).inflate(R.layout.mainactivity_loading, null);
    TextView textView = dialogView.findViewById(R.id.mainactivityloadingTextView);
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
    textView.setText(loadingtext);
    builder.setView(dialogView).setCancelable(false);
    loadingDialog = builder.create();
    loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return loadingDialog;
  }

  public AlertDialog createSimpleDialog(
      String title, String message, DialogInterface.OnClickListener positiveButtonListener,DialogInterface.OnClickListener negativeButtonListener) {

    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
    builder.setTitle(title);
    builder.setMessage(message);
    builder.setPositiveButton(R.string.positive_button, positiveButtonListener);
    builder.setNegativeButton(R.string.negative_button, negativeButtonListener);
    return builder.create();
  }
public AlertDialog createSimpleDialog(
      String title, String message, DialogInterface.OnClickListener positiveButtonListener) {

    return createSimpleDialog(title,message,positiveButtonListener,null);
  }
  public AlertDialog createSingleChoiceDialog(
      String title,
      String[] items,
      int checkedItem,
      DialogInterface.OnClickListener itemClickListener,
      String positiveButtonText,
      DialogInterface.OnClickListener positiveButtonListener,
      String negativeButtonText,
      DialogInterface.OnClickListener negativeButtonListener) {
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
    builder.setTitle(title);
    builder.setSingleChoiceItems(items, checkedItem, itemClickListener);
    builder.setPositiveButton(positiveButtonText, positiveButtonListener);
    builder.setNegativeButton(negativeButtonText, negativeButtonListener);
    return builder.create();
  }

  public AlertDialog createMultiChoiceDialog(
      String title,
      String[] items,
      boolean[] checkedItems,
      DialogInterface.OnMultiChoiceClickListener itemClickListener,
      String positiveButtonText,
      DialogInterface.OnClickListener positiveButtonListener,
      String negativeButtonText,
      DialogInterface.OnClickListener negativeButtonListener) {
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
    builder.setTitle(title);
    builder.setMultiChoiceItems(items, checkedItems, itemClickListener);
    builder.setPositiveButton(positiveButtonText, positiveButtonListener);
    builder.setNegativeButton(negativeButtonText, negativeButtonListener);
    return builder.create();
  }

  public AlertDialog createListDialog(
      String title, CharSequence[] items, DialogInterface.OnClickListener itemClickListener) {
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context);
    builder.setTitle(title);
    builder.setItems(items, itemClickListener);
    builder.setNegativeButton(R.string.negative_button, null);
    return builder.create();
  }
  
}
