package xyz.alexcrea.jacn.sdk;

/**
 * Current state of the Neuro sdk
 */
public enum NeuroSDKState {

    /**
     * The websocket is connecting. Actions can't be registered
     */
    CONNECTING,
    /**
     * The websocket is connected. Action can be registered
     */
    CONNECTED,
    /**
     * The websocket got closed. Actions can't be registered
     */
    CLOSED,
    /**
     * The websocket encountered an unrecoverable error. Actions can't be registered
     */
    ERROR,

}
