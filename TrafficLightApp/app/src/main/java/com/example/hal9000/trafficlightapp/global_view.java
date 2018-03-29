package com.example.hal9000.trafficlightapp;

import android.content.Context;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import java.util.ArrayList;


public class global_view extends Fragment {


    private OnFragmentInteractionListener mListener;
    private PinView imageView;
    private View view;
    private ArrayList<trafficLight> trafficLightList = new ArrayList();
    public global_view() {}


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
        applyTypology("2 Lights");
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

    public void applyTypology(String Typology)
    {
        if(Typology.equals("2 Lights"))
        {
            trafficLight trafficLight1 = new trafficLight(1);
            trafficLight trafficLight2 = new trafficLight(2);

            trafficLightList.add(trafficLight1);
            trafficLightList.add(trafficLight2);

            imageView.setImage(ImageSource.asset("road2.png"));

            MapPin mapPinA = new MapPin(2500f, 6500f, 1);
            MapPin mapPinB = new MapPin(6000f, 1500f, 2);

            ArrayList<MapPin> MapPins = new ArrayList();
            MapPins.add(mapPinA);
            MapPins.add(mapPinB);
            imageView.setPins(MapPins);
            addMapPinActionListener();


        }
        if(Typology.equals("3 Lights"))
        {
            trafficLight trafficLight1 = new trafficLight(1);
            trafficLight trafficLight2 = new trafficLight(2);
            trafficLight trafficLight3 = new trafficLight(3);

            trafficLightList.add(trafficLight1);
            trafficLightList.add(trafficLight2);
            trafficLightList.add(trafficLight3);

            imageView.setImage(ImageSource.asset("road3.png"));

            MapPin mapPinA = new MapPin(2500f, 6500f, 1);
            MapPin mapPinB = new MapPin(6000f, 1500f, 2);
            MapPin mapPinC = new MapPin(6000f, 6500f, 3);

            ArrayList<MapPin> MapPins = new ArrayList();
            MapPins.add(mapPinA);
            MapPins.add(mapPinB);
            MapPins.add(mapPinC);
            imageView.setPins(MapPins);
            addMapPinActionListener();
        }
        if(Typology.equals("4 Lights")) {
            trafficLight trafficLight1 = new trafficLight(1);
            trafficLight trafficLight2 = new trafficLight(2);
            trafficLight trafficLight3 = new trafficLight(3);
            trafficLight trafficLight4 = new trafficLight(4);

            trafficLightList.add(trafficLight1);
            trafficLightList.add(trafficLight2);
            trafficLightList.add(trafficLight3);
            trafficLightList.add(trafficLight4);

            imageView.setImage(ImageSource.asset("road4.png"));

            MapPin mapPinA = new MapPin(2500f, 6500f, 1);
            MapPin mapPinB = new MapPin(6000f, 1500f, 2);
            MapPin mapPinC = new MapPin(6000f, 6500f, 3);
            MapPin mapPinD = new MapPin(2500f, 1500f, 4);

            ArrayList<MapPin> MapPins = new ArrayList();
            MapPins.add(mapPinA);
            MapPins.add(mapPinB);
            MapPins.add(mapPinC);
            MapPins.add(mapPinD);
            imageView.setPins(MapPins);
            addMapPinActionListener();
        }
    }
    public boolean addMapPinActionListener()
    {
        final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (imageView.isReady()) {
                    PointF sCoord = imageView.viewToSourceCoord(e.getX(), e.getY());
                    int id = imageView.getPinIdByPoint(sCoord);
                    if(id != -1)
                    {
                        for(int x = 0; x<trafficLightList.size(); x++)
                        {
                            if(id == trafficLightList.get(x).getId())
                            {
                                Context context = getContext();
                                CharSequence text = "Traffic Light: "+ id;
                                int duration = Toast.LENGTH_SHORT;
                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();
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
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
