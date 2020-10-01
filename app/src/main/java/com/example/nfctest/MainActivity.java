package com.example.nfctest;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private NfcAdapter nfcAdapter;
    private String TAG;

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

        // intent auswerten, wenn Aktivität über intent gestartet wurde
        Intent intent = getIntent();
        Log.d(TAG, "onCreate: " + intent.getAction());
        handleNfcNdefIntent(intent);
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

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent: " + intent.getAction());
        handleNfcNdefIntent(intent);
        super.onNewIntent(intent);
    }

    private void handleNfcNdefIntent(Intent intent) {
        // Abbruch, wenn der Intent kein NFC-NDEF Intent ist
        if (!intent.hasExtra(NfcAdapter.EXTRA_TAG)) return;
        // Ansonsten Intent verarbeiten
        Toast.makeText(this, getString(R.string.nfc_intent_received) +
                ": " + intent.getAction(), Toast.LENGTH_LONG).show();

        Parcelable[] rawMessages =
                intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMessages != null && rawMessages.length > 0) {
            NdefMessage[] messages = new NdefMessage[rawMessages.length];
            Log.d(TAG, "handleNfcNdefIntent: message size = " + rawMessages.length);
            for (int i = 0; i < rawMessages.length; i++) {
                messages[i] = (NdefMessage) rawMessages[i];
                for(NdefRecord record : messages[i].getRecords()) {
                    String payloadStringData = new String(record.getPayload());
                    Log.d(TAG, "handleNfcNdefIntent: " + payloadStringData);
                }
            }
        }
    }
}