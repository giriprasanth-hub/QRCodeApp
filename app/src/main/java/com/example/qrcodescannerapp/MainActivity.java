package com.example.qrcodescannerapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Size;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private PreviewView previewView;
    private TextView qrCodeText;
    private BarcodeScanner scanner;
    private ExecutorService cameraExecutor;

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.previewView);
        qrCodeText = findViewById(R.id.qrCodeText);

        // Initialize barcode scanner
        scanner = BarcodeScanning.getClient();

        // Request camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startCamera();
        }

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void startCamera() {
        // Get the ProcessCameraProvider instance asynchronously
        ProcessCameraProvider cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        // Add a listener to the camera provider future
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    // Get the ProcessCameraProvider instance
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                    // Set up preview
                    Preview preview = new Preview.Builder().build();
                    preview.setSurfaceProvider(previewView.getSurfaceProvider());

                    // Set up image analysis for QR code scanning
                    ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                            .setTargetResolution(new Size(1280, 720))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build();

                    imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
                        if (imageProxy.getImage() != null) {
                            InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());

                            // Process image with ML Kit barcode scanner
                            scanner.process(image)
                                    .addOnSuccessListener(barcodes -> {
                                        for (Barcode barcode : barcodes) {
                                            String rawValue = barcode.getRawValue();
                                            if (rawValue != null) {
                                                qrCodeText.setText(rawValue);

                                                // If QR code contains a URL, provide an option to open it
                                                if (barcode.getValueType() == Barcode.TYPE_URL) {
                                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(rawValue));
                                                    startActivity(browserIntent);
                                                }
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to scan QR code", Toast.LENGTH_SHORT).show())
                                    .addOnCompleteListener(task -> imageProxy.close());
                        } else {
                            imageProxy.close();
                        }
                    });

                    // Bind camera to lifecycle
                    CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                    cameraProvider.bindToLifecycle(MainActivity.this, cameraSelector, preview, imageAnalysis);

                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Error starting camera", Toast.LENGTH_SHORT).show();
                }
            }
        }, ContextCompat.getMainExecutor(this));  // Use the main executor for listener
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
