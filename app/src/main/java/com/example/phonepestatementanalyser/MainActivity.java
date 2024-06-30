package com.example.phonepestatementanalyser;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_PDF_REQUEST = 1;
    TextView creditsTextView;
    TextView debitsTextView;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        creditsTextView = findViewById(R.id.creditsTextView);
        debitsTextView = findViewById(R.id.debitsTextView);
        button = findViewById(R.id.button);

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, 1);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                startActivityForResult(intent, PICK_PDF_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedPdfUri = data.getData();
            // Process the selected PDF

//            Handler mainHandler = new Handler(Looper.getMainLooper());
//
//// Send a task to the MessageQueue of the main thread
//            mainHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    // Code will be executed on the main thread
//                    processPdf(selectedPdfUri);
//                }
//            });

//            runOnUiThread(new Runnable() {
//                public void run() {
//                    //Do something on UiThread
//                    processPdf(selectedPdfUri);
//                }
//            });

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(new Runnable() {
                @Override
                public void run() {

                    //Background work here
                    ArrayList<Double> numbers = processPdf(selectedPdfUri);

                    handler.post(new Runnable() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void run() {
                            //UI Thread work here
                            creditsTextView.setText("Total Credits: ₹ " + numbers.get(0));
                            debitsTextView.setText("Total Debits: ₹ " + numbers.get(1));
                        }
                    });
                }
            });

        }
    }

    private ArrayList<Double> processPdf(Uri pdfUri) {
        ArrayList<Double> numbers = new ArrayList<>();
        try {
            InputStream inputStream = getContentResolver().openInputStream(pdfUri);
            PdfReader pdfReader = new PdfReader(inputStream);
            PdfDocument pdfDocument = new PdfDocument(pdfReader);

            StringBuilder text = new StringBuilder();
            for (int i = 1; i <= pdfDocument.getNumberOfPages(); i++) {
                text.append(PdfTextExtractor.getTextFromPage(pdfDocument.getPage(i)));
            }

            return extractTransactions(text.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return numbers;
    }

    private ArrayList<Double> extractTransactions(String text) {
//        Pattern pattern = Pattern.compile("DEBIT ₹"+"(\\d+(\\.\\d{1,2})?)");
        Pattern pattern = Pattern.compile("DEBIT\\s+₹\\s?(\\d+(?:,\\d{3})*(?:\\.\\d{1,2})?)");
        Pattern pattern1 = Pattern.compile("CREDIT\\s+₹\\s?(\\d+(?:,\\d{3})*(?:\\.\\d{1,2})?)");


        Matcher matcher = pattern.matcher(text);
        Matcher matcher1 = pattern1.matcher(text);

        Log.e("vikash***",text);
        double dsum = 0.0;
        double csum = 0.0;

        while (matcher.find()) {
            String amountStr = matcher.group(1).replace(",", "");
            dsum += Double.parseDouble(amountStr);
        }

        while (matcher1.find()) {
            String amountStr = matcher1.group(1).replace(",", "");
            csum += Double.parseDouble(amountStr);
        }

//        return sum;

//        displayResults(csum, dsum);
        ArrayList<Double> numbers = new ArrayList<>();
        numbers.add(csum);
        numbers.add(dsum);
        return numbers;
    }

    @SuppressLint("SetTextI18n")
    private void displayResults(double credits, double debits) {
        creditsTextView.setText("Total Credits: " + credits);
        debitsTextView.setText("Total Debits: " + debits);
//        Toast.makeText(this,  "Debit " + String.valueOf(debits),
//                Toast.LENGTH_LONG).show();
    }
}
