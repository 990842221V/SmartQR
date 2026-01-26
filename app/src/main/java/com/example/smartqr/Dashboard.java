package com.example.smartqr;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Dashboard extends Fragment {

    private TextView issuedCount, scannedCount, paidCount, unpaidCount;

    public Dashboard() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        issuedCount = view.findViewById(R.id.issued_count);
        scannedCount = view.findViewById(R.id.scanned_count);
        paidCount = view.findViewById(R.id.paid_count);
        unpaidCount = view.findViewById(R.id.unpaid_count);

        FloatingActionButton fabCreateQR = view.findViewById(R.id.fab_create_qr);
        FloatingActionButton fabScanQR = view.findViewById(R.id.fab_scan_qr);

        // Create QR
        fabCreateQR.setOnClickListener(v -> requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, new GenerateQRFragment())
                .addToBackStack(null)
                .commit());

        // Scan QR
        fabScanQR.setOnClickListener(v -> requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, new QRScannerFragment())
                .addToBackStack(null)
                .commit());

        // Load Dashboard stats
        loadDashboardStats();

        return view;
    }

    private void loadDashboardStats() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("tickets");

        database.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {

                int totalIssued = 0;
                int totalScanned = 0;
                int totalPaid = 0;
                int totalUnpaid = 0;

                for (DataSnapshot ticketSnap : task.getResult().getChildren()) {
                    totalIssued++; // Every ticket counts as issued

                    String status = ticketSnap.child("status").getValue(String.class);
                    String payment = ticketSnap.child("payment").getValue(String.class);

                    // Scanned tickets: status not "not_scanned"
                    if (status != null && !status.equalsIgnoreCase("not_scanned")) {
                        totalScanned++;
                    }

                    // Paid / Unpaid
                    if (payment != null && payment.equalsIgnoreCase("PAID")) {
                        totalPaid++;
                    } else {
                        totalUnpaid++;
                    }
                }

                // Update UI
                issuedCount.setText(String.valueOf(totalIssued));
                scannedCount.setText(String.valueOf(totalScanned));
                paidCount.setText(String.valueOf(totalPaid));
                unpaidCount.setText(String.valueOf(totalUnpaid));

            }
        });
    }
}
