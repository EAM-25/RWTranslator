package com.eam.rwtranslator.ui.editor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.eam.rwtranslator.R;
import com.eam.rwtranslator.databinding.ActivityExtendEditorBinding;
public class ExtendEditorActivity extends AppCompatActivity {
    ActivityExtendEditorBinding binding;
    EditText tranEditText;
    private long firstBackTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExtendEditorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tranEditText = binding.extendTranEditText;
        Intent i = getIntent();
        tranEditText.setText(i.getStringExtra("tran"));
        binding.toolbar.setNavigationOnClickListener(id -> returnEditText());
        binding.toolbar.setTitle(i.getStringExtra("key"));
    }
    @Override
    protected void onDestroy() {
        returnEditText();
        super.onDestroy();
    }
    private void returnEditText() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(tranEditText.getWindowToken(),0);
        Intent i = new Intent();
        i.putExtra("tranEditText", tranEditText.getText().toString());
        i.putExtra("index",getIntent().getIntExtra("index",-1));
        setResult(RESULT_OK, i);
        finish();
    }
   
}
