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
    public void onSessionConnected() {
        Log.i(LOGTAG, "session connected");
        super.onSessionConnected();
        Publisher publisher = this.getPublisher();
        if (null != publisher) {
            publisher.getView().setOnClickListener(new ControlBarClickViewListener(this.getPublisher().getName()));
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        finish();
    }

    @Override
    public void onSessionReceivedStream(final Stream stream) {
        Log.i(LOGTAG, "session received stream");
        super.onSessionReceivedStream(stream);
        if (this.getSubscriber() != null) {
            this.getSubscriber().getView().setOnClickListener(new ControlBarClickViewListener(stream.getName()));
        }
    }

    /**
     * A ControlBarClickViewListener is launched when publisher or subscriber view are clicked.
     */
    private class ControlBarClickViewListener implements View.OnClickListener {
        private String streamName;

        public ControlBarClickViewListener(String streamName) {
            this.streamName = streamName;
        }

        @Override
        public void onClick(View arg0) {
            Publisher publisher = ControlBarActivity.this.getPublisher();
            if (null != publisher) {
                if (null == publisherControlBarView) {
                    publisherControlBarView = new ControlBarView(ControlBarActivity.this,
                            ControlBarView.ViewType.PublisherView, streamName, mainLayout, ControlBarActivity.this,
                            publisher.getPublishVideo(),
                            publisher.getPublishAudio());
                    mainLayout.addView(publisherControlBarView);
                    publisherControlBarView.setVisibility(View.INVISIBLE);
                }
                publisherControlBarView.toggleVisibility();
            }

            Subscriber subscriber = ControlBarActivity.this.getSubscriber();
            if (null != subscriber) {
                if (null == subscriberControlBarView) {
                    subscriberControlBarView = new ControlBarView(ControlBarActivity.this,
                            ControlBarView.ViewType.SubscriberView, streamName, mainLayout,
                            ControlBarActivity.this,
                            subscriber.getSubscribeToVideo(),
                            subscriber.getSubscribeToAudio());
                    ControlBarActivity.this.getSubscriberView().addView(subscriberControlBarView);
                    subscriberControlBarView.setVisibility(View.INVISIBLE);
                }
                subscriberControlBarView.toggleVisibility();
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

