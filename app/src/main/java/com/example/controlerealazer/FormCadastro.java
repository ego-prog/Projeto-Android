package com.example.controlerealazer;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FormCadastro extends AppCompatActivity {
    private EditText edit_nome, edit_email, edit_senha;
    private Button bt_cadastrar;
    final String[] mensagens = {"Preencha todos os dados", "Cadastro realizado com sucesso", "Erro ao Cadastrar"};
    String usuarioID;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_cadastro);
        Objects.requireNonNull(getSupportActionBar()).hide();
        IniciarComponentes();
        bt_cadastrar.setOnClickListener(view -> {
            String nome = edit_nome.getText().toString();
            String email = edit_email.getText().toString();
            String senha = edit_senha.getText().toString();
            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                Alerta(view, mensagens[0]);

            } else {
                progressBar.setVisibility(View.VISIBLE);
                CadastrarUsuario(view, email, senha, nome);

            }

        });
    }

    private void CadastrarUsuario(View view, String email, String senha, String nome) {
        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        SalvarDadosUsuario(nome, email);
                        Alerta(view, mensagens[1]);
                        Intent intent = new Intent(FormCadastro.this, FormLogin.class);
                        Handler handler = new Handler();
                        handler.postDelayed(() -> {
                            startActivity(intent);
                            finish();
                        }, 3000);   //3 seconds

                    } else {
                        String erro;
                        new Handler().postDelayed(() -> progressBar.setVisibility(View.INVISIBLE), 1500);
                        try {
                            throw Objects.requireNonNull(Objects.requireNonNull(task.getException()));
                        } catch (FirebaseAuthWeakPasswordException e) {
                            erro = "Digite uma senha com no mínimo 6 caracteres";

                        } catch (FirebaseAuthUserCollisionException e) {
                            erro = "Esta conta já foi cadastrada";

                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            erro = "Email inválido";

                        } catch (Exception e) {
                            erro = "Erro ao cadastrar usuário";
                        }
                        Alerta(view, erro);
                    }

                });
    }

    private void SalvarDadosUsuario(String nome, String email) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> usuarios = new HashMap<>();
        usuarios.put("nome", nome);
        usuarios.put("email", email);
        usuarios.put("foto", "");

        usuarioID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        DocumentReference documentReference = db.collection("Usuarios").document(usuarioID);
        documentReference
                .set(usuarios)
                .addOnSuccessListener(unused -> Log.d("db", "Sucesso ao Criar os dados"))
                .addOnFailureListener(e -> Log.d("db_erro", "Erro ao Criar os dados" + e));

    }

    private void IniciarComponentes() {
        edit_nome = findViewById(R.id.edit_nome);
        edit_email = findViewById(R.id.edit_email);
        edit_senha = findViewById(R.id.edit_senha);
        bt_cadastrar = findViewById(R.id.bt_cadastrar);
        progressBar = findViewById(R.id.progressbar);
    }

    private void Alerta(View view, String mensagem) {
        Snackbar snackbar = Snackbar.make(view, mensagem, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(Color.WHITE);
        snackbar.setTextColor(Color.BLACK);
        snackbar.show();

    }
}