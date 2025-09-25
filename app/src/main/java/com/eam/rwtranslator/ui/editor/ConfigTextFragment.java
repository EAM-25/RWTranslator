package com.eam.rwtranslator.ui.editor;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import androidx.annotation.MainThread;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import com.eam.rwtranslator.R;
import com.eam.rwtranslator.ui.editor.SectionEditorAdapter;
import com.eam.rwtranslator.ui.editor.ConfigTextFragmentAdapter;
import com.eam.rwtranslator.data.model.SectionModel;
import com.eam.rwtranslator.ui.project.TranslationConfigManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import timber.log.Timber;

public class ConfigTextFragment extends AppCompatDialogFragment {

  Context context;
  SectionModel.Pair item;
  View view;
  ConfigTextFragmentAdapter adapter;
  SectionEditorAdapter dadapter;

  public ConfigTextFragment(
      Context context,
      View view,
      SectionModel.Pair item,
      ConfigTextFragmentAdapter adapter,
      SectionEditorAdapter dadapter) {
    this.context = context;
    this.view = view;
    this.item = item;
    this.adapter = adapter;
    this.dadapter = dadapter;
  }

  @Override
  public Dialog onCreateDialog(Bundle arg0) {
    // TODO: Implement this method
    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
    builder.setTitle(item.getKey().getKeyName());
    builder.setView(view);
    builder.setPositiveButton(
        R.string.positive_button,
        (mdialog, which) -> {
          mdialog.dismiss();
          boolean[] res = adapter.getModificationState();
          // Timber.d("%b_%b", res[0], res[1]);
          if (context instanceof SectionEditorActivity) {
            SectionEditorActivity activity = (SectionEditorActivity) context;
            if (res[0] || res[1]) {
              activity.setModified(true);
              if (res[1]) {
                dadapter.notifyDataSetChanged();
              }
            } else {
              activity.setModified(false);
            }
          }
        });
    builder.setNegativeButton(R.string.negative_button, null);
    AlertDialog dialog = builder.create();
    /* dialog
    .getWindow()
    .clearFlags(
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);*/
    dialog
        .getWindow()
        .setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
                | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    return dialog;
  }

  @Override
  @MainThread
  public void onDestroyView() {
    super.onDestroyView();
    // TODO: Implement this method
  }

  public interface ModificationStateCallBack {
    boolean[] getModificationState();
  }
}
