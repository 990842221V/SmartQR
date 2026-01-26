package com.example.smartqr.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.smartqr.R;
import com.example.smartqr.model.Ticket;

public class EditTicketDialog extends DialogFragment {

    private Ticket ticket;
    private OnTicketUpdateListener listener;

    public interface OnTicketUpdateListener {
        void onTicketUpdated(Ticket updatedTicket);
    }

    public EditTicketDialog(Ticket ticket) { this.ticket = ticket; }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try { listener = (OnTicketUpdateListener) context; }
        catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnTicketUpdateListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_ticket, null);

        EditText etName = view.findViewById(R.id.et_name);
        EditText etMobile = view.findViewById(R.id.et_mobile);
        EditText etEmail = view.findViewById(R.id.et_email);
        EditText etDescription = view.findViewById(R.id.et_description);
        EditText etAmount = view.findViewById(R.id.et_amount);
        RadioGroup rgPayment = view.findViewById(R.id.rg_payment);
        RadioButton rbPaid = view.findViewById(R.id.rb_paid);
        RadioButton rbUnpaid = view.findViewById(R.id.rb_unpaid);
        Spinner spinnerStatus = view.findViewById(R.id.spinner_status);
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnUpdate = view.findViewById(R.id.btn_update);

        etName.setText(ticket.getName());
        etMobile.setText(ticket.getMobile());
        etEmail.setText(ticket.getEmail());
        etDescription.setText(ticket.getDescription());
        etAmount.setText(String.valueOf(ticket.getAmount()));

        if (ticket.getPaymentStatus().equalsIgnoreCase("paid")) rbPaid.setChecked(true);
        else rbUnpaid.setChecked(true);

        String[] statuses = {"Approved", "Pending", "Declined"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, statuses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equalsIgnoreCase(ticket.getStatus())) {
                spinnerStatus.setSelection(i);
                break;
            }
        }

        btnCancel.setOnClickListener(v -> dismiss());
        btnUpdate.setOnClickListener(v -> {
            ticket.setName(etName.getText().toString());
            ticket.setMobile(etMobile.getText().toString());
            ticket.setEmail(etEmail.getText().toString());
            ticket.setDescription(etDescription.getText().toString());

            try { ticket.setAmount(Double.parseDouble(etAmount.getText().toString())); }
            catch (NumberFormatException e) { ticket.setAmount(0.0); }

            ticket.setPaymentStatus(rgPayment.getCheckedRadioButtonId() == R.id.rb_paid ? "paid" : "unpaid");
            ticket.setStatus(spinnerStatus.getSelectedItem().toString());

            if (listener != null) listener.onTicketUpdated(ticket);
            dismiss();
        });

        builder.setView(view);
        return builder.create();
    }
}
