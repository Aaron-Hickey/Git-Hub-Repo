package com.example.hal9000.trafficlightapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;

public class config extends Fragment {

    private configInterface mListener;
    private Button applyButton;
    private Spinner typologyOptions;
    private Spinner modeOptions;
    private Spinner distanceOptions;

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
        typologyOptions = view.findViewById(R.id.typologySpinner);
        modeOptions = view.findViewById(R.id.modeSpinner);
        distanceOptions = view.findViewById(R.id.distanceSpinner);
        applyButton = view.findViewById(R.id.applyButton);
        applyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String typologyOptionsValue = typologyOptions.getSelectedItem().toString();
                int distanceOptionsValue = Integer.parseInt(distanceOptions.getSelectedItem().toString());
                String modeOptionsValue = modeOptions.getSelectedItem().toString();

                mListener.updateGlobal(typologyOptionsValue, distanceOptionsValue, "","");
                System.out.println(typologyOptionsValue);
                System.out.println(distanceOptionsValue);
                System.out.println(modeOptionsValue);

            }
        });
        return view;
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
        void updateGlobal(String typology, int distance, String synch, String mode);

    }
}
