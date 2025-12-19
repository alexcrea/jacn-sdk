package xyz.alexcrea.jacn.sdk;

public enum ForceActionPriority {

    /**
     * Default priority
     * <br>
     * Will cause neuro to wait het to finish speaking before responding to the request
     */
    LOW("low"),
    /**
     * Will cause neuro to finish her speak sooner to respond to the request quicker
     */
    MEDIUM("medium"),
    /**
     * Will cause neuro to finish her speak quickly to respond to the request fast
     */
    HIGH("high"),
    /**
     * Will cause neuro to interrupt her speak and process the action
     * <br>
     * Note that this priority should rarely be used and with caution as it WILL cause annoying interruption
     */
    CRITICAL("critical"),
    ;

    public static final ForceActionPriority DEFAULT = ForceActionPriority.LOW;

    private final String value;


    ForceActionPriority(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
