package com.example.controlerealazer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class Menu extends AppCompatActivity {
    private ImageView credencialImageView, sairImageView, usuarioImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Objects.requireNonNull(getSupportActionBar()).hide();
        iniciarComponentes();
        sairImageView.setOnClickListener(v -> deslogar());
        credencialImageView.setOnClickListener(v -> TelaQrCode());
        usuarioImageView.setOnClickListener(v-> TelaDadosCadastro());
    }

    private void iniciarComponentes() {
        credencialImageView = findViewById(R.id.credencial_image_view);
        sairImageView = findViewById(R.id.sair_image_view);
        usuarioImageView = findViewById(R.id.usuario_image_view);
    }

    private void deslogar() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, FormLogin.class);
        startActivity(intent);
        finish();
    }

    private void TelaQrCode() {
        Intent intent = new Intent(this, QrCode.class);
        startActivity(intent);
    }
    private void TelaDadosCadastro() {
        Intent intent = new Intent(this, TelaPrincipal.class);
        startActivity(intent);
    }
}