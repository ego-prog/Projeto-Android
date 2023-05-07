package com.example.controlerealazer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class TelaPrincipal extends AppCompatActivity {
    private TextView nomeUsuarioTextView, emailUsuarioTextView, debugTextView;
    private Button bt_deslogar, bt_excluir, bt_editar;
    private FirebaseFirestore db;
    private String usuarioID, nomeUser, fotoEmString;
    private FirebaseUser user;
    private ProgressBar progressBar;
    private CircleImageView fotoImageView;
    private ImageView ic_camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_principal);
        Objects.requireNonNull(getSupportActionBar()).hide();

        iniciarComponentes();

        bt_deslogar.setOnClickListener(v -> deslogar());
        bt_excluir.setOnClickListener(v -> confirmaExcluirCadastroUsuario());
        bt_editar.setOnClickListener(v -> editarDados());
        ic_camera.setOnClickListener(v -> selecionarFoto());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onStart() {
        super.onStart();
        String email = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
        DocumentReference documentReference = db.collection("Usuarios").document(usuarioID);

        documentReference.addSnapshotListener((documentSnapshot, error) -> {
            if (documentSnapshot != null) {
                nomeUser = documentSnapshot.getString("nome");
                fotoEmString = documentSnapshot.getString("foto");
                nomeUsuarioTextView.setText(documentSnapshot.getString("nome"));
                emailUsuarioTextView.setText(email);
                byte[] imageEMBytes = Base64.getDecoder().decode(fotoEmString);
                Bitmap imagemDecodificada = BitmapFactory.decodeByteArray(imageEMBytes, 0, imageEMBytes.length);
                fotoImageView.setImageBitmap(imagemDecodificada);
            }
        });
    }

    private void iniciarComponentes() {
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        nomeUsuarioTextView = findViewById(R.id.textNomeUsuario);
        emailUsuarioTextView = findViewById(R.id.textEmailUsuario);
        bt_deslogar = findViewById(R.id.bt_deslogar);
        bt_excluir = findViewById(R.id.bt_excluir);
        bt_editar = findViewById(R.id.bt_editar);
        progressBar = findViewById(R.id.progressbar);
        fotoImageView = findViewById(R.id.imagefoto);
        ic_camera = findViewById(R.id.ic_camera);
        debugTextView = findViewById(R.id.DEBUG);
    }

    private void confirmaExcluirCadastroUsuario() {
        AlertDialog.Builder msgBox = new AlertDialog.Builder(TelaPrincipal.this);
        msgBox.setTitle("Excluir Usuário");
        msgBox.setMessage("Tem certeza que deseja excluir?");
        msgBox.setPositiveButton("Sim", (dialog, which) -> {
            progressBar.setVisibility(View.VISIBLE);
            excluirDadosUsuario();
            excluirCadastroUsuario();
            Handler handler = new Handler();
            handler.postDelayed(this::deslogar, 2000);
        });
        msgBox.setNegativeButton("Não", (dialog, which) -> {
                })
                .show();
    }

    private void excluirCadastroUsuario() {
        user.delete()
                .addOnSuccessListener(unused -> Log.d("db", "Sucesso ao excluir Usuário"))
                .addOnFailureListener(e -> Log.d("db_erro", "Erro ao excluir Usuário" + e));
    }

    private void deslogar() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(TelaPrincipal.this, FormLogin.class);
        startActivity(intent);
        finish();

    }

    private void excluirDadosUsuario() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("Usuarios")
                .document(usuarioID)
                .delete()
                .addOnSuccessListener(unused -> Log.d("db", "Sucesso ao excluir os dados"))
                .addOnFailureListener(e -> Log.d("db_erro", "Erro ao excluir os dados" + e));
    }

    private void editarDados() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection("Usuarios").document(usuarioID);
        documentReference.addSnapshotListener((documentSnapshot, error) -> {
            if (documentSnapshot != null) {
                nomeUser = documentSnapshot.getString("nome");
                fotoEmString = documentSnapshot.getString("foto");
            }
        });

        if (nomeUser != nomeUsuarioTextView.getText().toString()) {
            Map<String, Object> usuarioHashMap = new HashMap<>();
            usuarioHashMap.put("nome", nomeUsuarioTextView.getText().toString());
            usuarioHashMap.put("foto", fotoEmString);
            db.collection("Usuarios")
                    .document(usuarioID)
                    .update(usuarioHashMap)
                    .addOnSuccessListener(unused -> Log.d("db_sucesso", "Sucesso ao EDITAR os dados" + usuarioHashMap))
                    .addOnFailureListener(e -> Log.d("db_erro", "Erro ao EDITAR os dados" + e));

        }
       /* db.collection("Usuarios")
                .document(usuarioID)
                .update("nome", nomeUsuario.getText().toString())
                .addOnSuccessListener(unused -> Log.d("db", "Sucesso ao Alterar os dados"))
                .addOnFailureListener(e -> Log.d("db_erro", "Erro ao Alterar os dados" + e));*/
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            try {
                assert data != null;
                Bitmap fotoRegistrada = (Bitmap) data.getExtras().get("data");
                fotoImageView.setImageBitmap(fotoRegistrada);
                byte[] fotoEmBytes;
                ByteArrayOutputStream streamDaFotoEmBytes = new ByteArrayOutputStream();
                fotoRegistrada.compress(Bitmap.CompressFormat.PNG, 70, streamDaFotoEmBytes);
                fotoEmBytes = streamDaFotoEmBytes.toByteArray();
                fotoEmString = Base64.getEncoder().encodeToString(fotoEmBytes);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Map<String, Object> usuarioHashMap = new HashMap<>();
                usuarioHashMap.put("foto", fotoEmString);
                db.collection("Usuarios")
                        .document(usuarioID)
                        .update(usuarioHashMap)
                        .addOnSuccessListener(unused -> Log.d("db_sucesso", "Sucesso ao EDITAR os dados"))
                        .addOnFailureListener(e -> Log.d("db_erro", "Erro ao EDITAR os dados" + e));

            } catch (Exception e) {
                Log.e("Camera", "Erro ao carregar foto" + e);
            }
        }
    }

    private void selecionarFoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 1);
    }

}