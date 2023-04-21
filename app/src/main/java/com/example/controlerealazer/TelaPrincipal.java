package com.example.controlerealazer;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Objects;

public class TelaPrincipal extends AppCompatActivity {
    private TextView nomeUsuario, emailUsuario;
    private Button bt_deslogar, bt_excluir, bt_editar;
    private FirebaseFirestore db;
    private String usuarioID;
    private FirebaseUser user;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_principal);
        Objects.requireNonNull(getSupportActionBar()).hide();

        iniciarComponentes();

        bt_deslogar.setOnClickListener(v -> deslogar());

        bt_excluir.setOnClickListener(v -> confirmaExcluirCadastroUsuario());

        bt_editar.setOnClickListener(v -> editarDados());
    }

    @Override
    protected void onStart() {
        super.onStart();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        DocumentReference documentReference = db.collection("Usuarios").document(usuarioID);

        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {
                if (documentSnapshot != null) {
                    nomeUsuario.setText(documentSnapshot.getString("nome"));
                    emailUsuario.setText(email);
                }

            }
        });
    }

    private void iniciarComponentes() {
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        nomeUsuario = findViewById(R.id.textNomeUsuario);
        emailUsuario = findViewById(R.id.textEmailUsuario);
        bt_deslogar = findViewById(R.id.bt_deslogar);
        bt_excluir = findViewById(R.id.bt_excluir);
        bt_editar = findViewById(R.id.bt_editar);
        progressBar = findViewById(R.id.progressbar);
    }

    private void confirmaExcluirCadastroUsuario() {
        AlertDialog.Builder msgBox = new AlertDialog.Builder(TelaPrincipal.this);
        msgBox.setTitle("Excluindo Usuário");
        msgBox.setMessage("Tem certeza que deseja excluir?");
        msgBox.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressBar.setVisibility(View.VISIBLE);
                excluirDadosUsuario();
                excluirCadastroUsuario();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        deslogar();
                    }
                }, 3000);
//                deslogar();
            }
        });
        msgBox.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).show();
    }

    private void excluirCadastroUsuario() {
        user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.d("db", "Sucesso ao excluir Usuário");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("db_erro", "Erro ao excluir Usuário" + e.toString());
            }
        });

    }

    private void deslogar() {

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(TelaPrincipal.this, FormLogin.class);
        startActivity(intent);
        finish();

    }

    private void excluirDadosUsuario() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("Usuarios")
                .document(usuarioID)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("db", "Sucesso ao excluir os dados");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("db_erro", "Erro ao excluir os dados" + e.toString());
                    }
                });
    }

    private void editarDados() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("Usuarios")
                .document(usuarioID)
                .update("nome", nomeUsuario.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("db", "Sucesso ao excluir os dados");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("db_erro", "Erro ao excluir os dados" + e.toString());
                    }
                });
    }
}