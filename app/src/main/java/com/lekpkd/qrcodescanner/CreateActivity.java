package com.lekpkd.qrcodescanner;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import net.glxn.qrgen.android.QRCode;
import net.glxn.qrgen.core.image.ImageType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

public class CreateActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQ_PERMISSION = 1001;
    private static final String TAG = "CreateActivity";
    private TextInputEditText txtText;
    private Button btnGenerate;
    private ImageView img;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        img = findViewById(R.id.imageView);
        txtText = findViewById(R.id.txtText);
        btnGenerate = findViewById(R.id.btnGenerate);
        btnGenerate.setOnClickListener(this);


        btnSave = findViewById(R.id.btnSave);
        btnSave.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnGenerate) {
            generateQR();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQ_PERMISSION == requestCode && resultCode == RESULT_OK) {
            saveFile();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMISSION)
            saveFile();
    }

    private void generateQR() {
        String text = txtText.getText().toString().trim();

        Bitmap bmp = QRCode.from(text).to(ImageType.PNG).withCharset("UTF-8").withSize(100, 100).bitmap();

        img.setImageBitmap(bmp);

        btnSave.setVisibility(View.VISIBLE);

        hideKeyboard(this);
    }

    public void saveFile(View view) {

        saveFile();
    }


    private void saveFile() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_PERMISSION);
                return;
            }
        }

        btnSave.setEnabled(false);

        String text = txtText.getText().toString().trim();
        File file = QRCode.from(text).to(ImageType.PNG).withCharset("utf8").withSize(300, 300).file("QRCode");

        Log.i(TAG, "saveFile: " + file.getAbsolutePath());

        boolean dirExist = true;
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "QRCode");
        if (!dir.exists()) dirExist = dir.mkdir();

        if (dirExist) {
            try {
                String filePath = moveFile(file, dir);
                MediaScannerConnection.scanFile(this, new String[]{filePath}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                Log.i("ExternalStorage", "Scanned " + path + ":");
                                Log.i("ExternalStorage", "-> uri=" + uri);
                            }
                        });

                new AlertDialog
                        .Builder(this)
                        .setTitle(R.string.qr_generated)
                        .setMessage("File saved to " + filePath)
                        .setCancelable(false)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                btnSave.setVisibility(View.GONE);
                            }
                        })
                        .show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void hideKeyboard(Activity activity) {
        View v = activity.getCurrentFocus();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null && v != null;
        imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private String moveFile(File file, File dir) throws IOException {
        File newFile = new File(dir, file.getName());
        FileChannel outputChannel = null;
        FileChannel inputChannel = null;
        try {
            outputChannel = new FileOutputStream(newFile).getChannel();
            inputChannel = new FileInputStream(file).getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);
            inputChannel.close();
        } finally {
            if (inputChannel != null) inputChannel.close();
            if (outputChannel != null) outputChannel.close();
        }
        return newFile.toString();
    }
}
