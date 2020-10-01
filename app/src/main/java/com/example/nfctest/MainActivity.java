package com.example.nfctest;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import org.ndeftools.Message;
import org.ndeftools.Record;
import org.ndeftools.externaltype.AndroidApplicationRecord;
import org.ndeftools.wellknown.TextRecord;
import org.ndeftools.wellknown.UriRecord;

import java.util.List;

import static android.graphics.Color.rgb;

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
        if (rawMessages != null) {
            for (int i = 0; i < rawMessages.length; i++) {
                try {
                    List<Record> records = new Message((NdefMessage)rawMessages[i]);
                    Log.d(TAG, "Message " + i + " mit " + records.size() + " Records");
                    for(int k = 0; k < records.size(); k++) {
                        Log.d(TAG, " Record #" + k + " ist ein " +
                                records.get(k).getClass().getSimpleName());
                        Record record = records.get(k);
                        // Hier kann auf den erwarteten Recordtyp geprüft und dessen
                        // Inhalt verarbeitet werden
                        if(record instanceof TextRecord) {
                            TextRecord tr = (TextRecord) record;
                            Log.d(TAG, "TextRecord is " + tr.getText());
                            // Werte colorstring in der Form c-12-55-255 aus und
                            // setze die Bildschirmfarbe
                            String string = tr.getText();
                            String[] parts = string.split("-");
                            int color = rgb(Integer.parseInt(parts[1]),
                                    Integer.parseInt(parts[2]),
                                    Integer.parseInt(parts[3]));
                            getWindow().getDecorView().setBackgroundColor(color);
                        } else if(record instanceof UriRecord) {
                            UriRecord ur = (UriRecord)record;
                            Log.d(TAG, "UriRecord is " + ur.getUri());
                            // impliziten Intent zum öffnen des Links
                            Intent implicitIntent = new Intent(Intent.ACTION_VIEW);
                            implicitIntent.setData(Uri.parse(ur.getUri().toString()));
                            startActivity(implicitIntent);
                        }
                        else if(record instanceof AndroidApplicationRecord) {
                            AndroidApplicationRecord aar = (AndroidApplicationRecord)record;
                            Log.d(TAG, "Package is " + aar.getPackageName());
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Problem parsing message", e);
                }
            }
        }
    }
}