package com.opentok.android.demo.helloworld;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.opentok.android.OpentokException;
import com.opentok.android.Publisher;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.demo.R;

/**
 * This application demonstrates the basic workflow for getting started with the OpenTok Android SDK.
 * Basic hello-world activity shows publishing audio and video and subscribing to an audio and video stream
 */
public class HelloWorldActivity extends Activity implements Publisher.Listener, Subscriber.Listener, Session.Listener {

    private static final String LOGTAG = "demo-hello-world";
    // automatically connect during Activity.onCreate
    private static final boolean AUTO_CONNECT = true;
    // automatically publish during Session.Listener.onSessionConnected
    private static final boolean AUTO_PUBLISH = true;
    // automatically subscribe during Session.Listener.onSessionReceivedStream IFF stream is our own
    private static final boolean SUBSCRIBE_TO_SELF = false;


    /* Fill the following variables using your own Project info from the Dashboard */
    // Replace with your generated Session ID
    private static final String SESSION_ID = null;
    // Replace with your generated Token (use Project Tools or from a server-side library)
    private static final String TOKEN = null;
    private RelativeLayout publisherViewContainer;
    private RelativeLayout subscriberViewContainer;
    private Publisher publisher;
    private Subscriber subscriber;
    private Session session;
    private WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main_layout);
        
        publisherViewContainer = (RelativeLayout) findViewById(R.id.publisherview);
        subscriberViewContainer = (RelativeLayout) findViewById(R.id.subscriberview);

        // Disable screen dimming
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "Full Wake Lock");

        if (AUTO_CONNECT) {
            sessionConnect();
        }
    }


    @Override
    public void onStop() {
        super.onStop();

        if (session != null) {
            session.disconnect();
        }

        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!wakeLock.isHeld()) {
            wakeLock.acquire();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    private void sessionConnect() {
        session = Session.newInstance(HelloWorldActivity.this, SESSION_ID, HelloWorldActivity.this);
        session.connect(TOKEN);
    }

    @Override
    public void onSessionConnected() {

        Log.i(LOGTAG, "session connected");

        // Session is ready to publish.
        if (AUTO_PUBLISH) {
            //Create Publisher instance.
            publisher = Publisher.newInstance(HelloWorldActivity.this);
            publisher.setName("My First Publisher");
            publisher.setListener(HelloWorldActivity.this);

            RelativeLayout.LayoutParams publisherViewParams =
                    new RelativeLayout.LayoutParams(publisher.getView().getLayoutParams().width,
                            publisher.getView().getLayoutParams().height);
            publisherViewParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            publisherViewParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            publisherViewParams.bottomMargin = dpToPx(8);
            publisherViewParams.rightMargin = dpToPx(8);
            publisherViewContainer.setLayoutParams(publisherViewParams);
            publisherViewContainer.addView(publisher.getView());
            session.publish(publisher);
        }

    }


    @Override
    public void onSessionDroppedStream(Stream stream) {
        Log.i(LOGTAG, String.format("stream dropped", stream.toString()));
        
        boolean isMyStream = session.getConnection().equals(stream.getConnection());
        if ((SUBSCRIBE_TO_SELF && isMyStream) || (!SUBSCRIBE_TO_SELF && !isMyStream)) {
            subscriber = null;
            subscriberViewContainer.removeAllViews();
        }
    }

    @SuppressWarnings("unused")
    @Override
    public void onSessionReceivedStream(final Stream stream) {
        Log.i(LOGTAG, "session received stream");

        boolean isMyStream = session.getConnection().equals(stream.getConnection());
        //If this incoming stream is our own Publisher stream and subscriberToSelf is true let's look in the mirror.
        if ((SUBSCRIBE_TO_SELF && isMyStream) || (!SUBSCRIBE_TO_SELF && !isMyStream)) {
            subscriber = Subscriber.newInstance(HelloWorldActivity.this, stream);
            RelativeLayout.LayoutParams params =
                    new RelativeLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels,
                            getResources().getDisplayMetrics().heightPixels);
            View subscriberView = subscriber.getView();
            subscriberView.setLayoutParams(params);
            subscriberViewContainer.addView(subscriber.getView());
            subscriber.setListener(HelloWorldActivity.this);
            session.subscribe(subscriber);
        }
    }

    @Override
    public void onSubscriberConnected(Subscriber subscriber) {
        Log.i(LOGTAG, "subscriber connected");
    }

    @Override
    public void onSessionDisconnected() {
        Log.i(LOGTAG, "session disconnected");
    }

    @Override
    public void onSessionException(OpentokException exception) {
        Log.e(LOGTAG, "session failed! " + exception.toString());
    }

    @Override
    public void onSubscriberException(Subscriber subscriber, OpentokException exception) {
        Log.i(LOGTAG, "subscriber " + subscriber + " failed! " + exception.toString());
    }

    @Override
    public void onPublisherChangedCamera(int cameraId) {
        Log.i(LOGTAG, "publisher changed camera to cameraId: " + cameraId);
    }

    @Override
    public void onPublisherException(OpentokException exception) {
        Log.i(LOGTAG, "publisher failed! " + exception.toString());
    }

    @Override
    public void onPublisherStreamingStarted() {
        Log.i(LOGTAG, "publisher is streaming!");
    }

    @Override
    public void onPublisherStreamingStopped() {
        Log.i(LOGTAG, "publisher disconnected");
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public Subscriber getSubscriber() {
        return subscriber;
    }

    public Session getSession() {
        return session;
    }

    public RelativeLayout getPublisherView() {
        return publisherViewContainer;
    }

    public RelativeLayout getSubscriberView() {
        return subscriberViewContainer;
    }


    /**
     * Converts dp to real pixels, according to the screen density.
     * @param dp A number of density-independent pixels.
     * @return The equivalent number of real pixels.
     */
    private int dpToPx(int dp) {
        double screenDensity = this.getResources().getDisplayMetrics().density;
        return (int) (screenDensity * (double) dp);
    }
}
