package com.example.hal9000.trafficlightapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.content.Context;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.ViewGroup;

public class bluetooth_console extends Fragment {
    private TextView dataPanel;
    private EditText sendBox;
    private Spinner deviceList;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private OutputStream mmOutputStream;
    private InputStream mmInputStream;
    private Thread workerThread;
    private byte[] readBuffer;
    private int readBufferPosition;
    volatile boolean stopWorker;

    private OnFragmentInteractionListener mListener;

    public bluetooth_console() { }

    public static bluetooth_console newInstance() {
        bluetooth_console fragment = new bluetooth_console();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetooth_console, container, false);

        Button openButton =  view.findViewById(R.id.open);
        Button sendButton =  view.findViewById(R.id.send);
        Button closeButton =  view.findViewById(R.id.close);
        Button refreshButton = view.findViewById(R.id.Refresh);

        sendBox = view.findViewById(R.id.entry);
        dataPanel = view.findViewById(R.id.dataPanel);
        deviceList = view.findViewById(R.id.deviceList);
        openButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    openBT();
                } catch (IOException ex) {
                }
            }
        });
        refreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                displayDevices();
            }
        });
        //Send Button
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    sendData();
                } catch (IOException ex) {
                }
            }
        });

        //Close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    closeBT();
                } catch (IOException ex) {
                }
            }
        });
        connectAdapter();
        displayDevices();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener { }


    void displayDevices() {
        ArrayList<String> foundDevices = new ArrayList<String>();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                foundDevices.add(device.getName().toString());
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_spinner_item, foundDevices);
                deviceList.setAdapter(adapter);
            }
        }
    }

    void connectAdapter() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            dataPanel.append("No bluetooth adapter available\n");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }
    }

    void openBT() throws IOException {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {

                String deviceName = deviceList.getSelectedItem().toString();
                if (device.getName().equals(deviceName)) {
                    mmDevice = device;
                    break;
                }
            }
        }
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();
        beginListenForData();

        dataPanel.append("Bluetooth Open\n");
    }

    void beginListenForData() {
        final Handler handler = new Handler();
        //final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    try {
                        int bytesAvailable = mmInputStream.available();
                        if (bytesAvailable > 0) {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);

                            byte[] encodedBytes = new byte[readBufferPosition];
                            System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                            final String data = new String(packetBytes, "US-ASCII");
                            readBufferPosition = 0;

                            handler.post(new Runnable() {
                                public void run() {
                                    dataPanel.append(data);
                                    System.out.println(data);

                                }
                            });


                            Thread.sleep(100);

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        workerThread.start();
    }


    void sendData() throws IOException {
        String msg = sendBox.getText().toString();
        msg += "\n";
        mmOutputStream.write(msg.getBytes());
        dataPanel.append("Data Sent\n");
    }

    void closeBT() throws IOException {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        String tempData = dataPanel.getText().toString();
        dataPanel.setText(tempData + "Bluetooth Closed");
    }
}

