# Pani

***Deprecation Warning:  A fork of this project is under active development here: https://github.com/crisptrutski/matchbox .The future of pani itself is undecided at this time.***


A convenience library to access Firebase from Clojurescript.

[![Build Status](https://travis-ci.org/verma/pani.svg)](https://travis-ci.org/verma/pani)

The goal of this library is not to expose all functionality offered by Firebase, but to provide opinionated convenient.

# Current version

The library is in its formative years.  The current version is `0.0.3`.

[![Clojars Project](http://clojars.org/pani/latest-version.svg)](http://clojars.org/pani)

## NOTE RE: JVM USAGE

The API available in regular Clojure has fallen considerably behind, and not all the examples below will be runnable.

Rather than maintaining it separately going forward, in version `0.0.5` the `pani.core` namespace will be portable, and `pani.clojure.core` will be removed.

# Features

Pani offers several benefits over raw JS interop:

 * Idiomatic constructs
 * Async channels or callbacks for change notifications
 * Javascript objects abstraction

This library, for now, is entirely based on how I intend to use this library (may be with Om etc.) and would grow as I discover more things I'd like it to do.  Pull requests welcome!

# Usage

Require `pani`:

    (:require [pani.core :as p])          ; for clojurescript
    (:require [pani.async :as pa])        ; for clojurescript
    (:require [pani.clojure.core :as p])  ; for clojure, until v0.0.5

Create a root object:

	(def r (p/connect "https://your-app.firebaseio.com/"))

Bind a callback to recieve callback notifications when a `value` notification occurs:

    (p/listen-to r :ago :value #(log %1))

The `listen-to` call accepts either a key or a seq of keys (`get-in` style):

	(p/listen-to r [:info :world] :value #(log %1))

You can also listen to other Firebase notification events, e.g. the `child-added` notification:

	(p/listen-to r :messages :child-added #(log %1))

If no callback is specified, the `listen-to` call returns an async channel:

    (let [c (p/listen-to r :messages :child_added)]
      (go-loop [msg (<! c)]
        (.log js/console "New message (go-loop):" (:message msg))
        (recur (<! c))))

Use the `reset!` call to set a value, like `bind` this function accepts either a single key or a seq of keys:

	(p/reset-in! r [:info :world] "welcome")
	(p/reset-in! r :age 100)

Use the `conj!` function to push values into a collection:

	(p/conj! r {:message "hello"})

Finally, use the `get-in` function to get a new child node:

	(def messages-root (p/get-in r :messages))
	(p/listen-to messages-root :child-added #(log %1))

## Clojurescript Examples
***Note that***, most examples will require you to add your Firebase app url to the example.  You'd most likely have to edit a line like the following in one of the source files (most likely `core.cljs`):

	;; TODO: Set this to a firebase app URL
	(def firebase-app-url "https://your-app.firebaseio.com/")


All examples are available under the `examples` directory.  To run a Clojurescript example just run the respective `lein` command to build it:

    lein cljsbuild once <example-name>

This should build and place a `main.js` file along with an `out` directory in the example's directory.  You should now be able to go to the example's directory and open the `index.html` file in a web-browser.

## License

Copyright © 2014 Uday Verma

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
