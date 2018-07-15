package com.luc.ankireview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.database.Cursor;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.support.v4.content.ContextCompat;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import com.ichi2.anki.FlashCardsContract;



public class DeckPickerActivity extends AppCompatActivity {

    private static final String TAG = "DeckPickerActivity";


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // permission was granted, yay! Do the
            // contacts-related task you need to do.

            listDecks();

        } else {
            // permission denied, boo! Disable the
            // functionality that depends on this permission.
        }
        return;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck_picker);

        Log.d(TAG, "hello world");


        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                "com.ichi2.anki.permission.READ_WRITE_DATABASE")
                != PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "permission not granted yet");

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    "com.ichi2.anki.permission.READ_WRITE_DATABASE")) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                Log.d(TAG, "show explanation ?");

                ActivityCompat.requestPermissions(this,
                        new String[]{"com.ichi2.anki.permission.READ_WRITE_DATABASE"},
                        0);

            } else {
                Log.d(TAG, "request permission");

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{"com.ichi2.anki.permission.READ_WRITE_DATABASE"},
                        0);

                // we're going to get a callback later
            }
        } else {
            // Permission has already been granted
            Log.d(TAG, "permission already granted");

            listDecks();
        }

    }

    private void listDecks() {

        Cursor decksCursor = getContentResolver().query(FlashCardsContract.Deck.CONTENT_ALL_URI, null, null, null, null);
        if (decksCursor.moveToFirst()) {
            HashMap decks = new HashMap();
            do {
                long deckID = decksCursor.getLong(decksCursor.getColumnIndex(FlashCardsContract.Deck.DECK_ID));
                String deckName = decksCursor.getString(decksCursor.getColumnIndex(FlashCardsContract.Deck.DECK_NAME));

                Log.d(TAG, "deck name: " + deckName);

                try {
                    JSONObject deckOptions = new JSONObject(decksCursor.getString(decksCursor.getColumnIndex(FlashCardsContract.Deck.OPTIONS)));
                    JSONArray deckCounts = new JSONArray(decksCursor.getString(decksCursor.getColumnIndex(FlashCardsContract.Deck.DECK_COUNTS)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                decks.put(deckID, deckName);
            } while (decksCursor.moveToNext());
        }
    }

}