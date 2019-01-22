package com.lvs.photofinish.communication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
import android.util.Log;

import com.lvs.photofinish.communication.callback.TrackListenerCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Set;

import static com.lvs.photofinish.communication.protocol.message.Message.MESSAGE_LENGTH;

public class BluetoothCommunicationService implements CommunicationService {

    private final BluetoothAdapter blueAdapter;
    private OutputStream outputStream;
    private InputStream inputStream;
    private BluetoothSocket socket;
    private Thread listener;
    private final TrackListenerCallback listenerCallback;
    private boolean listening;


    public BluetoothCommunicationService(final TrackListenerCallback listenerCallback) {
        this.blueAdapter = BluetoothAdapter.getDefaultAdapter();
        this.listenerCallback = listenerCallback;
    }

    private Thread getListener() {
        return new Thread() {
            public void run() {
                while (listening) {
                    try {
                        int inputSize = inputStream.available();
                        inputSize = inputSize - inputSize % MESSAGE_LENGTH;

                        if (inputSize >= MESSAGE_LENGTH) {
                            for(int i = 0; i < inputSize; i += MESSAGE_LENGTH) {
                                byte[] msg = new byte[MESSAGE_LENGTH];
                                inputStream.read(msg);
                                listenerCallback.callback(msg);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    @Override
    public void send(byte[] data) throws IOException {
        outputStream.write(data);
    }

    @Override
    public boolean connect() throws IOException {
        if (blueAdapter != null) {
            if (blueAdapter.isEnabled()) {
                final Set<BluetoothDevice> bondedDevices = blueAdapter.getBondedDevices();
                final Iterator<BluetoothDevice> deviceIterator = bondedDevices.iterator();

                BluetoothDevice device = null;

                while (deviceIterator.hasNext()) {
                    BluetoothDevice bluetoothDevice = deviceIterator.next();
                    if (bluetoothDevice.getName().equals("Photo Finish ")) {
                        device = bluetoothDevice;
                    }
                }

                if (device != null) {
                    ParcelUuid[] uuids = device.getUuids();
                    this.socket = device.createRfcommSocketToServiceRecord(uuids[0].getUuid());
                    this.socket.connect();
                    this.outputStream = socket.getOutputStream();
                    this.inputStream = socket.getInputStream();

                    this.listening = true;
                    this.listener = getListener();
                    this.listener.start();
                    return true;
                } else {
                    Log.e("error", "No appropriate paired devices.");
                }
            } else {
                Log.e("error", "Bluetooth is disabled.");
            }
        }

        Log.e("error", "Bluetooth adapter not found.");

        return false;
    }

    @Override
    public boolean disconnect() throws IOException {
        listening = false;

        if (socket == null) {
            return true;
        }

        socket.close();

        if (!socket.isConnected()) {
            socket = null;
            listener = null;
            outputStream = null;
            inputStream = null;

            return true;
        }

        return false;
    }
}
