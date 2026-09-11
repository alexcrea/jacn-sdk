package xyz.alexcrea.jacn.sdk;

import org.jetbrains.annotations.NotNullByDefault;

/***
 * Represent the character currently playing
 * <p>
 * By default, fields are set to Unknown until startup is received see {}
 */
@NotNullByDefault
public class Character {
    public enum Type {
        /**
         * Represent neuro playing
         */
        NEURO,
        /**
         * Represent evil playing
         */
        EVIL,
        /**
         * Represent someone else playing
         */
        OTHER
    }

    public static final Character DEFAULT = new Character("Unknown", "Unknown", "Unknown");

    /**
     * The current neuro sdk session id
     * <p>
     * default to "Unknown" when uninitialized
     */
    public final String sessionID;
    /**
     * The character ID.
     * "neuro" for neuro, "evil" for evil
     * <p>
     * You can also use {@link type}
     * <p>
     * default to "Unknown" when uninitialized
     */
    public final String characterID;
    /**
     * The current character type.
     * <p>
     * default to {@link Type#OTHER OTHER} when uninitialized
     */
    public final Type type;
    /**
     * The current character display name.
     * <p>
     * default to "Unknown" when uninitialized
     */
    public final String displayName;

    public Character(String sessionID, String characterID, String displayName) {
        this.sessionID = sessionID;
        this.characterID = characterID;
        this.displayName = displayName;

        this.type = switch(characterID) {
            case "neuro" -> Type.NEURO;
            case "evil" -> Type.EVIL;
            default -> Type.OTHER;
        };
    }

    @Override
    public String toString() {
        return "Character{" + sessionID + ", " + characterID + ", " + type + ", " + displayName + "}";
    }
}
