package com.tarang.dpq2.view.activities;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;


import com.tarang.dpq2.R;
import com.tarang.dpq2.base.AppManager;
import com.tarang.dpq2.base.Logger;
import com.tarang.dpq2.base.baseactivities.BaseActivity;
import com.tarang.dpq2.base.jpos_class.ConstantApp;
import com.tarang.dpq2.base.utilities.Utils;
import com.tarang.dpq2.model.ReconcileSetupModel;
import com.tarang.dpq2.viewmodel.MenuViewModel;
import com.tarang.dpq2.worker.PrinterWorker;

import java.util.Calendar;



public class MerchantMenuActivity extends BaseActivity implements ConstantApp, View.OnClickListener {

    LinearLayout reconciliation_setup_layout, merchantmenu_layout,simNumberLayout,changePasswordLayout,select_connection_layout;
    TextView merchantmenu_text,reconciliation_time,reconcilation_status;
    private MenuViewModel viewModel;
    ToggleButton reconciliation_enable;
    EditText sim_number_et;
    Button submit_simNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_merchantmenu);
        initData();
        viewModel = ViewModelProviders.of(this).get(MenuViewModel.class);
        viewModel.init(this, this);

        viewModel.getConnectionStatus().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                Logger.v("getConnectionStatusMerchant -"+aBoolean);
                if(aBoolean != null && aBoolean){
                    viewModel.makeSocketConnection();
                }

            }
        });

        viewModel.getShowAlert().observe(this, viewModel.observable);

        viewModel.loadKeys().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean != null && aBoolean)
                    viewModel.loadKeysIntoTerminal();
            }
        });

        viewModel.startTimer().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean)
                    viewModel.startCountDownTimer();
                else
                    viewModel.startSAFCountDownTimer();
            }
        });

        viewModel.getPrintStatus().observe(this, viewModel.printObservable);


        init();
        setTextView();
    }


    private void init() {
        reconciliation_setup_layout = (LinearLayout) findViewById(R.id.reconcilation_setup);
        merchantmenu_text = (TextView) findViewById(R.id.merchant_menu_type);
        merchantmenu_layout = (LinearLayout) findViewById(R.id.merchantmenu_layout);
        simNumberLayout = (LinearLayout) findViewById(R.id.simNumberLayout);
        changePasswordLayout = (LinearLayout) findViewById(R.id.changePasswordLayout);
        select_connection_layout = (LinearLayout) findViewById(R.id.select_connection_layout);
        reconciliation_time = (TextView) findViewById(R.id.reconciliation_time);
        ReconcileSetupModel reconcileSetupModel1 = AppManager.getInstance().getReconcileSetupModel();
        reconciliation_time.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        showTimer = true;
        super.onResume();
    }

    private void setTextView() {
        String tag = getCurrentMenu().getMenu_tag();
        //  Toast.makeText(context, tag, Toast.LENGTH_SHORT).show();
        switch (tag) {
            case DUPLICATE:
                setTitle(getCurrentMenu().getMenu_name());
                merchantmenu_layout.setVisibility(View.VISIBLE);
                merchantmenu_text.setText(getCurrentMenu().getMenu_name());
                printDuplicate();
                break;
            case RECONCILIATION:
                setTitle(getCurrentMenu().getMenu_name());
                merchantmenu_layout.setVisibility(View.VISIBLE);
                makeReconsilation();
                break;
            case SANPSHOT_TOTAL:
                setTitle(getCurrentMenu().getMenu_name());
                merchantmenu_layout.setVisibility(View.VISIBLE);
                merchantmenu_text.setText(getCurrentMenu().getMenu_name());
                makeSnapShot(false);
                break;
            case RUNNING_TOTAL:
                setTitle(getCurrentMenu().getMenu_name());
                merchantmenu_layout.setVisibility(View.VISIBLE);
                merchantmenu_text.setText(getCurrentMenu().getMenu_name());
                makeSnapShot(true);
                break;
            case RECONCILE_SETUP:
                setTitle(getCurrentMenu().getMenu_name());
                reconciliation_setup_layout.setVisibility(View.VISIBLE);
                initReconcileView();
                break;
            case HISTORY_VIEW:
                setTitle(getCurrentMenu().getMenu_name());
                break;
            case LAST_EMV:
                setTitle(getCurrentMenu().getMenu_name());
                break;
            case CHANGE_PASSWORD:
                setTitle(getCurrentMenu().getMenu_name());
                changePasswordLayout.setVisibility(View.VISIBLE);
                setUpPasswordView();
                break;
            case CHANGE_PASSWORD_ADMIN:
                setTitle(getCurrentMenu().getMenu_name());
                changePasswordLayout.setVisibility(View.VISIBLE);
                setUpPasswordView();
                break;
            case TERMINFO_MENU:
                setTitle(getCurrentMenu().getMenu_name());
                break;
            case SIM_NUMBER:
                setTitle(getCurrentMenu().getMenu_name());
                simNumberLayout.setVisibility(View.VISIBLE);
                storeSimNumberLocally();
                break;
            case DE_SAF_ALL_FILE:
                setTitle(getCurrentMenu().getMenu_name());
                deSafFile();
                break;
            case SWITCH_CONNECTION:
                setTitle(getCurrentMenu().getMenu_name());
                select_connection_layout.setVisibility(View.VISIBLE);
                break;
            case SET_GPS_LOCATION:
                setTitle(getCurrentMenu().getMenu_name());
                break;
            case SELECT_LANGUAGE:
                setTitle(getCurrentMenu().getMenu_name());
                break;
        }
    }

    private void storeSimNumberLocally() {
        sim_number_et = findViewById(R.id.sim_number_et);
        submit_simNumber = findViewById(R.id.submit_simNumber);
        if (AppManager.getInstance().getString("sim_number")!=null){
            sim_number_et.setText(AppManager.getInstance().getString("sim_number"));
        }
        submit_simNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sim_number_et.getText().toString().trim()!= null && !sim_number_et.getText().toString().trim().equalsIgnoreCase("")) {
                    AppManager.getInstance().setString("sim_number", sim_number_et.getText().toString().trim());
                    Toast.makeText(context, context.getString(R.string.sim_number_changed_successfully), Toast.LENGTH_SHORT).show();
                    finish();
                }else {
                    Toast.makeText(MerchantMenuActivity.this, getString(R.string.plz_enter_valid_sim_number), Toast.LENGTH_SHORT).show();
                }
            }
        });
        //AppManager.getInstance().setString("sim_number", sim_number_et.getText().toString().trim());
    }

    private void setUpPasswordView() {
        final EditText input_current_password = findViewById(R.id.input_current_password);
        final EditText input_new_password = findViewById(R.id.input_new_password);
        final EditText input_confirm_password = findViewById(R.id.input_confirm_password);
        findViewById(R.id.submit_newpassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(input_current_password.getText().toString().trim().length() != 0){
                    if(input_current_password.getText().toString().trim().length() == 6){
                        if(input_new_password.getText().toString().trim().length() != 0) {
                            if (input_new_password.getText().toString().trim().length() == 6) {
                                if (input_new_password.getText().toString().equalsIgnoreCase(input_confirm_password.getText().toString())) {
                                    if (getCurrentMenu().getMenu_tag().equalsIgnoreCase(ConstantApp.CHANGE_PASSWORD_ADMIN)) {
                                        if (input_current_password.getText().toString().equalsIgnoreCase(AppManager.getInstance().getAdminPassword())) {
                                            AppManager.getInstance().setAdminPassword(input_new_password.getText().toString());
                                            Toast.makeText(context, context.getString(R.string.password_changed_successfully), Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(context, getString(R.string.please_enter_valid_current_password), Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        if (input_current_password.getText().toString().equalsIgnoreCase(AppManager.getInstance().getMerchantPassword())) {
                                            AppManager.getInstance().setMerchantPassword(input_new_password.getText().toString());
                                            Toast.makeText(context, context.getString(R.string.password_changed_successfully), Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(context, getString(R.string.please_enter_valid_current_password), Toast.LENGTH_SHORT).show();
                                        }

                                    }

                                } else
                                    Toast.makeText(context, getString(R.string.password_missmatch), Toast.LENGTH_SHORT).show();
                            } else
                                Toast.makeText(context, getString(R.string.please_enter_6_digit_password), Toast.LENGTH_SHORT).show();
                        }else
                            Toast.makeText(context, getString(R.string.please_enter_6_digit_password), Toast.LENGTH_SHORT).show();
                    }else
                        Toast.makeText(context, getString(R.string.invalid_current_password), Toast.LENGTH_SHORT).show();

                }else
                    Toast.makeText(context, getString(R.string.please_enter_current_passwrod), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deSafFile() {
        viewModel.doSAFOnly = true;
        viewModel.isDownload = 1;
        viewModel.createReconsilation(false);
    }

    private void printDuplicate() {
        viewModel.printDuplicate();
    }

    private void makeSnapShot(boolean isRunning) {
        viewModel.printSnapShot(isRunning);
    }

    private void makeReconsilation() {
        viewModel.doSAFOnly = false;
        viewModel.isDownload = 1;
        showTimer = false;
        viewModel.createReconsilation(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case (R.id.reconciliation_time):
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(MerchantMenuActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
                        String date = String.format("%02d:%02d", hourOfDay, minuteOfHour);
                        if(!(reconciliation_time.getText().toString().equalsIgnoreCase(date))){
                            AppManager.getInstance().resetReconsilationDate();
                        }
                        reconciliation_time.setText(date);
                        Logger.v("reconcile_time----"+reconciliation_time.getText().toString().trim());
                        reconcileSetupModel.setReconcileTime(reconciliation_time.getText().toString().trim());
                        AppManager.getInstance().setReconcileSetupModel(reconcileSetupModel);
                    }
                }, hour, minute,true);

                mTimePicker.show();
                break;
        }
    }
    ReconcileSetupModel reconcileSetupModel;
    private void initReconcileView() {
        reconcilation_status = findViewById(R.id.reconcilation_status);
        reconciliation_enable = findViewById(R.id.reconciliation_enable);
        if (AppManager.getInstance().getReconcileSetupModel()!=null){
            reconcileSetupModel = AppManager.getInstance().getReconcileSetupModel();
            reconcilation_status.setText(reconcileSetupModel.getReconcileStatus()?getString(R.string.reconsiled):getString(R.string.not_reconsiled));
            reconciliation_enable.setText(reconcileSetupModel.getReconcileToggle()?getString(R.string.on):getString(R.string.off));
            reconciliation_time.setText(reconcileSetupModel.getReconcileTime());
        }
        else{
            reconcileSetupModel = new ReconcileSetupModel();
            reconcilation_status.setText(getString(R.string.not_configured));
        }
        reconciliation_enable.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //toggle.toggle();
                if ( reconciliation_enable.getText().toString().equalsIgnoreCase("ON")) {
                    reconcileSetupModel.setReconcileToggle(true);
                    reconcileSetupModel.setReconcileTime(reconciliation_time.getText().toString().trim());
                    AppManager.getInstance().setReconcileSetupModel(reconcileSetupModel);
//                    Toast.makeText(context, "on", Toast.LENGTH_SHORT).show();


                    reconciliation_enable.setChecked(true);
                } else if ( reconciliation_enable.getText().toString().equalsIgnoreCase("OFF")) {
                    reconcileSetupModel.setReconcileToggle(false);
                    reconciliation_enable.setChecked(false);
                    reconcileSetupModel.setReconcileTime(reconciliation_time.getText().toString().trim());
                    AppManager.getInstance().setReconcileSetupModel(reconcileSetupModel);
//                    Toast.makeText(context, "off", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PrinterWorker.RECON_PRINTED = false;
        PrinterWorker.RECON_PRINTED_DUB = false;
        Utils.setNullDialoge();
    }
}