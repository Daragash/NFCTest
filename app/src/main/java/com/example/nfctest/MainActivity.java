package com.example.nfctest;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            // NFC auf dem Gerät nicht verfügbar
            Toast.makeText(this, getString(R.string.nfc_not_avail),
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // prüfen, ob NFC aktiviert ist
        if (!nfcAdapter.isEnabled()) {
            AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
            alertbox.setTitle(getString(R.string.nfc_alert_title));
            alertbox.setMessage(getString(R.string.nfc_not_activated));
            alertbox.setPositiveButton(getString(R.string.nfc_alert_yes),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                            startActivity(intent);
                        }
                    });
            alertbox.setNegativeButton(getString(R.string.nfc_alert_no),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });

                alertbox.show();
        }
    }
}