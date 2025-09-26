package com.eam.rwtranslator.ui.setting;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import com.eam.rwtranslator.databinding.ActivitySettingsBinding;
import com.eam.rwtranslator.R;
import android.widget.AdapterView;
import android.view.View;
import com.eam.rwtranslator.ui.setting.SettingsFragment;
import com.google.android.material.appbar.MaterialToolbar;
public class SettingActivity extends AppCompatActivity {
    ActivitySettingsBinding binding;
    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        binding=ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
       FragmentManager fragmentManager = getSupportFragmentManager();
       fragmentManager
    .beginTransaction()
    .replace(R.id.setting_framelayout, new SettingsFragment())
    .commit();
        binding.settingActToolbar.setNavigationOnClickListener(v->finish());
    }
}
