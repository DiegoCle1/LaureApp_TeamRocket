package it.uniba.dib.sms222312.studenti;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.HashMap;
import java.util.Map;

import it.uniba.dib.sms222312.R;
import it.uniba.dib.sms222312.docenti.VisualizzaTesiActivity;
import it.uniba.dib.sms222312.modelli.Classifica;

public class VisualizzaTesiStudenteActivity extends AppCompatActivity {

    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizza_tesi2);

        String nome = getIntent().getStringExtra("Nome");
        String corso = getIntent().getStringExtra("Corso");
        String descrizione = getIntent().getStringExtra("Descrizione");
        String media = getIntent().getStringExtra("Media");
        String durata = getIntent().getStringExtra("Durata");
        String docente = getIntent().getStringExtra("Docente");

        TextView textNome = findViewById(R.id.nome);
        TextView textCorso = findViewById(R.id.corso);
        TextView textDescrizione = findViewById(R.id.descrizione);
        TextView textMedia = findViewById(R.id.media);
        TextView textDurata = findViewById(R.id.durata);
        TextView textDocente = findViewById(R.id.docente);
        ImageView imageQr = findViewById(R.id.qr);
        Button classificaButton = findViewById(R.id.classifica);
        Button richiediButton = findViewById(R.id.richiesta);
        Button condividiButton = findViewById(R.id.condividi);
        Button contattaButton = findViewById(R.id.contatta);

        String text ="Nome: " + nome + " Corso: " + corso + " Media: " + media + " Durata: " + durata + " ore Descrizione: " + descrizione;
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix matrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 400, 400);
            BarcodeEncoder encoder = new BarcodeEncoder();
            bitmap = encoder.createBitmap(matrix);
            imageQr.setImageBitmap(bitmap);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }

        textNome.setText(nome);
        textCorso.setText(corso);
        textDescrizione.setText(descrizione);
        textMedia.setText(media);
        textDurata.setText(durata);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("utente").document(docente).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String docenta = documentSnapshot.getString("nome") + " " + documentSnapshot.getString("cognome");
                textDocente.setText(docenta);
            }
        });
        classificaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                FirebaseAuth auth = FirebaseAuth.getInstance();
                String user = auth.getCurrentUser().getUid();
                Classifica classifica = new Classifica(user, nome, durata, media);
                Map<String, Object> userDb = new HashMap<>();
                userDb.put("utente", classifica.getUtente());
                userDb.put("nome", classifica.getNome());
                userDb.put("durata", classifica.getDurata());
                userDb.put("media", classifica.getMedia());
                database.collection("classifica").document().set(userDb).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(VisualizzaTesiStudenteActivity.this,"Aggiunto alla classifica", Toast.LENGTH_SHORT).show();
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(VisualizzaTesiStudenteActivity.this,"Impossibile aggiungere alla classifica", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        richiediButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                String usera = auth.getCurrentUser().getUid();
                Query query = database.collection("tesi").whereEqualTo("corso", corso).whereEqualTo("nome", nome).whereEqualTo("descrizione", descrizione).whereEqualTo("media",media).whereEqualTo("ore",durata).whereEqualTo("docente",docente);
                query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            // Ottieni il primo documento corrispondente
                            DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                            // Ottieni l'ID del documento
                            String documentId = documentSnapshot.getId();
                            Intent intent = new Intent(VisualizzaTesiStudenteActivity.this, InvioRichiestaActivity.class);

                            intent.putExtra("Studente", usera);
                            intent.putExtra("Docente", docente);
                            intent.putExtra("Tesi", documentId);

                            startActivity(intent);
                        } else {
                            // Nessun documento corrispondente trovato
                        }
                    }
                });
            }
        });

        condividiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(VisualizzaTesiStudenteActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(VisualizzaTesiStudenteActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }else{
                    // Codice per condividere un Bitmap su WhatsApp
                    String bitmapPath = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Shared image", null);

                    Uri bitmapUri = Uri.parse(bitmapPath);

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("image/*");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
                    //shareIntent.setPackage("com.whatsapp");

                    startActivity(Intent.createChooser(shareIntent, "Condividi immagine"));
                }

            }
        });

        contattaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                database.collection("utente").document(docente).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String docentemail = documentSnapshot.getString("email");

                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:"));
                        Log.d("email", docentemail);
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{docentemail});
                        intent.putExtra(Intent.EXTRA_SUBJECT, nome);

                        if(intent.resolveActivity(getPackageManager()) != null){
                            startActivity(intent);
                        }else{
                            Toast.makeText(VisualizzaTesiStudenteActivity.this, "Nessuna app email installata", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
    }
}