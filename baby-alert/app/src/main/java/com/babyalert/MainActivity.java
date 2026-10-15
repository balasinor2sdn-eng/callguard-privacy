package com.babyalert;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.HapticFeedbackConstants;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.util.Date;

/**
 * One big button. Tapping it sends a push notification through ntfy.sh to the
 * private channel ("topic") that the other phone is subscribed to in the ntfy app.
 */
public class MainActivity extends Activity {

    private static final String SERVER = "https://ntfy.sh/";
    private static final String PREFS = "baby_alert";
    private static final String KEY_TOPIC = "topic";
    private static final String TOPIC_PATTERN = "[A-Za-z0-9_-]{6,64}";

    private SharedPreferences prefs;
    private Button awakeButton;
    private TextView status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        if (!prefs.contains(KEY_TOPIC)) {
            prefs.edit().putString(KEY_TOPIC, randomTopic()).apply();
        }

        awakeButton = findViewById(R.id.awake);
        status = findViewById(R.id.status);

        awakeButton.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            sendAwake();
        });
        findViewById(R.id.setup).setOnClickListener(v -> showSetup());
    }

    private String topic() {
        return prefs.getString(KEY_TOPIC, "");
    }

    private void sendAwake() {
        awakeButton.setEnabled(false);
        showStatus("Sending…", R.color.text_muted);

        final String topic = topic();
        new Thread(() -> {
            boolean ok = post(topic);
            runOnUiThread(() -> {
                awakeButton.setEnabled(true);
                if (ok) {
                    String time = DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date());
                    showStatus("✅ Sent at " + time, R.color.ok);
                    awakeButton.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
                } else {
                    showStatus("❌ Not sent. Check internet and tap again.", R.color.error);
                    awakeButton.performHapticFeedback(HapticFeedbackConstants.REJECT);
                }
            });
        }).start();
    }

    private static boolean post(String topic) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(SERVER + topic).openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Title", "Baby is awake");
            conn.setRequestProperty("Priority", "urgent");
            conn.setRequestProperty("Tags", "baby");
            byte[] body = "Baby just woke up!".getBytes(StandardCharsets.UTF_8);
            try (OutputStream out = conn.getOutputStream()) {
                out.write(body);
            }
            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private void showStatus(String text, int colorRes) {
        status.setText(text);
        status.setTextColor(getColor(colorRes));
    }

    private void showSetup() {
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(pad, pad / 2, pad, 0);

        TextView help = new TextView(this);
        help.setText("Channel name. It must be the same on both phones. "
                + "Tap \"Share\" to send the setup steps to the other phone.");
        box.addView(help);

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setSingleLine(true);
        input.setText(topic());
        box.addView(input);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Setup")
                .setView(box)
                .setPositiveButton("Save", null)
                .setNeutralButton("Share", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (saveTopic(input.getText().toString().trim())) dialog.dismiss();
            });
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                if (saveTopic(input.getText().toString().trim())) {
                    dialog.dismiss();
                    shareSetup();
                }
            });
        });
        dialog.show();
    }

    private boolean saveTopic(String topic) {
        if (!topic.matches(TOPIC_PATTERN)) {
            Toast.makeText(this, "Use 6-64 letters, numbers, - or _", Toast.LENGTH_LONG).show();
            return false;
        }
        prefs.edit().putString(KEY_TOPIC, topic).apply();
        return true;
    }

    private void shareSetup() {
        String text = "Baby Alert setup:\n"
                + "1. Install the free \"ntfy\" app from the Play Store\n"
                + "2. Open it and tap the + button\n"
                + "3. Topic name: " + topic() + "\n"
                + "4. Tap Subscribe. Done!";
        Intent send = new Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(send, "Send setup to…"));
    }

    private static String randomTopic() {
        String chars = "abcdefghijkmnpqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder("baby-");
        for (int i = 0; i < 10; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }
}
