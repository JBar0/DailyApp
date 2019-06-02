package com.example.jakuba;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class RecordsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    DbHelper dbHelper;
    Spinner spinData, spinTitle;
    Button btnShare;
    TextView txtTitle, txtArtist, txtSummary;

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

        ArrayAdapter<String> adapterDate = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, loader(1, null));
        adapterDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinData.setAdapter(adapterDate);

        ArrayAdapter<String> adapterTitle = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, loader(2, null));
        adapterTitle.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinTitle.setAdapter(adapterTitle);

        spinData.setOnItemSelectedListener(this);
        spinTitle.setOnItemSelectedListener(this);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.spinnerDate:
                String date = parent.getItemAtPosition(position).toString();
                Toast.makeText(parent.getContext(), "Data", Toast.LENGTH_LONG).show();
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

}
