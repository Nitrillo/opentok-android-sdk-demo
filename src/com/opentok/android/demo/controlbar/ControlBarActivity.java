package com.opentok.android.demo.controlbar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.opentok.android.Publisher;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;
import com.opentok.android.demo.R;
import com.opentok.android.demo.controlbar.view.ControlBarView;
import com.opentok.android.demo.controlbar.view.ControlBarView.ButtonType;
import com.opentok.android.demo.controlbar.view.ControlBarView.ViewType;
import com.opentok.android.demo.helloworld.HelloWorldActivity;

/**
 * Extends {@link HelloWorldActivity} with some basic controls for the end user.
 */
public class ControlBarActivity extends HelloWorldActivity implements ControlBarView.Listener {
    private static final String LOGTAG = "demo-control-bar";
    private ControlBarView publisherControlBarView;
    private ControlBarView subscriberControlBarView;
    private RelativeLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainLayout = (RelativeLayout) findViewById(R.id.mainlayout);
    }
    
    @Override
    public void onStop() {
        super.onStop();
        finish();
    }

    @Override
    public void onSessionConnected() {
        Log.i(LOGTAG, "session connected");
        super.onSessionConnected();

        // Here, we're assuming the superclass has created a new publisher.
        Publisher publisher = this.getPublisher();

        if (publisher != null) {
            publisherControlBarView = new ControlBarView(ControlBarActivity.this, ViewType.PublisherView,
                publisher.getName(), mainLayout, ControlBarActivity.this, publisher.getPublishAudio());
            mainLayout.addView(publisherControlBarView);
            publisherControlBarView.setVisibility(View.INVISIBLE);

            View publisherView = publisher.getView();
            publisherView.setOnClickListener(new ToggleVisibilityClickListener(publisherControlBarView));

        }
    }

    @Override
    public void onSessionReceivedStream(final Stream stream) {
        Log.i(LOGTAG, "session received stream");
        super.onSessionReceivedStream(stream);

        // Here, we're assuming the superclass has created a new subscriber.
        Subscriber subscriber = this.getSubscriber();
        if (null != subscriber) {
            subscriberControlBarView = new ControlBarView(ControlBarActivity.this, ViewType.SubscriberView,
                    stream.getName(), mainLayout, ControlBarActivity.this, subscriber.getSubscribeToAudio());
            mainLayout.addView(subscriberControlBarView);
            subscriberControlBarView.setVisibility(View.INVISIBLE);

            View subscriberView = subscriber.getView();
            subscriberView.setOnClickListener(new ToggleVisibilityClickListener(subscriberControlBarView));
        }
    }

    /**
     * Toggles visibility of a view when a click event is processed.
     */
    private class ToggleVisibilityClickListener implements View.OnClickListener {
        private View targetView;

        public ToggleVisibilityClickListener(View view) {
            this.targetView = view;
        }

        @Override
        public void onClick(View arg0) {
            Publisher publisher = ControlBarActivity.this.getPublisher();
            if (null == publisher) {
                return;
            }

            if (View.VISIBLE == targetView.getVisibility()) {
                targetView.setVisibility(View.INVISIBLE);
            } else {
                targetView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onOverlayControlButtonClicked(ButtonType buttonType, ViewType viewType, int status) {
        switch (buttonType) {
        case MuteButton:
            switch(viewType) {
            case PublisherView:
                if (0 < status) {
                    this.getPublisher().setPublishAudio(false);
                } else {
                    this.getPublisher().setPublishAudio(true);
                }
                break;
            case SubscriberView:
                if (0 < status) {
                    this.getSubscriber().setSubscribeToAudio(false);
                } else {
                    this.getSubscriber().setSubscribeToAudio(true);
                }
                break;
            default:
                break;
            }
            break;
        case CameraButton:
            this.getPublisher().swapCamera();
            break;
        default:
            Log.wtf(LOGTAG, "unknown button type " + buttonType);
            break;
        }
    }
}

