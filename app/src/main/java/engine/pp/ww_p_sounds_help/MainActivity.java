package engine.pp.ww_p_sounds_help;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    public static final int RECORD_AUDIO_PERMISSION_CODE = 1;

    private Button translateButton;
    private Button startTranscriptionButton;
    private Button processTextButton; // Nowy przycisk
    private TextView transcribedTextView;
    private Spinner sentencesSpinner; // Spinner

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;

    private String selectedSentence; // Do przechowywania wybranego zdania

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicjalizacja widoków
        translateButton = findViewById(R.id.btn_open_activity);
        startTranscriptionButton = findViewById(R.id.btn_start_transcription);
        processTextButton = findViewById(R.id.btn_process_text); // Inicjalizacja nowego przycisku
        transcribedTextView = findViewById(R.id.transcribed_text_view);
        sentencesSpinner = findViewById(R.id.sentencesSpinner); // Inicjalizacja Spinnera

        // Ustawienie listenerów
        translateButton.setOnClickListener(v -> openSecondActivity());
        startTranscriptionButton.setOnClickListener(v -> checkAndStartListening());
        processTextButton.setOnClickListener(v -> updateSpinnerWithSentences()); // Ustawienie akcji dla nowego przycisku

        // Nasłuchiwanie na wybór elementu w Spinnerze
        sentencesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Zapisz wybrane zdanie do zmiennej
                selectedSentence = (String) parent.getItemAtPosition(position);
                Toast.makeText(MainActivity.this, "Wybrano: " + selectedSentence, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedSentence = null;
            }
        });

        initializeSpeechRecognizer();
    }

    /**
     * Metoda, która pobiera tekst z TextView, dzieli go na zdania i wypełnia Spinner.
     */
    private void updateSpinnerWithSentences() {
        String fullText = transcribedTextView.getText().toString();

        if (fullText.trim().isEmpty()) {
            Toast.makeText(this, "Brak tekstu do przetworzenia.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Dzieli tekst na zdania. Regex szuka kropek, znaków zapytania i wykrzykników.
        String[] sentencesArray = fullText.split("(?<=[.?!])\\s*");
        List<String> sentencesList = new ArrayList<>(Arrays.asList(sentencesArray));

        // Tworzenie adaptera dla Spinnera
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, sentencesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Ustawienie adaptera w Spinnerze
        sentencesSpinner.setAdapter(adapter);
    }

    // Metoda do otwierania SecondActivity (możesz ją zmodyfikować, by przekazywać wybrane zdanie)
    public void openSecondActivity() {
        Intent intent = new Intent(this, SecondActivity.class);
        // Przykład przekazania wybranego zdania do kolejnej aktywności
        if (selectedSentence != null && !selectedSentence.isEmpty()) {
            intent.putExtra("SENTENCE_TO_TRANSLATE", selectedSentence);
        }
        startActivity(intent);
    }

    // Pozostała część kodu pozostaje bez zmian (transkrypcja mowy)
    // ...
    private void checkAndStartListening() {
        if (checkAudioPermission()) {
            startListening();
        } else {
            requestAudioPermission();
        }
    }

    private void initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    transcribedTextView.setHint("Słucham...");
                }

                @Override
                public void onBeginningOfSpeech() {}

                @Override
                public void onRmsChanged(float rmsdB) {}

                @Override
                public void onBufferReceived(byte[] buffer) {}

                @Override
                public void onEndOfSpeech() {
                    transcribedTextView.setHint("Przetwarzanie...");
                }

                @Override
                public void onError(int error) {
                    String errorMessage = getErrorText(error);
                    transcribedTextView.setText("Błąd: " + errorMessage);
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        transcribedTextView.setText(matches.get(0));
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        transcribedTextView.setText(matches.get(0));
                    }
                }

                @Override
                public void onEvent(int eventType, Bundle params) {}
            });

            speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        } else {
            Toast.makeText(this, "Rozpoznawanie mowy nie jest dostępne.", Toast.LENGTH_LONG).show();
            startTranscriptionButton.setEnabled(false);
        }
    }

    private void startListening() {
        if (speechRecognizer != null && speechRecognizerIntent != null) {
            transcribedTextView.setText("");
            transcribedTextView.setHint("Słucham...");
            speechRecognizer.startListening(speechRecognizerIntent);
        }
    }

    private boolean checkAudioPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Uprawnienia przyznane!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Odmówiono uprawnień do mikrofonu.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    private String getErrorText(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO: return "Błąd nagrywania";
            case SpeechRecognizer.ERROR_CLIENT: return "Błąd po stronie klienta";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: return "Brak uprawnień";
            case SpeechRecognizer.ERROR_NETWORK: return "Błąd sieci";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: return "Przekroczono czas oczekiwania na sieć";
            case SpeechRecognizer.ERROR_NO_MATCH: return "Nie rozpoznano mowy";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: return "Usługa jest zajęta";
            case SpeechRecognizer.ERROR_SERVER: return "Błąd serwera";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: return "Nie wykryto mowy";
            default: return "Nieznany błąd";
        }
    }
}