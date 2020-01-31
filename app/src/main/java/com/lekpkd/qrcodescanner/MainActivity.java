package com.lekpkd.qrcodescanner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class MainActivity extends AppCompatActivity implements ZBarScannerView.ResultHandler, View.OnClickListener {

    private static final int REQ_PERMISSION = 1001;
    private ZBarScannerView qrView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        qrView = findViewById(R.id.qrView);
        qrView.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_history) {
            openHistory("");
        } else if (item.getItemId() == R.id.action_create) {
            openGenerator();
        }
        return super.onOptionsItemSelected(item);
    }

    private void openGenerator() {
        Intent intent = new Intent(this, CreateActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQ_PERMISSION);
                return;
            }
        }

        startCapture();
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQ_PERMISSION) {
//                startCapture();
//        }
//    }

    private void startCapture() {
        qrView.setResultHandler(this);
        qrView.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        qrView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        String content = rawResult.getContents();

        openHistory(content);
    }

    private void openHistory(String content) {
        Intent intent = new Intent(this, HistoryActivity.class);
        intent.putExtra(HistoryActivity.CONTENT, content);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.qrView) {
            qrView.startCamera();
        }
    }
}
