package com.example.controlerealazer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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
    private TextView nomeUsuarioTextView, emailUsuarioTextView;
    private Button bt_excluir, bt_editar;
    private FirebaseFirestore db;
    private String fotoEmString, nomeUsuario;

    private ProgressBar progressBar;
    private CircleImageView fotoImageView;
    private ImageView ic_camera, delete_foto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_principal);
        Objects.requireNonNull(getSupportActionBar()).hide();
        iniciarComponentes();
        bt_excluir.setOnClickListener(v -> confirmaExcluirCadastroUsuario());
        bt_editar.setOnClickListener(v -> editarDados());
        ic_camera.setOnClickListener(v -> selecionarFoto());
        delete_foto.setOnClickListener(v -> deletarFotoConfirmacao());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        String email = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail();
        DocumentReference documentReference = db.collection("Usuarios").document(usuarioID);
        documentReference.addSnapshotListener((documentSnapshot, error) -> {
            if (documentSnapshot != null) {
                nomeUsuario = documentSnapshot.getString("nome");
                fotoEmString = documentSnapshot.getString("foto");
                nomeUsuarioTextView.setText(nomeUsuario);
                emailUsuarioTextView.setText(email);
                if (fotoEmString != null) {
                    if (!fotoEmString.isEmpty()) {
                        fotoImageView.setImageBitmap(decodificaFotoString2Bitmap(fotoEmString));
                    }
                }
            }
        });
    }

    private void iniciarComponentes() {
        nomeUsuarioTextView = findViewById(R.id.textNomeUsuario);
        emailUsuarioTextView = findViewById(R.id.textEmailUsuario);
        bt_excluir = findViewById(R.id.bt_excluir);
        bt_editar = findViewById(R.id.bt_editar);
        progressBar = findViewById(R.id.progressbar);
        fotoImageView = findViewById(R.id.imagefoto);
        ic_camera = findViewById(R.id.ic_camera);
        delete_foto = findViewById(R.id.delete_foto);
    }

    private void confirmaExcluirCadastroUsuario() {
        AlertDialog.Builder msgBox = new AlertDialog.Builder(TelaPrincipal.this);
        msgBox.setTitle("Excluir Usuário");
        msgBox.setMessage("Tem certeza que deseja excluir?");
        msgBox.setPositiveButton("Sim", (dialog, which) -> {
            Handler handlerDeslogar = new Handler();
            Handler handlerExcluirCadastro = new Handler();
            progressBar.setVisibility(View.VISIBLE);
            excluirDadosUsuario();
            handlerExcluirCadastro.postDelayed(this::excluirCadastroUsuario, 1000);
            handlerDeslogar.postDelayed(this::deslogar, 3000);
        });
        msgBox.setNegativeButton("Não", (dialog, which) -> {
        }).show();
    }

    private void excluirCadastroUsuario() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.delete().addOnSuccessListener(unused -> Log.d("db", "Sucesso ao excluir Usuário")).addOnFailureListener(e -> Log.d("db_erro", "Erro ao excluir Usuário" + e));
    }

    private void deslogar() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(TelaPrincipal.this, FormLogin.class);
        startActivity(intent);
        finish();

    }

    private void excluirDadosUsuario() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("Usuarios").document(usuarioID)
                .delete()
                .addOnSuccessListener(unused -> Log.d("db", "Sucesso ao excluir os dados"))
                .addOnFailureListener(e -> Log.d("db_erro", "Erro ao excluir os dados" + e));
    }

    private void editarDados() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String usuarioTemp = nomeUsuarioTextView.getText().toString();
        String usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();


        if (!nomeUsuario.equals(usuarioTemp)) {
            Map<String, Object> usuarioHashMap = new HashMap<>();
            if (!nomeUsuario.equals(usuarioTemp)) {
                usuarioHashMap.put("nome", nomeUsuarioTextView.getText().toString());
            }
//        usuarioHashMap.put("foto", fotoEmString);
            db.collection("Usuarios").document(usuarioID).update(usuarioHashMap).addOnSuccessListener(unused -> Log.d("db_sucesso", "Sucesso ao EDITAR os dados")).addOnFailureListener(e -> Log.d("db_erro", "Erro ao EDITAR os dados" + e));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        if (requestCode == 1) {
            try {
                assert data != null;
                fotoEmString = codificaFotoBitmap2String((Bitmap) data.getExtras().get("data"));
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Map<String, Object> usuarioHashMap = new HashMap<>();
                usuarioHashMap.put("foto", fotoEmString);
                db.collection("Usuarios").document(usuarioID).update(usuarioHashMap).addOnSuccessListener(unused -> Log.d("db_sucesso", "Sucesso ao EDITAR os dados")).addOnFailureListener(e -> Log.d("db_erro", "Erro ao EDITAR os dados" + e));

            } catch (Exception e) {
                Log.e("Camera", "Erro ao carregar foto" + e);
            }
        }
    }

    private void selecionarFoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Bitmap decodificaFotoString2Bitmap(String fotoEmString) {
        byte[] imageEMBytes = Base64.getDecoder().decode(fotoEmString);
        return BitmapFactory.decodeByteArray(imageEMBytes, 0, imageEMBytes.length);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String codificaFotoBitmap2String(Bitmap fotoCapturada) {
        fotoImageView.setImageBitmap(fotoCapturada);
        ByteArrayOutputStream streamDaFotoEmBytes = new ByteArrayOutputStream();
        fotoCapturada.compress(Bitmap.CompressFormat.PNG, 70, streamDaFotoEmBytes);
        byte[] fotoEmBytes = streamDaFotoEmBytes.toByteArray();
        return Base64.getEncoder().encodeToString(fotoEmBytes);
    }

    private void deletarFotoConfirmacao() {
        AlertDialog.Builder msgBox = new AlertDialog.Builder(TelaPrincipal.this);
        msgBox.setTitle("Excluir Foto");
        msgBox.setMessage("Tem certeza que deseja excluir?");
        msgBox.setPositiveButton("Sim", (dialog, which) -> {
            deletarFoto();
        });
        msgBox.setNegativeButton("Não", (dialog, which) -> {
        }).show();
    }

    public void deletarFoto() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        Map<String, Object> usuarioHashMap = new HashMap<>();
        usuarioHashMap.put("foto", "");
        db.collection("Usuarios").document(usuarioID).update(usuarioHashMap).addOnSuccessListener(unused -> Log.d("db_sucesso", "Sucesso ao DELETAR foto")).addOnFailureListener(e -> Log.d("db_erro", "Erro ao DELETAR foto " + e));
        Drawable myDrawable = getResources().getDrawable(R.drawable.ic_user);
        fotoImageView.setImageDrawable(myDrawable);
    }
}