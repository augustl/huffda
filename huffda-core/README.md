# huffda-core

Main server.

## Development

### Requirements

* A JVM (currently we use the JVM based Clojure stack to compile)
* Node.JS (version 6 or newer, probably)
* Leiningen (the Clojure build tool)

### Start server

`lein with-profile server-dev figwheel main-server-dev`

Run server

`node out/main-server-dev/main-server-dev-with-figwheel.js`

### Start client

`lein with-profile client-dev figwheel main-client-dev`

### Running tests

`lein doo node test auto`

or

`lein doo node test once`

## License

Copyright © 2017 August Lilleaas