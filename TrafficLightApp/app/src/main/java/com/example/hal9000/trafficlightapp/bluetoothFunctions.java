package com.example.hal9000.trafficlightapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class bluetoothFunctions {
    private BluetoothDevice device;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private byte[] readBuffer;
    private int readBufferPosition;
    volatile boolean stopWorker;
    private boolean connected = false;
    private boolean hasAdapter = false;
    private int messageLength = 7;
    private static final bluetoothFunctions INSTANCE = new bluetoothFunctions();

    private bluetoothFunctions() {

    }

    public static bluetoothFunctions getInstance() {
        return INSTANCE;
    }

    public void connectAdapter() {
        if (hasAdapter == false) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                System.out.println("No Adapter Found");
            }
        }
    }

    public boolean hasDevice() {
        if (device == null) {
            return false;
        }
        return true;
    }

    public boolean isConnected() {
        if (connected) {
            return true;
        }
        return false;
    }

    public boolean connectToDevice(String s) throws IOException {
        connectAdapter();
        if (connected) {
            closeBT();
        }
        boolean res = false;
        Set<BluetoothDevice> pairedDevices = getDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {


                if (device.getName().equals(s)) {
                    this.device = device;
                    connected = openConnection();
                    res = sendData("Test");
                    break;
                }
            }
        }
        return res;
    }

    public Set<BluetoothDevice> getDevices() {
        connectAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        return pairedDevices;
    }

    public boolean openConnection() throws IOException {
        if (connected) {
            closeBT();
        }
        if (device != null) {
            try {
                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
                socket = device.createRfcommSocketToServiceRecord(uuid);
                socket.connect();
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
                return true;
            } catch (IOException e) {
                System.out.println("Connection Failed");
                return false;
            }
        } else {
            System.out.println("No device");
            return false;
        }
    }

    public boolean sendData(String s) throws IOException {
        if (outputStream != null) {
            if(s.length()>messageLength)
            {
                System.out.println("Message is too long");
                return false;
            }
            while(s.length() < messageLength)
            {
                s += "$";
            }
            s +="\n";
            outputStream.write(s.getBytes());
            System.out.println("Send Data:" + s);
            return true;
        } else {
            System.out.println("Send message failed");
            return false;
        }
    }


    public String listenForResponse() throws IOException {
        if (connected) {
            int bytesAvailable = inputStream.available();
            readBufferPosition = 0;
            readBuffer = new byte[1024];
            if (bytesAvailable > 0) {
                byte[] packetBytes = new byte[bytesAvailable];
                inputStream.read(packetBytes);
                byte[] encodedBytes = new byte[readBufferPosition];
                System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                final String data = new String(packetBytes, "US-ASCII");
                readBufferPosition = 0;
                return data;
            }
        }
        return null;
    }

    public void closeBT() throws IOException {
        stopWorker = true;
        if (outputStream != null) {
            outputStream.close();
        }
        if (inputStream != null) {
            inputStream.close();
        }
        if (socket != null) {
            socket.close();
        }
        connected = false;
    }
}


