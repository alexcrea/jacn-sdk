package xyz.alexcrea.jacn.sdk.proposed;

import org.jetbrains.annotations.ApiStatus;

/**
 * Flag for proposed feature
 * They may be implemented later.
 * as per that reason, some of them may not have an accurate implementation.
 * see <a href="https://github.com/VedalAI/neuro-game-sdk/blob/main/API/PROPOSALS.md">PROPOSALS.md</a>
 * <p>
 * Note: these features may come into the official API. When they do, the feature will be enabled by default and the feature flag deprecated for removal.
 * If a feature is decided to never be implemented. then it will be deprecated for removal
 * A proposed feature marked as for removal can and will be removed at any time.
 */
@ApiStatus.Experimental
public enum ProposedFeature {

    /**
     * re register action after neuro crash/restart
     * Note: this comportment is currently implemented in Randy but not in Neuro.
     */
    RE_REGISTER_ALL,

    /**
     * handle graceful and immediate shutdown
     * TODO, currently not implemented
     */
    SHUTDOWN,

}
