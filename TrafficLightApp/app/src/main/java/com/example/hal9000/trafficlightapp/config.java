package com.example.hal9000.trafficlightapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class config extends Fragment {

    private String positiveResponse = "ok"; // response that the traffic light returns if configuration is successful

    private configInterface mListener;
    private Button applyButton;
    private Spinner typologyOptions;
    private Spinner modeOptions;
    private Spinner distanceOptions;
    private EditText distanceCustomOptions;
    private TextView warningText;
    private ImageButton refreshButton;
    private ProgressBar responseProgress;

    private Spinner configDeviceList;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mmSocket;
    private BluetoothDevice mmDevice;
    private OutputStream mmOutputStream;
    private InputStream mmInputStream;
    private Thread workerThread;
    private byte[] readBuffer;
    private int readBufferPosition;
    private String response;

    private String typologyOptionsValue;
    private String modeOptionsValue;
    private int distanceOptionsValue;

    private boolean connected = false;
    volatile boolean stopWorker;

    public config() {
    }

    public static config newInstance() {
        config fragment = new config();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_config, container, false);
        configDeviceList = view.findViewById(R.id.configDeviceList);

        typologyOptions = view.findViewById(R.id.typologySpinner);
        modeOptions = view.findViewById(R.id.modeSpinner);
        distanceOptions = view.findViewById(R.id.distanceSpinner);
        distanceCustomOptions = view.findViewById(R.id.distanceCustom);
        applyButton = view.findViewById(R.id.applyButton);
        warningText = view.findViewById(R.id.warningConfig);
        responseProgress = view.findViewById(R.id.configLoading);
        applyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    openBT();
                    connected = true;
                    warningText.setText("");
                } catch (IOException e) {
                    if (connected == false) {
                        warningText.setText("Failed to connect");
                    }
                }
                if (connected == true) {
                    try {
                        sendData();
                        //  closeBT();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        refreshButton = view.findViewById(R.id.refreshConfigButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                displayDevices();
            }
        });
        connectAdapter();
        displayDevices();
        return view;
    }

    void displayDevices() {
        ArrayList<String> foundDevices = new ArrayList<>();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                foundDevices.add(device.getName().toString());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_spinner_item, foundDevices);
                configDeviceList.setAdapter(adapter);
            }
        }
    }

    void connectAdapter() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            warningText.setText("No Adapter Found");
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

                String deviceName = configDeviceList.getSelectedItem().toString();
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
        listenForResponse();
        // System.out.println("bt open");
    }

    void sendData() throws IOException {
        typologyOptionsValue = typologyOptions.getSelectedItem().toString();
        distanceOptionsValue = 100;
        boolean valid = true;
        if (!TextUtils.isEmpty(distanceCustomOptions.getText().toString())) {
            int temp = Integer.parseInt(distanceCustomOptions.getText().toString());
            if (temp < 100 || temp > 3000) {
                warningText.setText("Distance must be between 100 and 3000");
                valid = false;
            } else {
                distanceOptionsValue = temp;
            }
        } else {
            distanceOptionsValue = Integer.parseInt(distanceOptions.getSelectedItem().toString());
        }

        modeOptionsValue = modeOptions.getSelectedItem().toString();

        if (valid) {
            responseProgress.setVisibility(View.VISIBLE);
            String msg = createMessage(typologyOptionsValue, modeOptionsValue, distanceOptionsValue);
            mmOutputStream.write(msg.getBytes());
            listenForResponse();
        }
    }

    String createMessage(String typo, String mode, int dist) {
        int typoCode = typologyOptions.getSelectedItemPosition() + 1;

        String modeCode = "0";
        if (mode.equals("Pendular")) {
            modeCode = "0";
        } else if (mode.equals("Red Barrage")) {
            modeCode = "1";
        } else if (mode.equals("Green Force")) {
            modeCode = "2";
        }

        String distCode;
        if (dist < 1000) {
            distCode = "0" + (dist / 100); // 01 = 100, 02 = 200 etc.
        } else {
            distCode = "" + (dist / 100); // 10 = 1000, 11 = 1100 etc.
        }

        return "Config:" + typoCode + "" + modeCode + "" + distCode;
    }

    private void listenForResponse() {
        final Handler handler = new Handler();
        //final byte delimiter = 10; //This is the ASCII code for a newline character
        response = "No response";
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
                                    // dataPanel.append(data);
                                    System.out.println(data);
                                    if (data.equals(positiveResponse)) {
                                        mListener.updateGlobal(typologyOptionsValue, modeOptionsValue, distanceOptionsValue);
                                        warningText.setText("");
                                        responseProgress.setVisibility(View.INVISIBLE);
                                        try {
                                            closeBT();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
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


    void closeBT() throws IOException {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        connected = false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof configInterface) {
            mListener = (configInterface) context;
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

    public interface configInterface {
        void updateGlobal(String typology, String mode, int distance);

    }

}
