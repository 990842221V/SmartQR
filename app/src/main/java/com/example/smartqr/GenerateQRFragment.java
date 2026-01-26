package com.example.smartqr;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class GenerateQRFragment extends Fragment {

    private static final String TAG = "GenerateQRFragment";

    // Input fields
    private TextInputEditText nameInput, idInput, mobileInput, descriptionInput;
    private SwitchMaterial paymentSwitch;

    // Preview views
    private View ticketPreviewCard;
    private ImageView qrCodeImage, templateImageView;
    private TextView ticketStatus;
    private MaterialButton shareButton;

    public GenerateQRFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_generate_q_r, container, false);

        // Initialize input fields
        nameInput = view.findViewById(R.id.name_input);
        idInput = view.findViewById(R.id.id_input);
        mobileInput = view.findViewById(R.id.mobile_input);
        descriptionInput = view.findViewById(R.id.description_input);
        paymentSwitch = view.findViewById(R.id.payment_switch);

        // Initialize preview
        ticketPreviewCard = view.findViewById(R.id.ticket_preview_card);
        templateImageView = view.findViewById(R.id.templateImageView);
        qrCodeImage = view.findViewById(R.id.qr_code_image);
        ticketStatus = view.findViewById(R.id.ticket_status);
        shareButton = view.findViewById(R.id.share_button);

        // Cancel button
        view.findViewById(R.id.cancel_button).setOnClickListener(v -> {
            if (getActivity() != null) getActivity().onBackPressed();
        });

        // Submit button
        view.findViewById(R.id.submit_button).setOnClickListener(v -> generateQRCode());

        // Share button
        shareButton.setOnClickListener(v -> shareTicket());

        return view;
    }

    // ------------------ GENERATE QR & SAVE TICKET ------------------
    private void generateQRCode() {
        String name = nameInput.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            nameInput.setError("Name is required");
            return;
        }

        String id = idInput.getText().toString().trim();
        String mobile = mobileInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        boolean isPaid = paymentSwitch.isChecked();

        // Generate unique 10-character code
        String uniqueCode = generateUniqueCode();

        // Generate QR bitmap with only the unique code
        Bitmap qrBitmap = generateQRBitmap(uniqueCode);

        if (qrBitmap != null) {
            qrCodeImage.setImageBitmap(qrBitmap);
            qrCodeImage.setVisibility(View.VISIBLE);

            ticketStatus.setText(isPaid ? "PAID" : "UNPAID");
            ticketPreviewCard.setVisibility(View.VISIBLE);

            // Save ticket to Firebase under that code
            saveTicketToFirebase(uniqueCode, name, id, mobile, description, isPaid);

            Toast.makeText(getContext(), "QR Code Generated Successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Failed to generate QR code", Toast.LENGTH_SHORT).show();
        }
    }

    // ------------------ UNIQUE CODE GENERATOR ------------------
    private String generateUniqueCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            int randomIndex = (int) (Math.random() * chars.length());
            code.append(chars.charAt(randomIndex));
        }
        return code.toString();
    }

    // ------------------ GENERATE QR BITMAP ------------------
    private Bitmap generateQRBitmap(String data) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512);
            Bitmap bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565);
            for (int x = 0; x < 512; x++) {
                for (int y = 0; y < 512; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ------------------ SAVE TICKET TO FIREBASE WITH STATUS ------------------
    private void saveTicketToFirebase(String code, String name, String id, String mobile,
                                      String description, boolean isPaid) {

        DatabaseReference database = FirebaseDatabase.getInstance().getReference("tickets");

        HashMap<String, Object> ticketData = new HashMap<>();
        ticketData.put("name", name);
        ticketData.put("id", TextUtils.isEmpty(id) ? "N/A" : id);
        ticketData.put("mobile", TextUtils.isEmpty(mobile) ? "N/A" : mobile);
        ticketData.put("description", TextUtils.isEmpty(description) ? "No description" : description);
        ticketData.put("payment", isPaid ? "PAID" : "UNPAID");
        ticketData.put("date", new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date()));
        ticketData.put("status", "not_scanned"); // <-- NEW FIELD

        database.child(code).setValue(ticketData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Ticket saved with code: " + code, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Ticket successfully saved with status field");

                    // Verify Firebase update
                    database.child(code).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                Log.d(TAG, "Firebase verification successful: " + snapshot.getValue());
                                Toast.makeText(getContext(), "Firebase updated successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.e(TAG, "Firebase verification failed: Data not found");
                                Toast.makeText(getContext(), "Firebase verification failed!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Log.e(TAG, "Firebase read failed: " + error.getMessage());
                        }
                    });

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save ticket: " + e.getMessage());
                    Toast.makeText(getContext(), "Failed to save ticket", Toast.LENGTH_SHORT).show();
                });
    }

    // ------------------ SHARE FUNCTION ------------------
    private void shareTicket() {
        if (ticketPreviewCard.getVisibility() != View.VISIBLE) {
            Toast.makeText(getContext(), "Generate ticket first to share!", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap ticketBitmap = Bitmap.createBitmap(ticketPreviewCard.getWidth(),
                ticketPreviewCard.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(ticketBitmap);
        ticketPreviewCard.draw(canvas);

        try {
            File cachePath = new File(getContext().getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "ticket.png");
            FileOutputStream stream = new FileOutputStream(file);
            ticketBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            Uri contentUri = FileProvider.getUriForFile(getContext(),
                    getContext().getPackageName() + ".fileprovider", file);

            if (contentUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setDataAndType(contentUri, getContext().getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, "Share Ticket via"));
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to share ticket", Toast.LENGTH_SHORT).show();
        }
    }

}
