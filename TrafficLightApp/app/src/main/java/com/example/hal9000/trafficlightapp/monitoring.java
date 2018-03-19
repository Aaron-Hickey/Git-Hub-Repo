package com.example.hal9000.trafficlightapp;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;


public class monitoring extends Fragment {
    private OnFragmentInteractionListener mListener;

    View view;
    ImageView greenImage;
    ImageView yellowImage;
    ImageView redImage;

    public monitoring() {}

    public static monitoring newInstance() {
        monitoring fragment = new monitoring();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_monitoring, container, false);
        displayWarning("opticalWarning");
        initVariables();
        disableLight(greenImage);
        disableLight(yellowImage);
        return view;
    }
    public void initVariables()
    {
        greenImage = view.findViewById(R.id.greenLight);
        yellowImage = view.findViewById(R.id.yellowLight);
        redImage = view.findViewById(R.id.redLight);
    }
    public void disableLight(ImageView light)
    {
        light.setVisibility(View.INVISIBLE);
    }
    public void enableLight(ImageView light)
    {
        light.setVisibility(View.VISIBLE);
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
    }

    public void displayWarning(String s) {
        Resources res = getResources();
        int id = res.getIdentifier(s, "id", getContext().getPackageName());
        LinearLayout tempLayout = view.findViewById(id);
        tempLayout.setVisibility(View.VISIBLE);
    }
}
