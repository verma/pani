# Pani

***Deprecation Warning:  A fork of this project is under active development here: https://github.com/crisptrutski/sunog .The future of pani itself is undecided at this time.***


A convenience library to access Firebase from Clojurescript.

[![Build Status](https://travis-ci.org/verma/pani.svg)](https://travis-ci.org/verma/pani)

The goal of this library is not to provide access to the entire functionality offered by Firebase, but to make it convenient to use Firebase as a data store from within Clojurescript.

# Current version

The library is in its infancy.  The current version is `0.0.3`.

[![Clojars Project](http://clojars.org/pani/latest-version.svg)](http://clojars.org/pani)


# Features

Pani offers several benefits over raw JS interop:

 * Idiomatic constructs
 * Async channels or callbacks for change notifications
 * Javascript objects abstraction

This library, for now, is entirely based on how I intend to use this library (may be with Om etc.) and would grow as I discover more things I'd like it to do.  Pull requests welcome!

# Usage

Require `pani`:

    (:require [pani.cljs.core :as p])       ; for clojurescript
    (:require [pani.clojure.core :as p])    ; for clojure

Create a root object:

	(def r (p/root "https://your-app.firebaseio.com/"))

Bind a callback to recieve callback notifications when a `value` notification occurs:

    (p/bind r :value :age #(log %1))

The `bind` call accepts either a key or a seq of keys (`get-in` style):

	(p/bind r :value [:info :world] #(log %1))

You can also bind to other Firebase notification events, e.g. the `child_added` notification:

	(p/bind r :child_added :messages #(log %1))

If no callback is specified, the `bind` call returns an async channel:

    (let [c (p/bind r :child_added :messages)]
      (go-loop [msg (<! c)]
        (.log js/console "New message (go-loop):" (:message msg))
        (recur (<! c))))

Use the `set!` call to set a value, like `bind` this function accepts either a single key or a seq of keys:

	(p/set! r [:info :world] "welcome")
	(p/set! r :age 100)

Use the `push!` function to push values into a collection:

	(p/push! r :messages {:message "hello"})

Finally, use the `walk-root` function to get a new child node:

	(def messages-root (p/walk-root r :messages))
	(p/bind messages-root :child_added [] #(log %1))

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
