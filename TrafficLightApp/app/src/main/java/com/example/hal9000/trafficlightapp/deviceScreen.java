package com.example.hal9000.trafficlightapp;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;


public class deviceScreen extends Fragment {

    private deviceScreenInterface mListener;
    private Spinner deviceSpinner;
    private Button connectButton;
    private ImageButton refreshButton;
    private bluetoothFunctions bf;

    public deviceScreen() { }


    public static deviceScreen newInstance() {
        deviceScreen fragment = new deviceScreen();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bf = bluetoothFunctions.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_device_screen, container, false);
        deviceSpinner = view.findViewById(R.id.deviceScreenDeviceList);
        connectButton = view.findViewById(R.id.deviceScreenConnectButton);
        connectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    boolean res = openBT();
                    {
                        if (res == true) {
                            Toast.makeText(getActivity(), "Connected", Toast.LENGTH_LONG).show();
                            mListener.returnToGlobal();
                            bf.sendData("G:");
                        }
                        else
                        {
                            Toast.makeText(getActivity(), "Failed to Connect", Toast.LENGTH_LONG).show();
                        }
                    }

                } catch (IOException e) {
                    Toast.makeText(getActivity(), "Failed to Connect", Toast.LENGTH_LONG).show();
                }
            }
        });
        refreshButton = view.findViewById(R.id.refreshDeviceScreenButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                displayDevices();
            }
        });
        displayDevices();
        return view;
    }

    private void displayDevices() {
        ArrayList<String> foundDevices = new ArrayList<>();
        Set<BluetoothDevice> pairedDevices = bf.getDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                foundDevices.add(device.getName().toString());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_spinner_item, foundDevices);
                deviceSpinner.setAdapter(adapter);
            }
        }
    }

    private boolean openBT() throws IOException {
        String deviceName = deviceSpinner.getSelectedItem().toString();
        return bf.connectToDevice(deviceName);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof deviceScreenInterface) {
            mListener = (deviceScreenInterface) context;
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

    public interface deviceScreenInterface {
        void returnToGlobal();
    }
}
