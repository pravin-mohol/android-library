/* Copyright 2016 Urban Airship and Contributors */

package com.urbanairship.actions;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.urbanairship.UAirship;

/**
 * An action that adds text to the clipboard.
 * <p/>
 * Accepted situations: SITUATION_PUSH_OPENED, SITUATION_WEB_VIEW_INVOCATION,
 * SITUATION_MANUAL_INVOCATION, SITUATION_BACKGROUND_NOTIFICATION_ACTION_BUTTON,
 * and SITUATION_FOREGROUND_NOTIFICATION_ACTION_BUTTON.
 * <p/>
 * Accepted argument value - A string with the clipboard text or a map with:
 * <ul>
 * <li>{@link #LABEL_KEY}: String, Optional</li>
 * <li>{@link #TEXT_KEY}: String, Required</li>
 * </ul>
 * <p/>
 * Result value: The arguments value.
 * <p/>
 * Default Registration Names: ^c, clipboard_action
 */
public class ClipboardAction extends Action {

    /**
     * Default registry name
     */
    public static final String DEFAULT_REGISTRY_NAME = "clipboard_action";

    /**
     * Default registry short name
     */
    public static final String DEFAULT_REGISTRY_SHORT_NAME = "^c";

    /**
     * Key to define the ClipData's label when providing the action's value as a map.
     */
    public static final String LABEL_KEY = "label";

    /**
     * Key to define the ClipData's text when providing the action's value as a map.
     */
    public static final String TEXT_KEY = "text";

    @Override
    public boolean acceptsArguments(@NonNull ActionArguments arguments) {
        switch (arguments.getSituation()) {
            case Action.SITUATION_BACKGROUND_NOTIFICATION_ACTION_BUTTON:
            case Action.SITUATION_FOREGROUND_NOTIFICATION_ACTION_BUTTON:
            case Action.SITUATION_PUSH_OPENED:
            case Action.SITUATION_MANUAL_INVOCATION:
            case Action.SITUATION_WEB_VIEW_INVOCATION:

                if (arguments.getValue().getMap() != null) {
                    return arguments.getValue().getMap().get("text").isString();
                }

                return arguments.getValue().getString() != null;
            case Action.SITUATION_PUSH_RECEIVED:
            default:
                return false;
        }
    }

    @Override
    public ActionResult perform(@NonNull final ActionArguments arguments) {

        // Get the text and label
        final String text, label;
        if (arguments.getValue().getMap() != null) {
            text = arguments.getValue().getMap().get(TEXT_KEY).getString();
            label = arguments.getValue().getMap().get(LABEL_KEY).getString();
        } else {
            text = arguments.getValue().getString();
            label = null;
        }

        // Clipboard must be accessed from a thread with a prepared looper
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= 11) {
                    ClipboardManager clipboardManager = (ClipboardManager) UAirship.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(label, text);
                    clipboardManager.setPrimaryClip(clip);
                } else {
                    android.text.ClipboardManager clipboardManager = (android.text.ClipboardManager) UAirship.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboardManager.setText(text);
                }
            }
        });

        // Return the text we are setting
        return ActionResult.newResult(arguments.getValue());
    }

}
