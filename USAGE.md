## Table of Contents
- [Common code](#Common)
  - [Create the SDK instance](#Create-the-SDK-instance)
  - [Create and register Actions](#Create-and-register-Actions)
  - [About Force Actions](#About-forced-Actions)
  - [Sending Context](#Sending-Context)
- [Using callback](#Common)
- [Using listeners](#Listeners)

## Common

There is 2 way to handle actions and sdk event with this SDK:
- use callbacks 
- or listeners. 

use what you prefer.

If you need more detail about a class of a function, please check javadoc. \
If you think this document can be improved, fell free to pr ! (and same thing apply to the javadoc)

### Create the SDK instance
You first need to build a NeuroSDK using the NeuroSDKBuilder
```java
NeuroSDKBuilder builder = sdk = new NeuroSDKBuilder("Your game name, with space & accent if any");
// Potentially do other thing with the builder
NeuroSDK sdk = builder.build();
```
Building the NeuroSDK will cause it to try to connect to a websocket either:
- The provided address and port
- The url provided at the "NEURO_SDK_WS_URL" environment variable (not yet implemented)
- The default address (localhost) and port (8000)

Building the NeuroSDK is not blocking. The SDK will NOT be open after build is called.
You can get the current state of the SDK using `sdk.getState()`

Using most functions of the SDK function on a will return false if it is considered as a failure to execute it. 
see function's javadoc to see detail.

You can set parameters of this builder:
- `addActionsOnConnect` allow to add action that will get registered just after startup command is sent
- `setPort` force the port of to connect
- `setAddress` force the address to connect


### Create and register Actions
Simply call one of the constructor of Action. They should look like \
`new Action(name, description, schema (optional), onResult (optional))`

**name**: The name of the action.
> [!NOTE]
> Only one action of the same name can be registered at the same time. \
> Current behavior it to just ignore registered action if already present. \
> It may change later and will probably follow the official SDK
> (Override previously registered action to the new one) \
> when the Neuro API side will change its behavior to that. \
> So please do not relly on current behavior for your game.

**description**: A description of the action. This will be sent to Neuro so be cautious.

**schema**: A *simple* JSON schema to send to Neuro. \
This schema will get validated by the SDK before executing the action.

**onResult**: called callback when \
See the [callback section](#Callback)
(currently not written)

To register an action. either add it one startup with `addActionsOnConnect` or use
```java
sdk.registerActions(list of actions);
```
and can unregister actions via
```java
sdk.unregisterActions(list of actions);
```
Only registered action can be user by Neuro and by force action.

### About forced Actions
Forced action is a way to force neuro to choose one of the provided actions.

> [!WARNING]  
> You can only use 1 forced action at the time. \
> please wait for the previous one to finish before another one is triggered

A force action can simply be done by calling
```java
sdk.forceActions(state (optional), query, ephemeral (default false), actions);
```
With the following parameters:

**state**: A optional, but recommended, representation of the state of the game. \
It can be any format (plaintext, JSON, XML, YAML, Markdown, etc...)

**query**: A plaintext message that tells Neuro what she is currently supposed to be doing

**ephemeral**: If the context provided in state and query should be remembered after this action

**actions**: The list to force Neuro to choose one of them.

### Sending Context
You can send context to Neuro when something on your game happen with
```java
sdk.sendContext("message", silent (bool));
```

with the message field being the message Neuro will directly receive. \
and silent: If neuro should not be prompted to react to this message \
(if false, she also may not speak about the sent context if she's already busy)

## Callback
[Example of using Callback](./src/main/java/xyz/alexcrea/jacn/example/callback/TicTacToeExample1.java) \
(not recommended currently as it need a rewrite)

TODO

## Listeners
[Example of using Listener](./src/main/java/xyz/alexcrea/jacn/example/callback/TicTacToeExample1.java) \
(currently cleaner than the callback example)

TODO