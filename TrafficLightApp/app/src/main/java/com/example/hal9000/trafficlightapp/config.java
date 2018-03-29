package com.example.hal9000.trafficlightapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;

public class config extends Fragment {

    private OnFragmentInteractionListener mListener;
    Button applyButton;
    Spinner typologyOptions;

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
        applyButton = view.findViewById(R.id.applyButton);
        applyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String typologyOptionsValue = typologyOptions.getSelectedItem().toString();
                mListener.updateGlobal(typologyOptionsValue);
            }
        });
        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);

        public void updateGlobal(String t);

    }
}
