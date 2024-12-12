# Java alexcrea's Neuro SDK
This Neuro SDK implementation is a, bad, unofficial,
implementation of the [Neuro SDK API](https://github.com/VedalAI/neuro-game-sdk/)

## Using the SDK
You need to install the SDK, then follow the [Usage Guide](./USAGE.md)

## Installing the SDK
see [the installation guide](./INSTALL.md)

## List of things to do before this being usable:
- [X] Make JSON schema
- [X] Implement a better way to handle things
- [X] Create example how to use the better way
- [ ] docs of how to use the better way

### And some detail
- [ ] move example to a module excluded from gradle publish
- [ ] rethink default sdk event callbacks

## Need help or suggestion ?
You can either create an issue/pr/discussion here or join [my discord](https://discord.gg/swBrMf327a)

Please note, since I plan to participate myself in the incoming neuro game jam I will not be actively 
available for help in that period.

---

### Credit
This implementation use [com.networknt:json-schema-validator](https://github.com/networknt/json-schema-validator)
for JSON schema validation
and [org.java-websocket:Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket) for the websocket
