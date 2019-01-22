package com.lvs.photofinish.communication;

import android.util.Log;

import com.lvs.photofinish.communication.callback.SimpleCallback;
import com.lvs.photofinish.communication.callback.TrackListenerCallback;
import com.lvs.photofinish.communication.callback.TrackResponseCallback;
import com.lvs.photofinish.communication.protocol.message.Message;
import com.lvs.photofinish.communication.protocol.message.MessageType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class TrackManager {

    private final SimpleCallback startCallback;
    private final SimpleCallback finishCallback;
    private final TrackResponseCallback trackFinishCallback;
    private final SimpleCallback connectCallback;
    private final SimpleCallback disconnectedCallback;

    private final UsbCommunicationService usbCommunicationService;
    private final BluetoothCommunicationService bluetoothCommunicationService;

    public TrackManager(final SimpleCallback startCallback,
                        final SimpleCallback finishCallback,
                        final TrackResponseCallback trackFinishCallback,
                        final SimpleCallback connectCallback,
                        final SimpleCallback disconnectedCallback,
                        final UsbCommunicationService usbCommunicationService) {
        this.startCallback = startCallback;
        this.finishCallback = finishCallback;
        this.trackFinishCallback = trackFinishCallback;
        this.connectCallback = connectCallback;
        this.disconnectedCallback = disconnectedCallback;
        this.usbCommunicationService = usbCommunicationService;
        this.bluetoothCommunicationService = new BluetoothCommunicationService(trackResponseCallback);
    }

    final TrackListenerCallback trackResponseCallback = new TrackListenerCallback() {
        @Override
        public void callback(byte[] bytes) {
            Log.d("debug", Arrays.toString(bytes));

            Message message = null;

            try {
                message = new Message(bytes);
            } catch (Exception e) {
                Log.e("error", String.format("Skipping track message: %s, reason: %s", Arrays.toString(bytes), e.getMessage()));
            }

            if (message == null) {
                return;
            }

            final MessageType type = message.getType();
            final byte[] data = message.getData();

            switch (type) {
                case TEST:
                    Log.d("debug", Arrays.toString(bytes));
                    break;
                case RACE:
                    startCallback.callback();
                    break;
                case FINISH:
                    finishCallback.callback();
                    break;
                case TRACK_1_FINISH:
                    handleTrackFinish(1, data);
                    break;
                case TRACK_2_FINISH:
                    handleTrackFinish(2, data);
                    break;
                default:
                    Log.e("Unknown track command:", Arrays.toString(message.getBytes()));
            }
        }
    };

    private void handleTrackFinish(int trackId, byte[] data) {
        int time = ByteBuffer.wrap(new byte[] {0, 0, data[0], data[1]}).getInt();
        trackFinishCallback.callback(trackId, time);
    }

    public void connect() {
        try {
            if (bluetoothCommunicationService.connect()) {
                connectCallback.callback();
            }
        } catch (IOException e) {
            Log.d("error", "Track connection failed");
        }
    }

    public void disconnect() {
        try {
            if (bluetoothCommunicationService.disconnect()) {
                disconnectedCallback.callback();
            }
        } catch (IOException e) {
            Log.d("error", "Failed to disconnect from track");
        }
    }

    public void reset() {
        final Message message = new Message(MessageType.READY);
        send(message.getBytes());
    }

    public void send(byte[] bytes) {
        try {
            bluetoothCommunicationService.send(bytes);
        } catch (IOException e) {
            Log.d("error", "Failed to send track message");
        }
    }

    public static void main(String[] args) {
        byte b1 = -17 & 0xf;
        System.out.println(b1);
    }

}
