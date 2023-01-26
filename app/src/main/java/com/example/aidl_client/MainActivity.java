package com.example.aidl_client;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import SeparatePackage.*;

public class MainActivity extends Activity implements View.OnClickListener {

    private TextView textViewDisplayResult;
    private EditText editTextFirstValue, editTextSecondValue;
    private Button buttonAdd, buttonSubtract, buttonMultiply, buttonDivide, buttonClearData;
    private aidlInterface aidlObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the UI
        textViewDisplayResult = findViewById(R.id.display_result);
        editTextFirstValue = findViewById(R.id.enter_first_value);
        editTextSecondValue = findViewById(R.id.enter_second_value);
        buttonAdd = findViewById(R.id.addition);
        buttonSubtract = findViewById(R.id.subtraction);
        buttonMultiply = findViewById(R.id.multiplication);
        buttonDivide = findViewById(R.id.division);
        buttonClearData = findViewById(R.id.clear_data);

        // Setting onClick Listeners
        buttonAdd.setOnClickListener(this);
        buttonSubtract.setOnClickListener(this);
        buttonMultiply.setOnClickListener(this);
        buttonDivide.setOnClickListener(this);
        buttonClearData.setOnClickListener(this);

        bindToAIDLService();
    }

    private void bindToAIDLService() {
        Intent aidlServiceIntent = new Intent("connect_to_aidl_service");
        bindService(implicitIntentToExplicitIntent(aidlServiceIntent, this), serviceConnectionObject, BIND_AUTO_CREATE);
    }

    ServiceConnection serviceConnectionObject = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            aidlObject = aidlInterface.Stub.asInterface(iBinder);
//            Toast.makeText(getApplicationContext(),"service has been connected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    public Intent implicitIntentToExplicitIntent(Intent implicitIntent, Context context) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentServices(implicitIntent, 0);
        if(resolveInfoList == null || resolveInfoList.size() != 1) {
            return null;
        }
        ResolveInfo serviceInfo = resolveInfoList.get(0);
        ComponentName component = new ComponentName(serviceInfo.serviceInfo.packageName, serviceInfo.serviceInfo.name);
        Intent explicitIntent = new Intent(implicitIntent);
        explicitIntent.setComponent(component);
        return explicitIntent;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.addition:
                verifyAndCalculate(1);
                break;
            case R.id.subtraction:
                verifyAndCalculate(2);
                break;
            case R.id.multiplication:
                verifyAndCalculate(3);
                break;
            case R.id.division:
                verifyAndCalculate(4);
                break;
            case R.id.clear_data:
                textViewDisplayResult.setText(null);
                editTextFirstValue.setText(null);
                editTextSecondValue.setText(null);
                break;
            default:
                Log.i("Error", "Default Case");
        }
    }

    private void verifyAndCalculate(int operationType) {
        if(isAnyValueMissing()) {
            Toast.makeText(this, "Please enter both the values", Toast.LENGTH_SHORT).show();
        }
        else if(Integer.parseInt(editTextSecondValue.getText().toString()) == 0) {
            Toast.makeText(this, "Please enter a non-zero second number", Toast.LENGTH_SHORT).show();
        }
        else {
            int result, firstValue, secondValue;
            firstValue = Integer.parseInt(editTextFirstValue.getText().toString());
            secondValue = Integer.parseInt(editTextSecondValue.getText().toString());
            try {
                result = aidlObject.calculateData(firstValue, secondValue, operationType);
                textViewDisplayResult.setText(Integer.toString(result));
            }
            catch (RemoteException e) {
                e.printStackTrace();
            }
            // result = performCalculation(firstValue, secondValue, operationType);
            // textViewDisplayResult.setText(Integer.toString(result));
        }
    }

    private int performCalculation(int firstValue, int secondValue, int operationType) {
        switch(operationType) {
            case 1:
                return firstValue + secondValue;
            case 2:
                return firstValue - secondValue;
            case 3:
                return firstValue * secondValue;
            case 4:
                return firstValue / secondValue;
            default:
                Log.d("Error", "Invalid Operation");
                return 0;
        }
    }

    private boolean isAnyValueMissing() {
        return editTextFirstValue.getText().toString().isEmpty() || editTextSecondValue.getText().toString().isEmpty();
    }
}