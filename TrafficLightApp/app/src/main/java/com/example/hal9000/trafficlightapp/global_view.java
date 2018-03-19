package com.example.hal9000.trafficlightapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


public class global_view extends Fragment {


    private OnFragmentInteractionListener mListener;
    ImageView roadImage;

    public global_view() {
        // Required empty public constructor
    }


    public static global_view newInstance() {
        global_view fragment = new global_view();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_global_view, container, false);
        roadImage = view.findViewById(R.id.roadView);
        applyTypology("3way");
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
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

    public void applyTypology(String Typology)
    {
        if(Typology.equals("2 Lights"))
        {
            roadImage.setImageResource(R.drawable.ic_road2);
        }
        if(Typology.equals("3 Lights"))
        {
            roadImage.setImageResource(R.drawable.ic_road3);
        }
        if(Typology.equals("4 Lights"))
        {
            roadImage.setImageResource(R.drawable.ic_road4);
        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
