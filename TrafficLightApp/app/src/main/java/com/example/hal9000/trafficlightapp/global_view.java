package com.example.hal9000.trafficlightapp;

import android.content.Context;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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
    PinView imageView;
    View view;
    private GestureDetector mDetector;

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
          imageView.setImage(ImageSource.asset("road2.png"));

            final MapPin mapPin = new MapPin(6000f, 0f, 1);
            MapPin mapPin1 = new MapPin(1000f, 5000f, 2);

            ArrayList<MapPin> MapPins = new ArrayList();
            MapPins.add(mapPin);
            MapPins.add(mapPin1);
            imageView.setPins(MapPins);
            final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    if (imageView.isReady()) {
                        PointF sCoord = imageView.viewToSourceCoord(e.getX(), e.getY());
                        Context context = getContext();
                        CharSequence text = "Traffic Light: "+imageView.getPinIdByPoint(sCoord);
                        int duration = Toast.LENGTH_SHORT;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
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

        }
        if(Typology.equals("3 Lights"))
        {

            imageView.setImage(ImageSource.asset("road3.png"));

            MapPin mapPin = new MapPin(6000f, 0f, 1);
            MapPin mapPin1 = new MapPin(1000f, 5000f, 2);
            MapPin mapPin2 = new MapPin(0f, 3000f, 2);

            ArrayList<MapPin> MapPins = new ArrayList();
            MapPins.add(mapPin);
            MapPins.add(mapPin1);
            MapPins.add(mapPin2);
            imageView.setPins(MapPins);      }
        if(Typology.equals("4 Lights"))
        {
            imageView.setImage(ImageSource.asset("road4.png"));        }
    }
    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            Log.d("TAG","onDown: ");

            // don't return false here or else none of the other
            // gestures will work
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.i("TAG", "onSingleTapConfirmed: ");
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.i("TAG", "onLongPress: ");
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.i("TAG", "onDoubleTap: ");
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            Log.i("TAG", "onScroll: ");
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            Log.d("TAG", "onFling: ");
            return true;
        }
    }
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
