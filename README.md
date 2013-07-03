opentok-android-sdk-demo
=======================

This sample app is a basic sample app that shows the most basic features of the OpenTok Android SDK. This app has three activities:

* **com.opentok.android.demo.helloworld.MainAcivity** &#151; This activity provides a user interface for launching either of the
other two activities.

* **com.opentok.android.demo.helloworld.HelloWorldActivity** &#151; Once the app connects to the OpenTok session, it publishes
an audio-video stream to an OpenTok session. The camera view is displayed onscreen. Then, an audio-video stream coming from the
OpenTok server is displayed onscreen; this is either the audio-video stream published by the Android devide or another
stream in the session, based on a Boolean setting in the code.

* **com.opentok.android.demo.controlbar.ControlBarActivity** &#151; This extends the HelloWorldActivity class. It adds user interface
controls for muting the audio video streams, displaying the names of streams, and changing the camera used by the publisher.

*Important:* Read "Testing the sample app" below for information on configuring and testing the sample app.

Also, be sure to read the README file for [the OpenTok Android SDK](https://github.com/opentok/opentok-android-sdk).

Notes
-----

* The OpenTok Android SDK is supported on the Samsung Galaxy S3.
* See the [API reference documentation](http://opentok.github.com/opentok-android-sdk) at the [OpenTok Android SDK project](https://github.com/opentok/opentok-android-sdk) on github.
* You cannot test using OpenTok videos in the ADT emulator.

Testing the sample app
----------------------

1.  Import the project into ADT.

2.  Configure the project to use your own OpenTok session and token. If you don't have an OpenTok API Key yet,
    [sign up for a Developer Account](https://dashboard.tokbox.com/signups/new). Then to generate the session ID
    and token, use the Project Tools on the [Project Details](https://dashboard.tokbox.com/projects) page.

    Open the HelloWorldActivity.java file and set the `SESSION_ID` and `TOKEN` strings to your own session ID and Token,
    respectively.

3.  Connect your Android device to a USB port on your computer. Set up [USB debugging](http://developer.android.com/tools/device.html)
    on your device.

4.  Run the app on your device, selecting MainActivity as the launch action.

    The app should start on your connected device.

    The first time the app runs, it prompts the user to allow the app to use the camera to stream live video.

5.  Tap the Hello World button in the main view of the app. This launches the Hello World activity in a new view.

    Once the app connects to the OpenTok session, it publishes an audio-video
    stream, which is displayed onscreen. Then, the same audio-video stream shows up as a subscribed stream (along with any
    other streams currently in the session).

6. Run the app again, but this time, tap the Control Bar button.

7.  Close the app. Now set up the app to subscribe to audio-video streams other than your own:
    -   Near the top of the HelloWorldActivity.java file, change the `SUBSCRIBE_TO_SELF` property to be set to `false`.
    -   Edit browser_demo.html located in the root directory of this project, and modify the variables `apiKey`, `sessionId`,
        and `token` with your OpenTok API Key, and with the matching session ID and token. (Note that you would normally use
        the OpenTok server-side libraries to issue unique tokens to each client in a session. But for testing purposes, you can
        use the same token on both clients. Also, depending on your app, you may use the OpenTok server-side libraries to generate
        new sessions.)
    -   Run the app on your Android device again.
    -   In a browser on your development computer, load the browser_demo.html file to add more streams to
        the session.
    -   In the web page, click the Connect and Publish buttons.

        ***Note:*** If the web page asks you to set the Flash Player Settings, or if you do not see a display of your camera in
        the page, see the instructions in
        [Flash Player Settings for local testing](http://www.tokbox.com/opentok/docs/js/tutorials/helloworld.html#localTest).

Understanding the code
----------------------

The MainActivity.java file simply contains user interface that launches one of the other activities &#1151; defined in HelloWorldActivity.java and ControlBarActivity.java &#151; in a new view.

### Adding views for videos

When the HelloWorldActivity activity is created, the app adds layout objects for the publisher and subscriber videos:

    publisherView = (RelativeLayout)findViewById(R.id.publisherview);
    subscriberView = (RelativeLayout)findViewById(R.id.subscriberview);

These views will display the Publisher and Subscriber videos. A *Publisher* is an object represents an audio-video stream
sent from the Android device to the OpenTok session. A *Subscriber* is an object that represents an audio-video stream from
the OpenTok session that you display on your device. It can be your own stream or (more commonly) a stream another client
publishes to the OpenTok session.

### Initializing a Session object and connecting to an OpenTok session

The code then calls a method to instantiate an a Session object and connection to the OpenTok session:

    private void sessionConnect(){
      session = Session.newInstance(MainActivity.this, SESSION_ID, MainActivity.this);
      session.connect(TOKEN);
    }

The `Session.newInstance()` static method instantiates a new Session object.

- The first parameter of the method is the Android application context associated with this process.
- The second parameter is the session ID for the OpenTok session your app connects to. You can generate a session ID from the [Developer Dashboard](https://dashboard.tokbox.com/projects) or from a
[server-side library](http://www.tokbox.com/opentok/docs/concepts/server_side_libraries.html).
- The third parameter is the listener to respond to state changes on the session. This listener is defined by the
Session.Listener interface, and the HelloWorldActivity class implements this interface.

The `connect()` method of the Session object connects your app to the OpenTok session. The `TOKEN` constant is the token string for the
client connecting to the session. See [Connection Token Creation](http://www.tokbox.com/docs/concepts/token_creation.html) for details.
You can generate a token from the [Developer Dashboard](https://dashboard.tokbox.com/projects) or from a
[server-side library](http://www.tokbox.com/opentok/docs/concepts/server_side_libraries.html). (In final applications,
use the OpenTok server-side library to generate unique tokens for each user.)

When the app connects to the OpenTok session, the `onSessionConnected()` method of the Session.Listener is called. An app must create
a Session object and connect to the session it before the app can publish or subscribe to streams in the session.


### Publishing an audio-video stream to a session

In the `onSessionConnected()` method, the app instantiates a Publisher object by calling the `Publisher.newInstance()`
static method. :

    publisher = Publisher.newInstance(HelloWorldActivity.this);

The `setName()` method of the Publisher object sets the name of the stream:

    publisher.setName("My First Publisher");

The name of a stream is an optional string that appears at the bottom of the stream's view when the user taps the stream
(or clicks it in a browser).

The `setListener()` method of the Publisher object sets a listener for publisher-related events.

    publisher.setListener(HelloWorldActivity.this);

Note that the HelloWorldActivity class implements the Publisher.Listener interface.

The `getView()` method of the Publisher object returns the view in which the Publisher will display video, and this view
is added to the publisherView we created earlier:

    publisherViewContainer.addView(publisher.getView());

Next, we call the `publish()` method of the Session object, passing in the Publisher object as a parameter:

    session.publish(publisher);

This publishes a stream to the OpenTok session. The `onPublisherStreamingStarted()` method, defined by the Publisher.Listener
interface, is called when the Publisher starts streaming.

### Subscribing to streams

When a stream is added to a session, the `onStreamReceived()` method of the Session.Listener is called.

This app subscribes to one stream, at most. It either subscribes to the stream you publish, or it subscribes to one
of the other streams in the session (if there is one), based on the `SUBSCRIBE_TO_SELF` property, which is set at the
top of the file. Normally, an app would not subscribe to a stream it publishes. (See the last step of "Testing the
sample app" above.) The connection for the stream you publish will match the connection for your session:

    boolean isMyStream = session.getConnection().equals(stream.getConnection());

(***Note:*** in a real application, a client would not normally subscribe to its own published stream. However,
for this test app, it is convenient for the client to subscribe to its own stream.)

The code initializes a Subscriber object for the stream:

    subscriber = Subscriber.newInstance(HelloWorldActivity.this, stream);

The code adds subscriber's view (which contains the video) to the app:

   subscriberViewContainer.addView(subscriber.getView());

The code sets the listener for subscriber-related events, defined by the Subscriber.Listener interface, which the
HelloWorldActivity implements:

    subscriber.setListener(HelloWorldActivity.this);

Finally, the code calls the `subscribe()` method of the Session object to subscribe to the stream

    session.subscribe(subscriber);

The `onSubscriberConnected()` method of the Subscriber.Listener interface is called when the subscriber connects to the
stream.

### Removing dropped streams

As streams leave the session (when clients disconnect or stop publishing), the `onSessionDroppedStream()` method
of the Session.Listener interface is call. The OpenTok Android SDK automatically removed a Subscriber's views when its
stream is dropped.

### Knowing when you have disconnected from the session

When the app disconnects from the session, the `onSessionDisconnected()` method of the Session.Listener
interface is called.

    @Override
    public void onSessionDisconnected() {
      Log.i(LOGTAG, "session disconnected");
    }

If an app cannot connect to the session (perhaps because of no network connection), the `onSessionException()` method
of the Session.Listener interface is called:

    @Override
    public void onSessionException(OpentokException exception) {
      Log.e(LOGTAG, "session failed! " + exception.toString());
    }

### Adding user interface controls for the videos

The ControlBarActivity class extends the HelloWorldActivity class, adding user interface controls to let the user do
the following:

* Display the name of the stream on top of the video
* Toggling publishing of audio in the publisher stream
* Change the camera used by the publisher
* Mute and play subscriber audio

The ControlBarActivity class creates instances of the ControlBarView class for the publisher and subscriber. This class
is a view that provides the user interface for these controls.

The ControlBarActivity.java file defines a ControlBarClickViewListener that implements android.view.View.OnClickListener. 

The ControlBarView.Listener interface defines methods invoked when the user clicks the ControlBarView controls. The
implements this ControlBarView.Listener interface.

The ControlBarView class and its related classes and interfaces are just a sample of how you can add user interface controls
to the OpenTok video views. Your app can use a different user interface that uses the OpenTok Android SDK similarly to the way
that these classes use it. However, you are welcome to use any of the demo app code and user interface controls in your own app.
This README file will discuss the OpenTok Android SDK API used by these classes, without discussing the code that creates the
user interface (which is independent of the OpenTok Android SDK).

#### Displaying the name of the stream

The `onSessionReceivedStream()` method sets a click listener for the view for the Publisher. This listener is defined by the ControlBarClickViewListener class:

    publisher.getView().setOnClickListener(new ControlBarClickViewListener(this.getPublisher().getName()));

The constructor for the ControlBarClickViewListener class takes one parameter: the name to display when the user taps the view.
You obtain the name of a Publisher by calling its `getName()` method.

For a stream that you subscribe to, you obtain the name by calling the `getName()` method of the Stream object.

The `onSessionReceivedStream()` method sets a click listener for the view for the Subscriber.

    this.getSubscriber().getView().setOnClickListener(new ControlBarClickViewListener(stream.getName()));

You obtain the name of the stream you subscribe to by calling its `getName()` method of the Stream object.

The ControlBarView object associated with the publisher or subscriber view displays the name in an android.widget.TextView
object. The ControlBarView is made visible when the user taps the publisher or subscriber view.

#### Toggling publishing of audio in the publisher stream

When the user taps the mute button in the ControlBarView object for the publisher, the `onOverlayControlButtonClicked()`
method of the ControlBarClickViewListener object toggles the publisher's audio. It calls the `setPublishAudio()` method
of the Publisher object, passing in either `true` (publish audio) or `false` (don't publish audio).

#### Changing the camera used by the publisher

When the user taps the toggle camera button in the ControlBarView object for the publisher, the `onOverlayControlButtonClicked()`
method of the ControlBarClickViewListener object toggles the publisher's camera. It calls the `swapCamera()` method
of the Publisher object.

#### Muting and playing subscriber audio

When the user taps the mute button in the ControlBarView object for the subscriber, the `onOverlayControlButtonClicked()`
method of the ControlBarClickViewListener object toggles the subscriber's audio. It calls the `setSubscribeToAudio()` method
of the Subscriber object, passing in either `true` (subscribe to audio) or `false` (don't subscribe to audio).


Next steps
----------

For details on the full OpenTok Android API, see the [reference documentation](http://opentok.github.io/opentok-android-sdk/).
