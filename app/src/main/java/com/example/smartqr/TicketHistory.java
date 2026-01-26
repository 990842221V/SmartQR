package com.example.smartqr;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.smartqr.adapter.TicketAdapter;
import com.example.smartqr.dialog.EditTicketDialog;
import com.example.smartqr.model.Ticket;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class TicketHistory extends Fragment
        implements TicketAdapter.OnTicketClickListener, EditTicketDialog.OnTicketUpdateListener {

    private RecyclerView recyclerView;
    private TicketAdapter adapter;
    private List<Ticket> ticketList;
    private List<Ticket> filteredList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout emptyState;
    private TextView txtTotalTickets;
    private EditText etSearch;
    private HorizontalScrollView filterContainer;

    private Button chipAll, chipPaid, chipUnpaid, chipApproved, chipDeclined;
    private String currentFilter = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ticket_history, container, false);

        initializeViews(view);
        setupRecyclerView();
        loadTicketsFromFirebase();
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        emptyState = view.findViewById(R.id.empty_state);
        txtTotalTickets = view.findViewById(R.id.txt_total_tickets);
        etSearch = view.findViewById(R.id.et_search);
        filterContainer = view.findViewById(R.id.filter_container);

        chipAll = view.findViewById(R.id.chip_all);
        chipPaid = view.findViewById(R.id.chip_paid);
        chipUnpaid = view.findViewById(R.id.chip_unpaid);
        chipApproved = view.findViewById(R.id.chip_approved);
        chipDeclined = view.findViewById(R.id.chip_declined);

        FloatingActionButton fabAddTicket = view.findViewById(R.id.fab_add_ticket);
        fabAddTicket.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, new QRScannerFragment())
                    .addToBackStack(null)
                    .commit();
        });

        ImageView btnFilter = view.findViewById(R.id.btn_filter);
        btnFilter.setOnClickListener(v -> {
            if (filterContainer.getVisibility() == View.VISIBLE) {
                filterContainer.setVisibility(View.GONE);
            } else {
                filterContainer.setVisibility(View.VISIBLE);
            }
        });

        Button btnClearSearch = view.findViewById(R.id.btn_clear_search);
        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            filterTickets();
        });

        Button btnScanNew = view.findViewById(R.id.btn_scan_new);
        btnScanNew.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, new QRScannerFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ticketList = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new TicketAdapter(filteredList, this);
        recyclerView.setAdapter(adapter);
    }

    /** Load tickets from Firebase Realtime Database **/
    private void loadTicketsFromFirebase() {
        ticketList.clear();
        filteredList.clear();
        adapter.updateList(filteredList);
        swipeRefreshLayout.setRefreshing(true);

        DatabaseReference ticketsRef = FirebaseDatabase.getInstance().getReference("tickets");
        ticketsRef.get().addOnCompleteListener(task -> {
            swipeRefreshLayout.setRefreshing(false);
            if (task.isSuccessful() && task.getResult() != null) {
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    String id = snapshot.child("id").getValue(String.class);
                    String name = snapshot.child("name").getValue(String.class);
                    String mobile = snapshot.child("mobile").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);
                    String payment = snapshot.child("payment").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);
                    String date = snapshot.child("date").getValue(String.class);

                    double amount = 0; // default if you don’t have amount in Firebase

                    Ticket ticket = new Ticket(id, name, mobile, description,
                            payment != null ? payment.toLowerCase() : "unpaid",
                            status != null ? status.toLowerCase() : "pending",
                            date, amount);

                    ticketList.add(ticket);
                }

                filterTickets();
            } else {
                // Failed to fetch
                checkEmptyState();
            }
        });
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::loadTicketsFromFirebase);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterTickets(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        chipAll.setOnClickListener(v -> setFilter("all"));
        chipPaid.setOnClickListener(v -> setFilter("paid"));
        chipUnpaid.setOnClickListener(v -> setFilter("unpaid"));
        chipApproved.setOnClickListener(v -> setFilter("approved"));
        chipDeclined.setOnClickListener(v -> setFilter("declined"));
    }

    private void setFilter(String filter) {
        currentFilter = filter;
        updateChipStyles();
        filterTickets();
    }

    private void updateChipStyles() {
        chipAll.setBackgroundResource(R.drawable.chip_unselected_background);
        chipPaid.setBackgroundResource(R.drawable.chip_unselected_background);
        chipUnpaid.setBackgroundResource(R.drawable.chip_unselected_background);
        chipApproved.setBackgroundResource(R.drawable.chip_unselected_background);
        chipDeclined.setBackgroundResource(R.drawable.chip_unselected_background);

        chipAll.setTextColor(getResources().getColor(R.color.orange));
        chipPaid.setTextColor(getResources().getColor(R.color.orange));
        chipUnpaid.setTextColor(getResources().getColor(R.color.orange));
        chipApproved.setTextColor(getResources().getColor(R.color.orange));
        chipDeclined.setTextColor(getResources().getColor(R.color.orange));

        switch (currentFilter) {
            case "all": chipAll.setBackgroundResource(R.drawable.chip_selected_background); chipAll.setTextColor(getResources().getColor(R.color.white)); break;
            case "paid": chipPaid.setBackgroundResource(R.drawable.chip_selected_background); chipPaid.setTextColor(getResources().getColor(R.color.white)); break;
            case "unpaid": chipUnpaid.setBackgroundResource(R.drawable.chip_selected_background); chipUnpaid.setTextColor(getResources().getColor(R.color.white)); break;
            case "approved": chipApproved.setBackgroundResource(R.drawable.chip_selected_background); chipApproved.setTextColor(getResources().getColor(R.color.white)); break;
            case "declined": chipDeclined.setBackgroundResource(R.drawable.chip_selected_background); chipDeclined.setTextColor(getResources().getColor(R.color.white)); break;
        }
    }

    private void filterTickets() {
        String searchText = etSearch.getText().toString().toLowerCase();
        filteredList.clear();

        for (Ticket ticket : ticketList) {
            boolean matchesSearch = searchText.isEmpty() ||
                    ticket.getName().toLowerCase().contains(searchText) ||
                    ticket.getTicketId().toLowerCase().contains(searchText) ||
                    ticket.getDescription().toLowerCase().contains(searchText);

            boolean matchesFilter = currentFilter.equals("all") ||
                    (currentFilter.equals("paid") && ticket.getPaymentStatus().equalsIgnoreCase("paid")) ||
                    (currentFilter.equals("unpaid") && ticket.getPaymentStatus().equalsIgnoreCase("unpaid")) ||
                    (currentFilter.equals("approved") && ticket.getStatus().equalsIgnoreCase("approved")) ||
                    (currentFilter.equals("declined") && ticket.getStatus().equalsIgnoreCase("declined"));

            if (matchesSearch && matchesFilter) {
                filteredList.add(ticket);
            }
        }

        adapter.updateList(filteredList);
        updateTotalCount();
        checkEmptyState();
    }

    private void updateTotalCount() {
        txtTotalTickets.setText("Total: " + filteredList.size());
    }

    private void checkEmptyState() {
        if (filteredList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onEditClick(Ticket ticket) {
        EditTicketDialog dialog = new EditTicketDialog(ticket);
        dialog.show(getParentFragmentManager(), "EditTicketDialog");
    }

    @Override
    public void onItemClick(Ticket ticket) {
        // Optional: show ticket details if needed
    }

    @Override
    public void onTicketUpdated(Ticket updatedTicket) {
        for (int i = 0; i < ticketList.size(); i++) {
            if (ticketList.get(i).getId().equals(updatedTicket.getId())) {
                ticketList.set(i, updatedTicket);
                break;
            }
        }
        filterTickets();
    }
}
