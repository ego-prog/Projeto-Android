package com.example.controlerealazer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import java.util.Objects;

public class FormCadastro extends AppCompatActivity {
    private EditText edit_nome, edit_email, edit_senha;
    private Button bt_cadastrar;
    String[] mensagens = {"Preencha todos os dados", "Cadastro realizado com sucesso", "Erro ao Cadastrar"};

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
                CadastrarUsuario(view, email, senha);

            }

        });
    }

    private void CadastrarUsuario(View view, String email, String senha) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, senha).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Alerta(view, mensagens[1]);
                    Intent intent = new Intent(FormCadastro.this, FormLogin.class);
                    Handler handler = new Handler();
                    handler.postDelayed(() -> {
                        startActivity(intent);
                        finish();
                    }, 3000);   //3 seconds

                } else {
                    String erro;
                    try {
                        throw task.getException();
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

            }
        });
    }

    private void IniciarComponentes() {
        edit_nome = findViewById(R.id.edit_nome);
        edit_email = findViewById(R.id.edit_email);
        edit_senha = findViewById(R.id.edit_senha);
        bt_cadastrar = findViewById(R.id.bt_cadastrar);
    }

    private void Alerta(View view, String mensagem) {
        Snackbar snackbar = Snackbar.make(view, mensagem, Snackbar.LENGTH_SHORT);
        snackbar.setBackgroundTint(Color.WHITE);
        snackbar.setTextColor(Color.BLACK);
        snackbar.show();

    }
}