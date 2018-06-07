package com.example.hal9000.trafficlightapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static android.content.Context.NOTIFICATION_SERVICE;

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
    private TextView batteryText;
    private Button backButton;
    private Executor executor = Executors.newSingleThreadExecutor();
    private bluetoothFunctions bf;
    volatile boolean stopWorker;
    private trafficLight trafficLight;
    private Spinner spinnerMonitor;
    private ArrayList<trafficLight> trafficLightList = new ArrayList();


    //NotificationManager notificationManager;

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
        initVariables();


        backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mListener.returnToGlobal();
            }
        });
        spinnerMonitor = view.findViewById(R.id.spinnerMonitor);
        spinnerMonitor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(!trafficLightList.isEmpty())
                {
                    for (trafficLight tl : trafficLightList) {
                        if(tl.getId() == (position+1))
                        {
                            trafficLight = tl;
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        return view;
    }

    public void initVariables() {
        //  notificationManager = (NotificationManager)
        //        getActivity().getSystemService(NOTIFICATION_SERVICE);
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
        batteryText = view.findViewById(R.id.batteryMonitor);
        bf = bluetoothFunctions.getInstance();
    }

    public void updateInfo(final trafficLight t) {
        trafficLight = t;
        try {
            bf.sendData("M:" + trafficLight.getId());
        } catch (IOException e) {
            Toast.makeText(getActivity(), "Failed to Refresh Data", Toast.LENGTH_LONG).show();
        }
        displayLights();

        stopWorker = false;
        final Handler handler = new Handler();
        executor.execute(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                    handler.post(new Runnable() {
                        public void run() {
                            idText.setText(Integer.toString(trafficLight.getId()));
                            stateText.setText(trafficLight.getState());
                            substateText.setText(trafficLight.getSubstate());
                            typoText.setText(trafficLight.getTypology());
                            modeText.setText(trafficLight.getMode());
                            densityText.setText(trafficLight.getDensity());
                            distanceText.setText(Integer.toString(trafficLight.getDistance()) + "m");
                            batteryText.setText(trafficLight.getBattery());

                            if (trafficLight.getBattery() == "Deep Discharge" || trafficLight.getBattery() == "Discharged") {
                                displayWarning("lowBatteryWarning");
                            } else {
                                removeWarning("lowBatteryWarning");
                            }
                            if (trafficLight.isCycleDesync()) {
                                displayWarning("desyncWarning");
                            } else {
                                removeWarning("desyncWarning");
                            }
                            if (trafficLight.isFallen()) {
                                displayWarning("fallenWarning");
                            } else {
                                removeWarning("fallenWarning");
                            }
                            if (trafficLight.isOpticalFailure()) {
                                displayWarning("opticalWarning");
                            } else {
                                removeWarning("opticalWarning");
                            }

                            if (trafficLight.isSignalLost()) {
                                displayWarning("signalWarning");
                            } else {
                                removeWarning("signalWarning");
                            }

                            if (trafficLight.getSubstate() == "Green" || trafficLight.getSubstate() == "Green Flashing" || trafficLight.getSubstate() == "Green Barrage") {
                                enableGreen();
                            } else if (trafficLight.getSubstate() == "Yellow" || trafficLight.getSubstate() == "Yellow Barrage" || trafficLight.getSubstate() == "Yellow Flashing") {
                                enableYellow();
                            } else if (trafficLight.getSubstate() == "Red" || trafficLight.getSubstate() == "Full Red" || trafficLight.getSubstate() == "Red Extended" || trafficLight.getSubstate() == "Full Red Barrage" || trafficLight.getSubstate() == "Red Barrage") {
                                enableRed();
                            }

                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    private void displayLights() {
        ArrayList<String> trafficLightStrings = new ArrayList<>();
        trafficLightList = mListener.getLights();

        if (trafficLightList.size() > 0) {
            for (trafficLight tl : trafficLightList) {
                trafficLightStrings.add(Integer.toString(tl.getId()));
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_spinner_item, trafficLightStrings);
                spinnerMonitor.setAdapter(adapter);
            }
        }
    }

    public void enableGreen() {
        greenImage.setVisibility(View.VISIBLE);
        yellowImage.setVisibility(View.INVISIBLE);
        redImage.setVisibility(View.INVISIBLE);
    }

    public void enableYellow() {
        greenImage.setVisibility(View.INVISIBLE);
        yellowImage.setVisibility(View.VISIBLE);
        redImage.setVisibility(View.INVISIBLE);
    }

    public void enableRed() {
        greenImage.setVisibility(View.INVISIBLE);
        yellowImage.setVisibility(View.INVISIBLE);
        redImage.setVisibility(View.VISIBLE);
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

        ArrayList<trafficLight> getLights();


    }

    public void displayWarning(String s) {
        Resources res = getResources();
        int id = res.getIdentifier(s, "id", getContext().getPackageName());
        LinearLayout tempLayout = view.findViewById(id);
        if (tempLayout.getVisibility() == View.INVISIBLE) {
            tempLayout.setVisibility(View.VISIBLE);
            //  notifyWarning(message);
        }
    }

    public void removeWarning(String s) {
        Resources res = getResources();
        int id = res.getIdentifier(s, "id", getContext().getPackageName());
        LinearLayout tempLayout = view.findViewById(id);
        if (tempLayout.getVisibility() == View.VISIBLE) {
            tempLayout.setVisibility(View.INVISIBLE);
        }
    }


}