package com.example.hal9000.trafficlightapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;


public class config extends Fragment {

    private configInterface mListener;
    private Button applyButton;
    private Spinner typologyOptions;
    private Spinner modeOptions;
    private Spinner distanceOptions;
    private EditText distanceCustomOptions;
    private TextView warningText;
    private ProgressBar responseProgress;
    private Switch constructionSwitch;
    private String typologyOptionsValue;
    private String modeOptionsValue;
    private int distanceOptionsValue;
    private int constructionSwitchValue = 0;
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

        bf = bluetoothFunctions.getInstance();

        typologyOptions = view.findViewById(R.id.typologySpinner);
        modeOptions = view.findViewById(R.id.modeSpinner);
        distanceOptions = view.findViewById(R.id.distanceSpinner);
        distanceCustomOptions = view.findViewById(R.id.distanceCustom);
        applyButton = view.findViewById(R.id.applyButton);
        warningText = view.findViewById(R.id.warningConfig);
        responseProgress = view.findViewById(R.id.configLoading);
        constructionSwitch = view.findViewById(R.id.constructionSwitch);

        constructionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    constructionSwitchValue = 1;
                } else {
                    constructionSwitchValue = 0;
                }
            }
        });

        applyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopWorker = true;
                if (bf.hasDevice() && bf.isConnected()) {
                    try {
                        sendData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getActivity(), "Please connect to a device first", Toast.LENGTH_LONG).show();
                }

            }
        });

        return view;
    }

    private void sendData() throws IOException {
        typologyOptionsValue = typologyOptions.getSelectedItem().toString();
        distanceOptionsValue = 100;
        boolean valid = true;
        if (!TextUtils.isEmpty(distanceCustomOptions.getText().toString())) {
            int temp = Integer.parseInt(distanceCustomOptions.getText().toString());
            if (temp < 100 || temp > 3000) {
                Toast.makeText(getActivity(), "Distance must be between 100 and 3000", Toast.LENGTH_LONG).show();
                valid = false;
            } else {
                distanceOptionsValue = temp;
            }
        } else {
            distanceOptionsValue = Integer.parseInt(distanceOptions.getSelectedItem().toString());
        }

        modeOptionsValue = modeOptions.getSelectedItem().toString();
        if (valid) {

            String msg = createMessage(typologyOptionsValue, modeOptionsValue, distanceOptionsValue, constructionSwitchValue);
            boolean sendStatus = bf.sendData(msg);
            if (sendStatus == true) {
                mListener.updateGlobal(typologyOptionsValue);
                Toast.makeText(getActivity(), "Configured", Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(getActivity(), "Configuration Failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    private String createMessage(String typo, String mode, int dist, int construction) {
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
        double distD = dist;

        distD = (100 * Math.ceil(distD / 100));
        dist = (int) distD;
        if (dist < 1000) {
            distCode = "0" + (dist / 100); // 01 = 100, 02 = 200 etc.
        } else {
            distCode = "" + (dist / 100); // 10 = 1000, 11 = 1100 etc.
        }

        return "C:" + typoCode + "" + modeCode + "" + distCode + "" + construction;
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

    public void updateConfiguration(int typology, int mode, int distance, boolean construction) {
        typologyOptions.setSelection(typology);
        modeOptions.setSelection(mode);
        distanceCustomOptions.setText("" + distance);
        constructionSwitch.setChecked(construction);
        try {
            mListener.updateGlobal(typologyOptions.getSelectedItem().toString());
        } catch (IOException e) {
            Toast.makeText(getActivity(), "Failed to update Global View", Toast.LENGTH_LONG).show();
        }
    }

    public interface configInterface {
        void updateGlobal(String typology) throws IOException;

    }
}
