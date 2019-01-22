package com.lvs.photofinish;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.lvs.photofinish.communication.TrackManager;
import com.lvs.photofinish.communication.callback.SimpleCallback;
import com.lvs.photofinish.communication.callback.TrackResponseCallback;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {
    private Button startButton, sendButton, clearButton, stopButton, resetButton;
    private TextView textView;
    private EditText editText;

    private TrackManager trackManager;

    final SimpleCallback startCallback = new SimpleCallback() {
        @Override
        public void callback() {
            tvAppend(textView,"START: start fake timer\n");
        }
    };

    final SimpleCallback finishCallback = new SimpleCallback() {
        @Override
        public void callback() {
            tvAppend(textView,"FINISH: race is over\n");
        }
    };

    final SimpleCallback disconnectedCallback = new SimpleCallback() {
        @Override
        public void callback() {
            setUiEnabled(false);
            tvAppend(textView,"Track disconnected\n");
        }
    };

    final SimpleCallback connectedCallback = new SimpleCallback() {
        @Override
        public void callback() {
            setUiEnabled(true);
            tvAppend(textView,"Track connected\n");
        }
    };

    final TrackResponseCallback trackResponseCallback = new TrackResponseCallback() {
        @Override
        public void callback(Integer trackId, Integer time) {
                final String data = String.format("Track #%s finished in: %s\n", trackId, formatTime(time));
                tvAppend(textView, data);
        }
    };

    private static String formatTime(final Integer time) {
        return new SimpleDateFormat("mm:ss:SSS").format(new Date(time));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUIElements();

        setUiEnabled(false);

        //initUSBService();

        initTrackManager();
    }

    private void initTrackManager() {
        this.trackManager = new TrackManager(
                startCallback,
                finishCallback,
                trackResponseCallback,
                connectedCallback,
                disconnectedCallback,
                null //TODO
        );
    }

    private void initUSBService() {
        /*final UsbCommunicationService usbCommunicationService = new UsbCommunicationService(
                (UsbManager) getSystemService(this.USB_SERVICE),
                trackResponseCallback,
                connectedCallback,
                startCallback,
                disconnectedCallback
        );

        final BroadcastRecieverTemplate usbReceiver = usbCommunicationService.getBroadcastReceiver();
        registerReceiver(usbReceiver.getReceiver(), usbReceiver.getFilter());*/
    }

    private void initializeUIElements() {
        startButton = (Button) findViewById(R.id.buttonStart);
        sendButton = (Button) findViewById(R.id.buttonSend);
        clearButton = (Button) findViewById(R.id.buttonClear);
        stopButton = (Button) findViewById(R.id.buttonStop);
        resetButton = (Button) findViewById(R.id.buttonReset);
        editText = (EditText) findViewById(R.id.editText);
        textView = (TextView) findViewById(R.id.textView);
    }

    public void setUiEnabled(boolean bool) {
        startButton.setEnabled(!bool);
        sendButton.setEnabled(bool);
        stopButton.setEnabled(bool);
        textView.setEnabled(bool);

    }

    public void onClickStart(View view) {
        trackManager.connect();
    }

    public void onClickReset(View view) {
        trackManager.reset();
    }

    public void onClickSendTest(View view) {
        String string = editText.getText().toString();

        byte[] bytes = new byte[string.length()];

        for(int i = 0; i < bytes.length; i++) {
            String s = String.valueOf(string.charAt(i));
            bytes[i] = Byte.parseByte(s);
        }

        trackManager.send(bytes);
        tvAppend(textView, "\nData Sent : " + string + "\n");
    }

    public void onClickStop(View view) {
        trackManager.disconnect();
        setUiEnabled(false);
        tvAppend(textView,"\nSerial Connection Closed! \n");
    }

    public void onClickClear(View view) {
        textView.setText(" ");
    }

    private void tvAppend(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ftv.append(ftext);
            }
        });
    }

}
