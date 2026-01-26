package com.example.smartqr;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.util.HashMap;
import java.util.Map;

public class QRScannerFragment extends Fragment {

    private static final int CAMERA_REQUEST_CODE = 100;

    private CompoundBarcodeView barcodeView;

    private View ticketDetailsLayout;
    private TextView txtName, txtID, txtMobile, txtDescription, txtPayment, txtStatus, txtDate;
    private Button btnApprove, btnDecline;
    private ImageView btnClose;

    private DatabaseReference database;

    public QRScannerFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_q_r_scanner, container, false);

        // Scanner
        barcodeView = view.findViewById(R.id.barcode_scanner);

        // Ticket details
        ticketDetailsLayout = view.findViewById(R.id.ticket_details_layout);
        txtName = view.findViewById(R.id.txt_name);
        txtID = view.findViewById(R.id.txt_id);
        txtMobile = view.findViewById(R.id.txt_mobile);
        txtDescription = view.findViewById(R.id.txt_description);
        txtPayment = view.findViewById(R.id.txt_payment);
        txtStatus = view.findViewById(R.id.txt_status);
        txtDate = view.findViewById(R.id.txt_date);

        btnApprove = view.findViewById(R.id.btn_approve);
        btnDecline = view.findViewById(R.id.btn_decline);
        btnClose = view.findViewById(R.id.btn_close);

        database = FirebaseDatabase.getInstance().getReference("tickets");

        // Close ticket details and resume scanner
        btnClose.setOnClickListener(v -> resumeScanner());

        // Camera permission check
        if (hasCameraPermission()) {
            startScanner();
        } else {
            requestCameraPermission();
        }

        return view;
    }

    // ---------------- CAMERA PERMISSION ----------------

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                requireActivity(),
                new String[]{Manifest.permission.CAMERA},
                CAMERA_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanner();
            } else {
                Toast.makeText(getContext(),
                        "Camera permission is required to scan QR codes",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // ---------------- SCANNER ----------------

    private void startScanner() {
        ticketDetailsLayout.setVisibility(View.GONE);
        barcodeView.resume();

        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result.getText() != null) {
                    barcodeView.pause();
                    fetchTicketFromFirebase(result.getText());
                }
            }
        });
    }

    private void resumeScanner() {
        ticketDetailsLayout.setVisibility(View.GONE);
        barcodeView.resume();
    }

    // ---------------- FIREBASE ----------------

    private void fetchTicketFromFirebase(String code) {
        database.child(code).get().addOnSuccessListener(snapshot -> {
            if (!snapshot.exists()) {
                Toast.makeText(getContext(), "Ticket not found", Toast.LENGTH_SHORT).show();
                resumeScanner();
                return;
            }

            txtName.setText(snapshot.child("name").getValue(String.class));
            txtID.setText(snapshot.child("id").getValue(String.class));
            txtMobile.setText(snapshot.child("mobile").getValue(String.class));
            txtDescription.setText(snapshot.child("description").getValue(String.class));
            txtPayment.setText(snapshot.child("payment").getValue(String.class));
            txtDate.setText(snapshot.child("date").getValue(String.class));

            String status = snapshot.child("status").getValue(String.class);
            txtStatus.setText(status != null ? status.toUpperCase() : "UNKNOWN");

            ticketDetailsLayout.setVisibility(View.VISIBLE);

            btnApprove.setOnClickListener(v -> updateStatus(code, "approved"));
            btnDecline.setOnClickListener(v -> updateStatus(code, "declined"));

        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error reading ticket", Toast.LENGTH_SHORT).show();
            resumeScanner();
        });
    }

    private void updateStatus(String code, String status) {
        Map<String, Object> update = new HashMap<>();
        update.put("status", status);

        database.child(code).updateChildren(update)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(),
                            "Ticket " + status.toUpperCase(),
                            Toast.LENGTH_SHORT).show();
                    resumeScanner();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Failed to update status",
                                Toast.LENGTH_SHORT).show());
    }

    // ---------------- LIFECYCLE ----------------

    @Override
    public void onResume() {
        super.onResume();
        if (barcodeView != null) barcodeView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (barcodeView != null) barcodeView.pause();
    }
}
