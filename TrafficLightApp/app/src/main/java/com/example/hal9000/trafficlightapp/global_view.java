package com.example.hal9000.trafficlightapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;


public class global_view extends Fragment {


    private OnFragmentInteractionListener mListener;
    SubsamplingScaleImageView imageView;
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
        imageView = (SubsamplingScaleImageView)view.findViewById(R.id.imageView);
       // imageView.setImage(ImageSource.asset("road2.png"));

        applyTypology("2 Lights");
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
            imageView.setImage(ImageSource.asset("road2.png"));        }
        if(Typology.equals("3 Lights"))
        {
            imageView.setImage(ImageSource.asset("road3.png"));        }
        if(Typology.equals("4 Lights"))
        {
            imageView.setImage(ImageSource.asset("road4.png"));        }
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
