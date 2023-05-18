package com.example.controlerealazer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.Base64;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class QrCode extends AppCompatActivity {
    private FirebaseFirestore db;
    private CircleImageView photoIDImageView;
    private ImageView qrCodeImageView;
    private String usuarioID, fotoEmString;
    private TextView nomeTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);
        Objects.requireNonNull(getSupportActionBar()).hide();
        iniciarComponentes();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onStart() {
        super.onStart();
        db = FirebaseFirestore.getInstance();
        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference documentReference = db.collection("Usuarios").document(usuarioID);
        documentReference.addSnapshotListener(
                (documentSnapshot, error) -> {
                    if (documentSnapshot != null) {
                        String nome = documentSnapshot.getString("nome");
                        nomeTextView.setText(nome);
                        fotoEmString = documentSnapshot.getString("foto");
                        criarQrCode(nome);
                        if (fotoEmString != null) {
                            if (!fotoEmString.isEmpty()) {
                                photoIDImageView.setImageBitmap(decodificarFotoString2Bitmap(fotoEmString));
                            }
                        }
                    }
                }
        );
    }

    private void iniciarComponentes() {
        photoIDImageView = findViewById(R.id.imagefotoqrcode);
        qrCodeImageView = findViewById(R.id.qrcode);
        nomeTextView = findViewById(R.id.nome_text_view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Bitmap decodificarFotoString2Bitmap(String fotoEmString) {
        byte[] imageEMBytes = Base64.getDecoder().decode(fotoEmString);
        return BitmapFactory.decodeByteArray(imageEMBytes, 0, imageEMBytes.length);
    }

    private void criarQrCode(String string) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(string, BarcodeFormat.QR_CODE, 300, 300);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            qrCodeImageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}