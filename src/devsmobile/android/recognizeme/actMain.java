package devsmobile.android.recognizeme;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.BaseColumns;
import android.provider.Contacts.People;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class actMain extends Activity implements OnClickListener {

    private int Code = 1453;

    Button btnRecognizeMe;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        try {
            rcvrMediaButton _receiver = new rcvrMediaButton();

            registerReceiver(_receiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
            registerReceiver(_receiver, new IntentFilter(Intent.ACTION_MEDIA_BUTTON));

        } catch (Exception e) {
            // TODO: handle exception
        }

        Bundle b = getIntent().getExtras();
        Boolean fromReceiver = false;
        if (b != null) {
            fromReceiver = b.getBoolean("fromReceiver");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        btnRecognizeMe = (Button) findViewById(R.id.btnRecognizeMe);

        PackageManager pm = getPackageManager();
        List activities = pm.queryIntentActivities(new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() != 0) {
            btnRecognizeMe.setOnClickListener(this);
        } else {
            btnRecognizeMe.setEnabled(false);
            btnRecognizeMe.setText("Ses tanıma bulunamadı.");
        }


        OpenRecognizer();
    }

    private void PlaySound(String keyword, OnCompletionListener compleateListener) {
        try {
            FileHelper fh = new FileHelper(this);

            String url = "http://translate.google.com.tr/translate_tts?ie=UTF-8&tl=tr&q=" + URLEncoder.encode(keyword, "UTF-8");

            MediaPlayer mp = MediaPlayer
                    .create(getBaseContext(),
                            Uri.fromFile(fh
                                    .DownloadFile(url)));
            mp.start();
            mp.setOnCompletionListener(new OnCompletionListener() {

                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });

            if (compleateListener != null) {
                mp.setOnCompletionListener(compleateListener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClick(View element) {
        OpenRecognizer();
    }

    private void OpenRecognizer() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR");
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Aramak istediğiniz kişinin ismini söyleyin.");

        startActivityForResult(i, Code);

        //PlaySound(R.raw.talk);

        // Get instance of Vibrator from current Context
        Vibrator v = (Vibrator) getSystemService(getApplicationContext().VIBRATOR_SERVICE);

        // Vibrate for 300 milliseconds
        v.vibrate(100);
    }

    public List<RMContact> getContact(String displayName) {
        List<RMContact> foundedContacts = new ArrayList<RMContact>();
        Uri lkup = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, displayName);
        ContentResolver cr = getContentResolver();
        Cursor idCursor = cr.query(lkup, null, null, null, null);
        while (idCursor.moveToNext()) {

            RMContact contact = new RMContact();

            contact.setContactId(idCursor.getLong(idCursor.getColumnIndex(ContactsContract.Contacts._ID)));
            contact.setKey(idCursor.getString(idCursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)));
            contact.setDisplayName(idCursor.getString(idCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));

            if (Integer.parseInt(idCursor.getString(
                    idCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                Cursor pCur = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{String.valueOf(contact.getContactId())}, null);

                while (pCur.moveToNext()) {
                    contact.getPhoneNumbers().add(pCur.getString(pCur.getColumnIndex(Phone.NUMBER)));
                }
                pCur.close();
            }

            foundedContacts.add(contact);
        }
        idCursor.close();

        return foundedContacts;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Code) {

            if (resultCode == RESULT_OK) {
                List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                RMContact contact = null;
                for (String foundedText : results) {
                    List<RMContact> foundedContacts = getContact(foundedText);

                    for (RMContact foundedContact : foundedContacts) {
                        if (foundedContact.getPhoneNumbers().size() > 0) {
                            contact = foundedContact;
                            break;
                        }
                    }
                }

                if (contact == null) {
                    PlaySound("Kayıt bulunamadı!", null);
                } else {
                    final RMContact finalContact = contact;
                    PlaySound(contact.getDisplayName() + " aranıyor.", new OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            Intent callIntent = new Intent(Intent.ACTION_CALL);
                            callIntent.setData(Uri.parse("tel:" + finalContact.getPhoneNumbers().get(0)));
                            startActivity(callIntent);
                        }
                    });
                }
            }
            finish();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}