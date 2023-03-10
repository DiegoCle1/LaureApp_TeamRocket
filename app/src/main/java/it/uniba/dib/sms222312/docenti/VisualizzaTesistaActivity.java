package it.uniba.dib.sms222312.docenti;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import it.uniba.dib.sms222312.R;
import it.uniba.dib.sms222312.modelli.ListaRichiesteInterface;
import it.uniba.dib.sms222312.modelli.ListaTaskAdapter;
import it.uniba.dib.sms222312.modelli.Task;

public class VisualizzaTesistaActivity extends AppCompatActivity implements ListaRichiesteInterface {

    RecyclerView recyclerView;
    ArrayList<Task> taskArrayList;
    ListaTaskAdapter myAdapter;
    FirebaseFirestore db;
    String tesista;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizza_tesista);

        tesista = getIntent().getStringExtra("Tesista");

        recyclerView = findViewById(R.id.listaTask);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        taskArrayList = new ArrayList<Task>();
        myAdapter = new ListaTaskAdapter(VisualizzaTesistaActivity.this, taskArrayList, this);
        recyclerView.setAdapter(myAdapter);

        EventChangeListener();

        Button btnTask = findViewById(R.id.btnTask);

        btnTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VisualizzaTesistaActivity.this, AggiungiTaskActivity.class);

                intent.putExtra("Tesista", tesista);

                startActivity(intent);
            }
        });
    }

    private void EventChangeListener() {

        db.collection("task").whereEqualTo("tesista",tesista).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error != null){
                    Log.e("Firestore error", error.getMessage());
                    return;
                }
                for(DocumentChange dc : value.getDocumentChanges()){
                    if(dc.getType() == DocumentChange.Type.ADDED){
                        taskArrayList.add(dc.getDocument().toObject(Task.class));
                    }
                    myAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(this, VisualizzaTaskDocenteActivity.class);
        Task task =taskArrayList.get(position);
        intent.putExtra("task",  task);
        startActivity(intent);
    }
}