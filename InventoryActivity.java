package com.cmelugin.inventory;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class InventoryActivity extends AppCompatActivity {


    public static final String EXTRA_USERNAME = "com.cmelugin.inventory.loginvalue";
    public static final String CHANNEL_ID = "channel_low_stock";
    private RecyclerViewAdapter mAdapter;
    private Database mItemDb;
    private String mUsername;
    private RecyclerView recyclerView;
    private int mSelectedInventoryPosition = RecyclerView.NO_POSITION;
    private InventoryItem mInventoryItem;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        Intent intent = getIntent();
        mUsername = intent.getStringExtra(EXTRA_USERNAME);
        TextView screenTitle = (TextView) findViewById(R.id.invTitle);
        screenTitle.setText(mUsername + "'s Inventory");
        setupAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.clear();
        setupAdapter();
    }

    // Build the adapter. Also used to refresh inventory items.
    public void setupAdapter() {
        mItemDb = Database.getInstance(getApplicationContext());

        recyclerView = findViewById(R.id.grid_recycler_view);

        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 4));
        mAdapter = new RecyclerViewAdapter(loadInventory(mUsername));
        recyclerView.setAdapter(mAdapter);
    }

    // Loads inventory
    private List<InventoryItem> loadInventory(String username) {
        return mItemDb.getInventoryItems(username);
    }

    public void onItemSelected(InventoryItem item) {
        mInventoryItem = item;
    }



    // update quantities
    public void onAnyUpdateQty(View view) {
        int number;
        number = Integer.parseInt(view.getTooltipText().toString());
        if ((mInventoryItem.getQuantity()) + number >= 0) {
            mItemDb.updateQuantity(mInventoryItem.getId(), mInventoryItem.getQuantity(), number);
            int newQty = mInventoryItem.getQuantity() + number;
            mInventoryItem.setQuantity(newQty);
            mAdapter.notifyItemChanged(mInventoryItem.getId());
            onResume();
            checkForLow(mInventoryItem);
        }
        else {
            Toast.makeText(this, "Can't have less than ZERO items!!!", Toast.LENGTH_SHORT).show();
        }
    }


    // On click for new item. Launches AddItemActivity
    public void onAddItemClick(View view) {
        Intent intent = new Intent(this, AddItemActivity.class);
        intent.putExtra(InventoryActivity.EXTRA_USERNAME, mUsername);
        startActivity(intent);
    }

    // On click for delete button. Launches a dialog to confirm
    public void onDeleteButtonClick(View view) {
        // Check to see if item is selected before deleting it
        if (mInventoryItem != null) {

            // Set up confirm dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.confirm_dialog_message)
                    .setTitle(R.string.confirm_dialog_title)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Confirm
                            mItemDb.deleteItem(mInventoryItem);
                            mAdapter.removeItem(mInventoryItem);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Do nothing
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    // Called when decreasing quantities, and if
    private void checkForLow(InventoryItem item) {
        if (item.getNotifyOnLow() == 1) {
            if (item.getQuantity() <= 1) {
                createNotificationChannel();
                createNotification(item.getTitle());
            }
        }
    }

    // Notification channel
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < 26) return;

        CharSequence name = "Low stock";
        String description = "item is out of stock";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);

        //Register channel with system
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    // Notification builder
    private void createNotification(String text) {
        // Create notification with properties
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.star_on)
                .setContentTitle("Stock alert")
                .setContentText("Item out of stock: " + text)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        int notifyInt = text.hashCode();

        // Get compatibility NotificationManager
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // Post notification using ID. if same ID, this notification replaces previous one
        notificationManager.notify(notifyInt, notification);
    }



    // Stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private InventoryItem mInventoryItem;
        private TextView mTextView;

        public ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.recycler_view_item, parent, false));
            itemView.setOnClickListener(this);
            mTextView = itemView.findViewById(R.id.cardTitle);
        }

        public void bind(InventoryItem item, int position) {
            mInventoryItem = item;
            String cardText = item.getTitle() + "\n" + item.getQuantity();
            mTextView.setText(cardText);

            if (mSelectedInventoryPosition == position) {
                // Make item stand out
                mTextView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));
            } else {
                // Make background color something else.
                mTextView.setBackgroundColor(getResources().getColor(R.color.colorSecondaryLight));
            }
        }

        // Called when an item is selected
        @Override
        public void onClick(View view) {
            mAdapter.notifyItemChanged(mSelectedInventoryPosition);
            mSelectedInventoryPosition = getAdapterPosition();
            mAdapter.notifyItemChanged(mSelectedInventoryPosition);
            onItemSelected(mInventoryItem);
        }
    }


    public class RecyclerViewAdapter extends RecyclerView.Adapter<ViewHolder> {

        private List<InventoryItem> mInventoryItems;

        //data is passed into the constructor
        public RecyclerViewAdapter(List<InventoryItem> items) {
            mInventoryItems = items;
        }

        // inflates the cell layout from xml when needed
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
            return new ViewHolder(layoutInflater, parent);
        }

        // binds the data to the TextView in each cell
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.bind(mInventoryItems.get(position), position);
        }

        // total number of cells
        @Override
        public int getItemCount() {
            return mInventoryItems.size();
        }

        // Removes item from the list when it is deleted
        public void removeItem(InventoryItem item) {
            // Find item in the list
            int index = mInventoryItems.indexOf(item);
            if (index >= 0) {
                // Remove item
                mInventoryItems.remove(index);
                notifyItemRemoved(index);
            }
        }

        // Clears the screen so that new items or changed items can be displayed
        public void clear() {
            int size = mInventoryItems.size();
            mInventoryItems.clear();
            notifyItemRangeRemoved(0, size);
        }
    }
}

