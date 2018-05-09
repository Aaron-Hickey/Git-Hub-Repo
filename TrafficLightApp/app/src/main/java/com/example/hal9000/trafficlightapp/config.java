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
import android.widget.Toast;

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

    private String typologyOptionsValue;
    private String modeOptionsValue;
    private int distanceOptionsValue;

    private boolean connected = false;
    volatile boolean stopWorker;

    private bluetoothFunctions bf;

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
        try {
            bf = new bluetoothFunctions();
        } catch (IOException e) {
            System.out.println("Failed to create bluetooth object");
        }

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
                openBT();
                warningText.setText("");
                    try {
                        sendData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

            }
        });

        refreshButton = view.findViewById(R.id.refreshConfigButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                displayDevices();
            }
        });
        displayDevices();
        return view;
    }

    void displayDevices() {
        ArrayList<String> foundDevices = new ArrayList<>();
        Set<BluetoothDevice> pairedDevices = bf.getDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                foundDevices.add(device.getName().toString());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_spinner_item, foundDevices);
                configDeviceList.setAdapter(adapter);
            }
        }
    }

    void openBT() {
        String deviceName = configDeviceList.getSelectedItem().toString();
        bf.connectToDevice(deviceName);

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

            String msg = createMessage(typologyOptionsValue, modeOptionsValue, distanceOptionsValue);
            boolean sendStatus = bf.sendData(msg);
            if(sendStatus == true)
            {
                responseProgress.setVisibility(View.VISIBLE);
            }
            else
            {
                warningText.setText("Message Failed To Send");
            }
            //System.out.println(bf.listenForResponse());

            //mmOutputStream.write(msg.getBytes());
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
          Toast.makeText(getActivity(), "Waiting for response...",
                  Toast.LENGTH_LONG).show();
         final Handler handler = new Handler();
          //final byte delimiter = 10; //This is the ASCII code for a newline character
          stopWorker = false;
       //   readBufferPosition = 0;
       //   readBuffer = new byte[1024];
          workerThread = new Thread(new Runnable() {
              public void run() {
                  while (!Thread.currentThread().isInterrupted() && !stopWorker) {

                      try {

                        /*  int bytesAvailable = mmInputStream.available();
                          if (bytesAvailable > 0) {
                              byte[] packetBytes = new byte[bytesAvailable];
                              mmInputStream.read(packetBytes);

                              byte[] encodedBytes = new byte[readBufferPosition];
                              System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                              final String data = new String(packetBytes, "US-ASCII");
                              readBufferPosition = 0;

                              handler.post(new Runnable() {
                                  public void run() {
                                      System.out.println(data);
                                      if (data.equals(positiveResponse)) {
                                          mListener.updateGlobal(mmDevice, typologyOptionsValue, modeOptionsValue, distanceOptionsValue);
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
                            */
                        final String data = bf.listenForResponse();
                          handler.post(new Runnable() {
                              public void run() {
                                  System.out.println(data);
                                  if (data.equals(positiveResponse)) {
                                      mListener.updateGlobal(mmDevice, typologyOptionsValue, modeOptionsValue, distanceOptionsValue);
                                      warningText.setText("");
                                      responseProgress.setVisibility(View.INVISIBLE);
                                      stopWorker = true;
                                      try {
                                          bf.closeBT();
                                      } catch (IOException e) {
                                          e.printStackTrace();
                                      }
                                  }

                              }
                          });
                          Thread.sleep(100);


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


     /* void closeBT() throws IOException {
          stopWorker = true;
          mmOutputStream.close();
          mmInputStream.close();
          mmSocket.close();
          connected = false;
      }*/

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
        void updateGlobal(BluetoothDevice bluetoothDevice, String typology, String mode, int distance);

    }

}
