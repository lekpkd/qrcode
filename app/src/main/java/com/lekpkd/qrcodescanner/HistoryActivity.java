package com.lekpkd.qrcodescanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    public static final String CONTENT = "content_text";
    private static final String SETTING_EDITOR = "settings";
    private static final String PREF_HISTORY = "pref_history";
    private static final String TAG = "HistoryActivity";
    private TextView tvResultText;
    private SharedPreferences pref;
    RecyclerView listView;
    private HistoryAdapter mAdapter;
    private List<ScanedItem> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        pref = getSharedPreferences(SETTING_EDITOR, MODE_PRIVATE);

        tvResultText = findViewById(R.id.tvResultText);
        listView = findViewById(R.id.listView);

        listView.setItemAnimator(new DefaultItemAnimator());
        listView.setHasFixedSize(false);
        listView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new HistoryAdapter();

        listView.setAdapter(mAdapter);

        String content = getIntent().getStringExtra(CONTENT);

        tvResultText.setText(content);

        loadHistory();

        if (content != null) {
            if (!content.equals("")) {

                saveToHistory(content);

            }
        }


    }

    private void saveToHistory(String content) {
        items.add(0,new ScanedItem(content, new Date(System.currentTimeMillis())));
        if(items.size()>50) items.remove(items.size() - 1);
        String jsonText = new Gson().toJson(items);
        Log.i(TAG, "jsonText: " + jsonText);
        pref.edit().putString(PREF_HISTORY, jsonText).apply();
    }

    private void loadHistory() {
        if (pref.contains(PREF_HISTORY)) {
            String jsonText = pref.getString(PREF_HISTORY, "[]");

            Log.i(TAG, "loadHistory: " + jsonText);

            Type listType = new TypeToken<List<ScanedItem>>() {
            }.getType();
            items = new Gson().fromJson(jsonText, listType);
        }
    }

    public void copyText(View view) {
        copyToClipboard(tvResultText.getText());
    }


    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item_view, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            holder.tvContent.setText(items.get(position).getContent());
            holder.tvDate.setText(items.get(position).getDate().toString());
            holder.tvContent.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    tvResultText.setText(holder.tvContent.getText());
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView tvContent, tvDate;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                tvDate = itemView.findViewById(R.id.tvDate);
                tvContent = itemView.findViewById(R.id.tvContent);
            }
        }
    }

    private void copyToClipboard(CharSequence text) {
        if(text.toString().equals("")) return;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(text, text);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, "Text copied to clipboard.", Toast.LENGTH_SHORT).show();
    }
}
