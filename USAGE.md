## Table of Contents

- [Common code](#Common)
  - [Create the SDK instance](#Create-the-SDK-instance)
  - [Create and register Actions](#Create-and-register-Actions)
  - [About Option Map Action](#About-Option-Map-Action)
  - [About Force Actions](#About-Force-Actions)
  - [Sending Context](#Sending-Context)
- [Using callback](#Common)
- [Using listeners](#Listeners)

## Common

There is two ways to handle actions and SDK events with this SDK:

- Use callbacks
- Or listeners.

Use what you prefer.

If you need more details about a class or a function, please check the javadoc.
If you think this document can be improved, feel free to submit a PR! The same applies to the javadoc.

Note: Callback and listener functions will be called from the websocket thread.
Take caution when performing actions from it.

### Create the SDK instance

You first need to build a NeuroSDK using the NeuroSDKBuilder:

```java
NeuroSDKBuilder builder = new NeuroSDKBuilder("Your game name, with space & accent if any");
// Potentially do other things with the builder
NeuroSDK sdk = builder.build();
```

Building the NeuroSDK will cause it to try to connect to a websocket using either:

- The provided address and port
- The URL provided in the "NEURO_SDK_WS_URL" environment variable
- The default address (localhost) and port (8000)

Building the NeuroSDK is not blocking. The SDK will NOT be open after the build method is called.
You can get the current state of the SDK using `sdk.getState()`.

Using most functions of the SDK methods will return false if it is considered a failure to execute.
See the function's javadoc for details.

You can set parameters of this builder:

- `addActionsOnConnect`: Allows to add Actions that will be registered just after the startup command is sent.
- `setPort`: Force the port to connect to.
- `setAddress`: Force the address to connect to.

### Create and register Actions

Simply call one of the constructors of Action. They should look like: \
`new Action(name, description, schema (optional), onResult (optional))`.

**name**: The name of the action.
> [!NOTE]
> Only one action with the same name can be registered at the same time.
> The current behavior is to ignore the registered action if it is already present.
> This may change later and will probably follow the official SDK
> (Override previously registered action with the new one)
> when the Neuro API changes its behavior.
> So please do not rely on the current behavior for your game.

**description**: A description of the action. This will be sent to Neuro, so be cautious.

**schema**: A *simple* JSON schema to send to Neuro.
This schema will get validated by the SDK before executing the action.

**onResult**: Called callback when an Action request is sent\
See the [callback section](#Callback).

To register an action, either add it on startup with `addActionsOnConnect` or use:

```java
sdk.registerActions(list of actions);
```

And you can unregister actions via:

```java
sdk.unregisterActions(list of actions);
```

Only registered actions can be used by Neuro and on force action.

### About Option Map Action

Option map action allows Neuro to select one of the provided options.
You can provide a generic class and get the option result as an object of this class.

For example, the following snippet from the Listener example is used to create an option action "play"
and add every TicTacToeLocation from the list possibleLocations.
Every option needs to have a distinct name. Here, the option name is provided by loc.actionName().
The option name is how Neuro can distinguish between options. 
Please make it obvious what the options are meant to do.

```java
OptionMapAction<TicTacToeLocation> playAction = new OptionMapAction<>(
        "play",
        "Play a position in the tic tac toe grid.");
for (TicTacToeLocation loc : possibleLocations) {
    playAction.setOption(loc.actionName(), loc);
}
```

You can then get the result with an action request. This snippet is also from the second example.

```java
if(!request.from().getName().contentEquals("play")) return null;
// We know we created an option action of the generic type TicTacToeLocation when we registered the "play" action
// So it is okay to cast like this.
OptionMapAction<TicTacToeLocation> action = (OptionMapAction<TicTacToeLocation>) request.from();

// Fetch location from data of request
TicTacToeLocation location = action.get(request);
```

With "request" being the ActionRequest.

### About Force Actions

A force action is a way to force Neuro to choose one of the provided actions.

> [!WARNING]
> You can only use one forced action at a time.
> Please wait for the previous one to finish before triggering another one.

A force action can simply be done by calling:

```java
sdk.forceActions(state (optional), query, ephemeral (default false), actions);
```

With the following parameters:

**state**: An optional, but recommended, representation of the state of the game. \
It can be in any format as it will be interpreted by Neuro (plaintext, JSON, XML, YAML, Markdown, etc...).

**query**: A plaintext message that tells Neuro what she's currently supposed to be doing.

**ephemeral**: Whether the context provided in state and query should be remembered after this action.

**actions**: The list of actions to force Neuro to choose from.

### Sending Context

You can send context to Neuro when something happens in your game with:

```java
sdk.sendContext(message, silent (bool));
```

With the message field being the message Neuro will directly receive. \
And silent: If Neuro should not be prompted to react to this message. \
(If false, Neuro also may not speak about the sent context if she's already busy).

## Callback

[Example of using Callback](./src/main/java/xyz/alexcrea/jacn/example/callback/TicTacToeExample1.java)
(Not recommended currently as it needs a rewrite)

You can set websocket events callback via setters on the Neuro SDK builder, namely:

- **onConnect**: Called when the websocket gets connected. Should not fail but check the status to be sure.
- **onClose**: Called when the websocket gets closed. Will not trigger if it did not get opened first.
- **onError**: Happens when there is an error running or connecting to the websocket.
  The provided exception should be an instance of class ConnectException if it could not connect to the websocket.

These callbacks have default values, mostly logging what happened.
So don't forget to log things yourself if you set them.

You can also set things on an action:

- One function "onResult" (via `Action.setOnResult` or as the last constructor parameter).
  This function is used when Neuro (or Randy) executes this action and the SDK needs an ActionResult to respond to it.
- Also, one callback for after the result (via `Action.setAfterResult`).
  It is used to process anything you like after the result is sent.

> [!NOTE]
> Your onResult functions should return as fast as possible as Neuro would freeze for the time it executes. \
> AfterResult callback is not constrained by this.

By default, onResult and afterResult are null.

If onResult returns null, the action will be processed by listeners if any.
If the callback and every listener is or returns null, then the action is considered failed.

> [!NOTE]
> AfterResult callback will be executed only if the result was provided by the onResult function.

## Listeners

[Example of using Listener](./src/main/java/xyz/alexcrea/jacn/example/callback/TicTacToeExample1.java)
(Currently cleaner than the callback example)

You can create an SDK listener by extending your class with AbstractSDKListener.
You can also directly implement an instance of NeuroSDKListener if you like,
but the AbstractSDKListener also allows you to access some SDK methods directly
and has less generic methods.

You can then add your listener by calling:

```java
YourListenerClass listener = new YourListenerClass();
sdkBuilder.addListener(listener);
```

On your SDK builder, with listener being an instance of your listener class.

> [!TIP]
> If you like to split your code, unlike callbacks, you can add multiple listeners.

In your listener class, you can implement multiple methods from AbstractSDKListener.
Some to handle the SDK state:

- `onConnectSuccess`: Called when a connection handshake is successfully done with a websocket.
- `onConnectFailed`: Called when a connection handshake is unsuccessfully done with a websocket.
- `onConnectError`: Called when we could not connect to the websocket.
- `onClose`: Called when the websocket gets closed for any reason.
- `onError`: Called when a websocket exception happens. Will also be called before onConnectError.

And some methods to handle actions:

- `onActionRequest`: Called when Neuro requests an action.
- `onAfterResult`: Called only after onActionRequest of this listener returns a non-null result.

> [!NOTE]
> Your onActionRequest method should return as fast as possible as Neuro would freeze for the time it executes. \
> OnAfterResult method is not constrained by this.
>
> Only one listener or callback can return a non-null result.
> After a non-null result, no more listener "onActionRequest" methods will be called.
> The action's callback will be called before every listener.
> The order of calls of listeners is not guaranteed.

The default implementation of onActionRequest returns null.
So onAfterResult will never be called if you do not also implement onActionRequest.

It is recommended you read the javadoc of the functions you are implementing.