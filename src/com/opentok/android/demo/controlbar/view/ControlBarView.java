package com.opentok.android.demo.controlbar.view;

import com.opentok.runtime.Workers;
import com.opentok.view.SVGViewButton;
import com.opentok.view.SVGViewButton.SVGButtonLayout;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Camera;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * ControlBarView contains the UI resources to mute/unmute audio for subscriber and publisher
 * and to switch camera for publisher.
 * It shows the name of stream.
 * It is hidden for subscriber and publisher. It appears when subscriber or publisher views are clicked.
 * After 8 seconds, it is hidden.
 */
public class ControlBarView  extends RelativeLayout {
    private static final int CONTROL_BUTTON_WIDTH = 32;
    private static final int CONTROL_BUTTON_HEIGHT = 32;
    public static final int CONTROL_PANEL_HEIGHT = 48;
    private boolean muteState = false;
    private String name;
    private TextView nameBar;
    private LinearLayout leftControlBar;
    private LinearLayout rightControlBar;
    private SVGButtonLayout muteButtonContainer;
    private SVGButtonLayout camButtonContainer;
    private ViewType viewType;
    private RelativeLayout mainLayout;
    private long visibilityExpirationTime;
    private Context context;
    private Boolean showNameBar;
    private Listener controlBarListener;
   
    /**
     * Asynchronous callbacks for classes using a ControlBarView.
     */
    public interface Listener {

        /**
         * Invoked when a click event is processed for this view.
         * @param buttonType The type of button clicked.
         * @param viewType The type of widget (eg. Subscriber/Publisher) associated with this controller.
         * @param status Event-specific code. For example, the mute button latches with values 0 and 1.
         */
        void onOverlayControlButtonClicked(ButtonType buttonType, ViewType viewType, int status);
    }

    /**
     * Layout Mode depending on density.
     **/
    public static enum LayoutMode {
        Minimal, //title bar disabled - 48dp (sub), 96dp (pub)
        Small,   // 150dp
        Medium,  // 320dp
        Large,   // 500dp
    }

    /**
     * ViewType to show controlBarView depending on subscriber or publisher version.
     **/
    public static enum ViewType {
        PublisherView,
        SubscriberView,
    }

    /**
     * ButtonType to launch different actions depending on it.
     **/
    public static enum ButtonType {
        MuteButton,
        CameraButton,
    }

    /**
     * Creates a new ControlBarView.
     * @param context
     * @param type
     * @param name
     * @param mainLayout
     * @param listener
     */
    public ControlBarView(Context context, ViewType type, String name,
        RelativeLayout mainLayout, Listener listener, Boolean hasAudio) {
        super(context);

        this.name = name;
        this.viewType = type;
        this.mainLayout = mainLayout;
        this.context = context;
        this.showNameBar = true;
        this.controlBarListener = listener;

        SVGViewButton mutedView = null;
        SVGViewButton unmutedView = null;


        RelativeLayout.LayoutParams controlParams = new RelativeLayout.LayoutParams(mainLayout.getWidth(),
                dpToPx(CONTROL_PANEL_HEIGHT));

        if (ViewType.SubscriberView.equals(type)) {
            controlParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            controlParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            controlParams.topMargin = dpToPx(6);

        } else if (ViewType.PublisherView.equals(type)) {

            controlParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            controlParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        }
        controlParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        controlParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        controlParams.bottomMargin = dpToPx(8);

        setLayoutParams(controlParams);

        //right-aligned control bar elements
        rightControlBar = new LinearLayout(context);
        rightControlBar.setId(0x0BA5);
        rightControlBar.setBackgroundColor(0xFF282828);

        //left-aligned control bar elements
        leftControlBar = new LinearLayout(context);
        leftControlBar.setId(0x0F00);
        leftControlBar.setBackgroundColor(0xFF282828);

        adjustWidthControlBar();

        RelativeLayout.LayoutParams rightParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT);
        rightControlBar.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);

        RelativeLayout.LayoutParams leftParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT);
        leftParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        leftParams.leftMargin = 8;
        leftControlBar.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        leftControlBar.setLayoutParams(leftParams);
        rightParams.addRule(RelativeLayout.RIGHT_OF, leftControlBar.getId());

        rightControlBar.setLayoutParams(rightParams);

        // camera controls
        if (ViewType.PublisherView == type && 1 < Camera.getNumberOfCameras()) {
            //switch cameraButton
            SVGViewButton camView = new SVGViewButton(context, SVGControlIcons.CAMERA,
                    dpToPx(CONTROL_BUTTON_WIDTH), dpToPx(CONTROL_BUTTON_HEIGHT));
            camButtonContainer = SVGViewButton.createSVGButtonLayout(context, true);
            camButtonContainer.addButton(camView);
            camButtonContainer.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != controlBarListener) {
                        controlBarListener.onOverlayControlButtonClicked(ButtonType.CameraButton,
                                ControlBarView.this.viewType, 0);
                    }
                }
            });
            rightControlBar.addView(camButtonContainer);
        }

        // mute controls
        String mutedIcon;
        String unmutedIcon;

        if (ViewType.PublisherView == type) {
            // mic icon for publishers
            mutedIcon = SVGControlIcons.MIC_MUTED;
            unmutedIcon = SVGControlIcons.MIC_UNMUTED;
        } else {
            //speaker icon for subscribers
            mutedIcon = SVGControlIcons.SPEAKER_MUTED;
            unmutedIcon = SVGControlIcons.SPEAKER_UNMUTED;
        }
      
        mutedView = new SVGViewButton(context, mutedIcon,
                dpToPx(CONTROL_BUTTON_WIDTH), dpToPx(CONTROL_BUTTON_HEIGHT));
        unmutedView = new SVGViewButton(context, unmutedIcon,
                dpToPx(CONTROL_BUTTON_WIDTH), dpToPx(CONTROL_BUTTON_HEIGHT));

        muteButtonContainer = SVGViewButton.createSVGButtonLayout(context, true);
        muteButtonContainer.addButton(unmutedView);
        muteButtonContainer.addButton(mutedView);

        // if audio is initialized as disabled
        if (!hasAudio) {
            muteButtonContainer.swapButtons();
        }
        
        muteButtonContainer.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                muteButtonContainer.swapButtons();
                muteState = !muteState;
                if (null != controlBarListener) {
                    controlBarListener.onOverlayControlButtonClicked(ButtonType.MuteButton,
                            ControlBarView.this.viewType,
                            muteState ? 1 : 0);
                }
            }
        });
        rightControlBar.addView(muteButtonContainer);
        addView(rightControlBar, rightParams);

        // A name tag for the widget, if there is a name associated with the widget.
        nameBar = new TextView(context);
        nameBar.setGravity(Gravity.CENTER);
        nameBar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        nameBar.setTextColor(Color.WHITE);
        nameBar.setText(null != this.name ? this.name : "");

        nameBar.setSingleLine(true);
        adjustNameBarWidth();
        nameBar.setEllipsize(TruncateAt.END);

        nameBar.setPadding(20, 0, 0, 0);
        leftControlBar.addView(nameBar);

        addView(leftControlBar, leftParams);

    }

    /**
     * Inverts the visibility state between View.INVISIBLE and View.VISIBLE.
     */
    public void toggleVisibility() {
        int currentVisibility = getVisibility();
        if (View.VISIBLE == currentVisibility) {
            setVisibility(View.INVISIBLE);
        } else if (View.INVISIBLE == currentVisibility) {
            setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onVisibilityChanged(View view, int visibility) {
        //ensures only the last change from invisible to visible can expire the view below
        visibilityExpirationTime = System.currentTimeMillis() + 7500;

        if (View.VISIBLE == visibility) {
            Workers.submitToMainLoop(new Runnable() {
                @Override
                public void run() {
                    //expire the view if we're still visible and sufficient time has elapsed
                    if (View.VISIBLE == getVisibility() && System.currentTimeMillis() > visibilityExpirationTime) {
                        setVisibility(View.INVISIBLE);
                    }
                }
            }, 8000);
        }
    }

    /**
     * Maintains proper control bar width, based on the available real-estate of the parent.
     */
    public void adjustWidthControlBar() {
        int widthDp = pxToDp(mainLayout.getWidth());

        if (widthDp < 150) {
            setLayoutMode(LayoutMode.Minimal);
        } else if (widthDp < 310) {
            setLayoutMode(LayoutMode.Small);
        } else if (widthDp < 600) {
            setLayoutMode(LayoutMode.Medium);
        } else {
            setLayoutMode(LayoutMode.Large);
        }
        forceLayout();
    }

    @Override
    protected void onSizeChanged(final int width, int height, int oldw, int oldh) {
        adjustWidthControlBar();
    }

    /**
     * Asserts the correct width of the name bar.
     */
    public void adjustNameBarWidth() {

        int buttonOffset = dpToPx(56);

        if (null != camButtonContainer) {
            buttonOffset += dpToPx(48);
        }

        nameBar.setMaxWidth(getLayoutParams().width - buttonOffset);

        RelativeLayout.LayoutParams params = (LayoutParams) leftControlBar.getLayoutParams();
        params.width = getLayoutParams().width - buttonOffset;

        leftControlBar.requestLayout();
        nameBar.requestLayout();

        // show/collapse the left side of the control bar.
        if (showNameBar) {
            leftControlBar.setVisibility(View.VISIBLE);
        } else {
            leftControlBar.setVisibility(View.GONE);
        }
    }

    /**
     * Sets the layout mode, which will produce an appropriate layout for the size of this screen.
     * @param mode
     */
    private void setLayoutMode(LayoutMode mode) {
        RelativeLayout.LayoutParams params = (LayoutParams) getLayoutParams();

        switch (mode) {
            case Large:
                params.width = dpToPx(500);
                break;
            case Medium:
                params.width = dpToPx(320);
                break;
            case Small:
                params.width = dpToPx(150);
                break;
            case Minimal:
                showNameBar = false;
                params.width = LayoutParams.WRAP_CONTENT;
                break;
            default:
                Log.wtf("control-bar", "Unknown layout mode " + mode);
                break;
        }

        //show/collapse the left side of the control bar.
        if (showNameBar) {
            leftControlBar.setVisibility(View.VISIBLE);
        } else {
            leftControlBar.setVisibility(View.GONE);
        }
        setLayoutParams(params);
    }

    /**
     * Converts dp to real pixels, according to the screen density.
     * @param dp A number of density-independent pixels.
     * @return The equivalent number of real pixels.
     */
    private int dpToPx(int dp) {
        double screenDensity = getContext().getResources().getDisplayMetrics().density;
        return (int) (screenDensity * (double) dp);
    }

    /**
     * Inversion function of {@link #dpToPx(int)}.
     * @param px A number of real pixels.
     * @return The equivalent number of density-independent pixels.
     */
    private int pxToDp(int px) {
        double density = context.getResources().getDisplayMetrics().density;
        return (int) ((double) px / density);
    }

}