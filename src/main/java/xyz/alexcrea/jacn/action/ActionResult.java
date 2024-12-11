package xyz.alexcrea.jacn.action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An action result is information sent back to Neuro to inform the result of the action
 *
 * @param id      the action id from the action request
 * @param success whether the action was successful.
 *                If this is false and this action if part of an action force,
 *                the whole actions force will be immediately retried by Neuro.
 * @param message a plaintext message that describe what happened when the action was executed.
 *                if not successful, this can either be empty,
 *                or provide a small context to Neuro regarding the action she just took
 *                (e.g. "Remember to not share this with anyone."). This information will be directly received by Neuro.
 */
@SuppressWarnings({"unused"})
public record ActionResult(
        @NotNull String id,
        boolean success,
        @Nullable String message
) {

    /**
     * An action result is information sent back to Neuro to inform the result of the action
     *
     * @param request the request origin of this result
     * @param success whether the action was successful.
     *                If this is false and this action if part of an action force,
     *                the whole actions force will be immediately retried by Neuro.
     * @param message a plaintext message that describe what happened when the action was executed.
     *                if not successful, this can either be empty,
     *                or provide a small context to Neuro regarding the action she just took
     *                (e.g. "Remember to not share this with anyone."). This information will be directly received by Neuro.
     */
    public ActionResult(@NotNull ActionRequest request, boolean success, @Nullable String message) {
        this(request.id(), success, message);
    }

    /**
     * An action result is information sent back to Neuro to inform the result of the action
     * This method constructor not give any message back to Neuro.
     *
     * @param id the action id from the action request
     * @param success whether the action was successful.
     *                If this is false and this action if part of an action force,
     *                the whole actions force will be immediately retried by Neuro.
     */
    public ActionResult(@NotNull String id, boolean success) {
        this(id, success, null);
    }

    /**
     * An action result is information sent back to Neuro to inform the result of the action
     * This method constructor not give any message back to Neuro.
     *
     * @param request the request origin of this result
     * @param success whether the action was successful.
     *                If this is false and this action if part of an action force,
     *                the whole actions force will be immediately retried by Neuro.
     */
    public ActionResult(@NotNull ActionRequest request, boolean success) {
        this(request.id(), success, null);
    }

}
