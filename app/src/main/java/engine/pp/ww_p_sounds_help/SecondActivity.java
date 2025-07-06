package engine.pp.ww_p_sounds_help;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecondActivity extends AppCompatActivity {

    private Spinner wordsSpinner; // ZMIANA: Zmiana nazwy dla jasności
    private ImageView imageView;
    private Button prevButton;
    private Button nextButton;

    private List<String> wordsInSentence; // Lista słów/liter do wyświetlenia w Spinnerze
    private Map<String, String> wordToImageUrlMap; // Mapa: słowo -> URL obrazka

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        // Inicjalizacja widoków
        wordsSpinner = findViewById(R.id.categorySpinner); // Używamy tego samego ID z XML
        imageView = findViewById(R.id.imageView);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);

        // Inicjalizacja list
        wordsInSentence = new ArrayList<>();
        wordToImageUrlMap = new HashMap<>();

        // ODBIERANIE DANYCH Z MAINACTIVITY
        String sentenceToTranslate = getIntent().getStringExtra("SENTENCE_TO_TRANSLATE");

        if (sentenceToTranslate != null && !sentenceToTranslate.isEmpty()) {
            processSentence(sentenceToTranslate);
        } else {
            Toast.makeText(this, "Nie otrzymano zdania do przetłumaczenia.", Toast.LENGTH_LONG).show();
            // Można tu np. wyłączyć przyciski lub pokazać informację
        }

        // Ustawienie nasłuchiwania na wybór w Spinnerze
        wordsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedWord = wordsInSentence.get(position);
                updateImageForWord(selectedWord);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                imageView.setImageDrawable(null); // Wyczyść obraz, jeśli nic nie jest wybrane
            }
        });

        // Ustawienie akcji dla przycisków
        prevButton.setOnClickListener(v -> showPreviousImage());
        nextButton.setOnClickListener(v -> showNextImage());
    }

    /**
     * NOWA METODA: Przetwarza otrzymane zdanie.
     * Dzieli je na słowa/litery i przygotowuje dane dla Spinnera i mapy obrazków.
     */
    private void processSentence(String sentence) {
        // Krok 1: Wyczyść stare dane
        wordsInSentence.clear();
        wordToImageUrlMap.clear();

        // Krok 2: Podziel zdanie. Zmień logikę w zależności od potrzeb
        // Przykład 1: Podział na słowa
        // String[] words = sentence.toLowerCase().replaceAll("[.?!,]", "").split("\\s+");

        // Przykład 2: Podział na litery (dla alfabetu migowego)
        String cleanedSentence = sentence.toUpperCase().replaceAll("[^A-Z]", ""); // Usuń wszystko oprócz liter
        char[] letters = cleanedSentence.toCharArray();

        // Krok 3: Przygotuj mapę URL-i i listę do Spinnera
        String baseUrl = "https://raw.githubusercontent.com/PiotrIT2015/WW-P-sounds-help/main/img/";

        for (char letter : letters) {
            String letterStr = String.valueOf(letter);
            wordsInSentence.add(letterStr); // Dodajemy literę do listy dla Spinnera

            // Zakładamy, że obrazki nazywają się A.jpg, B.jpg itd.
            String imageUrl = baseUrl + letterStr + ".jpg";
            wordToImageUrlMap.put(letterStr, imageUrl);
        }

        // Krok 4: Zaktualizuj Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, wordsInSentence);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wordsSpinner.setAdapter(adapter);
    }

    /**
     * NOWA METODA: Aktualizuje obrazek na podstawie wybranego słowa/litery.
     */
    private void updateImageForWord(String word) {
        String imageUrl = wordToImageUrlMap.get(word.toUpperCase()); // Pobierz URL z mapy
        if (imageUrl != null) {
            Glide.with(this)
                    .load(imageUrl)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Toast.makeText(SecondActivity.this, "Nie udało się załadować obrazka dla: " + word, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(imageView);
        } else {
            Toast.makeText(this, "Brak obrazka dla: " + word, Toast.LENGTH_SHORT).show();
            imageView.setImageResource(android.R.drawable.ic_menu_gallery); // Przykładowy placeholder
        }
    }

    private void showPreviousImage() {
        int currentIndex = wordsSpinner.getSelectedItemPosition();
        if (currentIndex > 0) {
            wordsSpinner.setSelection(currentIndex - 1);
        } else {
            wordsSpinner.setSelection(wordsSpinner.getCount() - 1); // Zapętl do końca
        }
    }

    private void showNextImage() {
        int currentIndex = wordsSpinner.getSelectedItemPosition();
        if (currentIndex < wordsSpinner.getCount() - 1) {
            wordsSpinner.setSelection(currentIndex + 1);
        } else {
            wordsSpinner.setSelection(0); // Zapętl do początku
        }
    }
}