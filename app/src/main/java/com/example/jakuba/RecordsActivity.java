package com.example.jakuba;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.share.Share;
import com.facebook.share.model.ShareContent;

import java.util.ArrayList;
import java.util.List;

public class RecordsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    private static final int PERMISSION_REQUEST_CODE = 1;
    public static final String SPIND_POS = "spinD";
    public static final String SPINT_POS = "spinT";
    public static final String PHONE_NUM = "number";

    DbHelper dbHelper;
    Spinner spinData, spinTitle;
    Button btnShare;
    TextView txtTitle, txtArtist, txtSummary;
    EditText editPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);
        dbHelper = new DbHelper(this);

        txtTitle = findViewById(R.id.txtLoadedTitle);
        txtArtist = findViewById(R.id.txtLoadedArtist);
        txtSummary = findViewById(R.id.txtLoadedSummary);
        spinData = findViewById(R.id.spinnerDate);
        spinTitle = findViewById(R.id.spinnerTitle);
        editPhone = findViewById(R.id.editPhone);
        btnShare = findViewById(R.id.btnShare);

        ArrayAdapter<String> adapterDate = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, loader(1, null));
        adapterDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinData.setAdapter(adapterDate);

        ArrayAdapter<String> adapterTitle = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, loader(2, null));
        adapterTitle.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinTitle.setAdapter(adapterTitle);

        spinData.setOnItemSelectedListener(this);
        spinTitle.setOnItemSelectedListener(this);
        btnShare.setOnClickListener(this);

        if (savedInstanceState != null) {
            spinData.setSelection(savedInstanceState.getInt(SPIND_POS));
            spinTitle.setSelection(savedInstanceState.getInt(SPINT_POS));
            editPhone.setText(savedInstanceState.getString(PHONE_NUM));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnShare:
                StringBuilder sb = new StringBuilder();
                if (!txtTitle.getText().equals("")) {
                    sb.append(txtTitle.getText().toString() + "\n\n");
                }
                if (!txtArtist.getText().equals("")) {
                    sb.append(txtArtist.getText().toString() + "\n\n");
                }
                if (!txtSummary.getText().equals("")) {
                    sb.append(txtSummary.getText().toString());
                }

                if (Build.VERSION.SDK_INT >= 23) {
                    if (checkPermission()) {
                        Log.e("permission", "Permission already granted.");
                    } else {
                        requestPermission();
                    }
                }

                if(checkPermission()) {
                    SmsManager smsManager = SmsManager.getDefault();
                    ArrayList<String> parts = smsManager.divideMessage(sb.toString());
                    smsManager.sendMultipartTextMessage(editPhone.getText().toString(), null, parts, null, null);
                    Toast.makeText(getApplicationContext(), "Message was sent", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Message was not sent", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.spinnerDate:
                String date = parent.getItemAtPosition(position).toString();
                ArrayAdapter<String> adapterTitle = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, loader(3, date));
                adapterTitle.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinTitle.setAdapter(adapterTitle);
                break;
            case R.id.spinnerTitle:
                Cursor cursor = dbHelper.retrieveByTitle(parent.getItemAtPosition(position).toString());
                while (cursor.moveToNext()) {
                    if (cursor.getCount() == 0) {
                        Toast.makeText(getApplicationContext(), "No data in database", Toast.LENGTH_LONG).show();
                        break;
                    }
                    txtTitle.setText(cursor.getString(0));
                    txtArtist.setText(cursor.getString(1));
                    txtSummary.setText(cursor.getString(2));
                }
                break;
            default:
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SPIND_POS, spinData.getSelectedItemPosition());
        outState.putInt(SPINT_POS, spinTitle.getSelectedItemPosition());
        outState.putString(PHONE_NUM, editPhone.getText().toString());
        super.onSaveInstanceState(outState);
    }

    public List<String> loader(int p, String date) {
        List<String> array = new ArrayList<>();
        Cursor cursor;
        if (p == 1) {
            cursor = dbHelper.retrieveDate();
        } else if (p == 2) {
            cursor = dbHelper.retrieveTitle();
        } else {
            cursor = dbHelper.retrieveByDate(date);
        }
        while (cursor.moveToNext()) {
            if (cursor.getCount() == 0) {
                Toast.makeText(getApplicationContext(), "No data in database", Toast.LENGTH_LONG).show();
                break;
            }
            array.add(cursor.getString(0));
        }
        return array;
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(RecordsActivity.this, Manifest.permission.SEND_SMS);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_REQUEST_CODE);
    }

}
