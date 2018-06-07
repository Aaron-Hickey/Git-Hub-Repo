package com.example.hal9000.trafficlightapp;

import android.content.Context;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.davemorrissey.labs.subscaleview.ImageSource;
import java.io.IOException;
import java.util.ArrayList;



public class global_view extends Fragment {

    private globalInterface mListener;
    private PinView imageView;
    private View view;
    private ArrayList<trafficLight> trafficLightList = new ArrayList();
    private bluetoothFunctions bf;
    private LinearLayout configReminder;

    public global_view() {
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
        view = inflater.inflate(R.layout.fragment_global_view, container, false);
        imageView = view.findViewById(R.id.imageView);
        configReminder = view.findViewById(R.id.ConfigReminder);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof globalInterface) {
            mListener = (globalInterface) context;
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

    public void updateTrafficLights(int id, String state, String substate, String typology, String mode, String density, int distance, String battery, boolean opticalFailure, boolean fallen, boolean cycleDesync, boolean signalLost)
    {
        if(!trafficLightList.isEmpty() && id <= trafficLightList.size() && id > 0) {
            trafficLight tl = trafficLightList.get(id - 1);
            tl.setState(state);
            tl.setSubstate(substate);
            tl.setTypology(typology);
            tl.setMode(mode);
            tl.setDensity(density);
            tl.setDistance(distance);
            tl.setBattery(battery);
            tl.setCycleDesync(cycleDesync);
            tl.setFallen(fallen);
            tl.setOpticalFailure(opticalFailure);
            tl.setSignalLost(signalLost);
        }
    }

    public ArrayList<trafficLight> createTrafficLights(String typology) throws IOException {
        configReminder.setVisibility(View.INVISIBLE);
        int numberOfLights;
        if(typology.equals("2F P Turning"))
        {
            numberOfLights = 2;
        }
        else if(typology.equals("3F P Turning") || typology.equals("3F P PR SE"))
        {
            numberOfLights = 3;
        }
        else
        {
            numberOfLights = 4;
        }

        trafficLightList = new ArrayList();
        ArrayList<MapPin> pinHolder = new ArrayList(); // temporary array to find mapPins by iteration
        MapPin mapPinA = new MapPin(2500f, 6500f, 1);
        MapPin mapPinB = new MapPin(6000f, 1500f, 2);
        MapPin mapPinC = new MapPin(6000f, 6500f, 3);
        MapPin mapPinD = new MapPin(2500f, 1500f, 4);

        pinHolder.add(mapPinA);
        pinHolder.add(mapPinB);
        pinHolder.add(mapPinC);
        pinHolder.add(mapPinD);

        ArrayList<String> stringHolder = new ArrayList();
        stringHolder.add("road2.png");
        stringHolder.add("road3.png");
        stringHolder.add("road4.png");

        ArrayList<MapPin> MapPins = new ArrayList();

        for (int x = 0; x < numberOfLights; x++) {
            System.out.println(x);
            trafficLightList.add(new trafficLight(x + 1, "-", "-", "-" ,"-" , "-", 0,  "-",false,false,false,false));
            MapPins.add(pinHolder.get(x));
        }
        imageView.setImage(ImageSource.asset(stringHolder.get(numberOfLights - 2)));
        imageView.setPins(MapPins);
        addMapPinActionListener();
        return trafficLightList;
    }

    public boolean addMapPinActionListener() {
        final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (imageView.isReady()) {
                    PointF coord = imageView.viewToSourceCoord(e.getX(), e.getY());
                    int id = imageView.getPinIdByPoint(coord);
                    if (id != -1) {
                        for (int x = 0; x < trafficLightList.size(); x++) {
                            if (id == trafficLightList.get(x).getId()) {
                                mListener.updateMonitoring(trafficLightList.get(x));
                            }
                        }
                    }
                }
                return true;
            }
        });
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });
        return false;
    }

    public interface globalInterface {
        void updateMonitoring(trafficLight t);
    }
}
