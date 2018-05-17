package com.example.hal9000.trafficlightapp;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;

public class monitoring extends Fragment {
    private OnFragmentInteractionListener mListener;
    private View view;
    private ImageView greenImage;
    private ImageView yellowImage;
    private ImageView redImage;
    private TextView idText;
    private TextView stateText;
    private TextView substateText;
    private TextView typoText;
    private TextView modeText;
    private TextView densityText;
    private TextView distanceText;
    private TextView countryText;
    private TextView batteryText;
    private Button backButton;

    private Thread workerThread;
    volatile boolean stopWorker;

    public monitoring() {
    }

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
        displayWarning("lowBatteryWarning");
        displayWarning("fallenWarning");
        displayWarning("signalWarning");
        displayWarning("desyncWarning");
        initVariables();
        disableLight(greenImage);
        disableLight(yellowImage);

        backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mListener.returnToGlobal();
            }
        });

        return view;
    }

    public void initVariables() {
        greenImage = view.findViewById(R.id.greenLight);
        yellowImage = view.findViewById(R.id.yellowLight);
        redImage = view.findViewById(R.id.redLight);
        idText = view.findViewById(R.id.idMonitor);
        stateText = view.findViewById(R.id.stateMonitor);
        substateText = view.findViewById(R.id.substateMonitor);
        typoText = view.findViewById(R.id.typologyMonitor);
        modeText = view.findViewById(R.id.modeMonitor);
        densityText = view.findViewById(R.id.densityMonitor);
        distanceText = view.findViewById(R.id.distanceMonitor);
        countryText = view.findViewById(R.id.countryMonitor);
        batteryText = view.findViewById(R.id.batteryMonitor);
    }

    public void updateInfo(final trafficLight t) {
        stopWorker = false;
        final Handler handler = new Handler();
        workerThread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    handler.post(new Runnable() {
                        public void run() {
                            idText.setText(Integer.toString(t.getId()));
                            stateText.setText(t.getState());
                            substateText.setText(t.getSubstate());
                            typoText.setText(t.getTypology());
                            modeText.setText(t.getMode());
                            densityText.setText(t.getDensity());
                            distanceText.setText(Double.toString(t.getDistance()));
                            countryText.setText(t.getCountry());
                            batteryText.setText(Integer.toString(t.getBattery()));
                        }
                    });
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        workerThread.start();
    }

    public void disableLight(ImageView light) {
        light.setVisibility(View.INVISIBLE);
    }

    public void enableLight(ImageView light) {
        light.setVisibility(View.VISIBLE);
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
        void returnToGlobal();
    }

    public void displayWarning(String s) {
        Resources res = getResources();
        int id = res.getIdentifier(s, "id", getContext().getPackageName());
        LinearLayout tempLayout = view.findViewById(id);
        tempLayout.setVisibility(View.VISIBLE);
    }
}