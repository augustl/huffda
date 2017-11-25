# huffda-core

Main server.

## Development

### Requirements

* A JVM (currently we use the JVM based Clojure stack to compile)
* Node.JS (version 6 or newer, probably)
* Leiningen (the Clojure build tool)

### Compiling

`lein cljsbuild main auto`

or

`lein cljsbuild main once`

### Running

`node out/main.js`

### Running tests

`lein doo node test auto`

or

`lein doo node test once`

## License

Copyright © 2017 August Lilleaas