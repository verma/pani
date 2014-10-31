(ns pani.cljs.core
  (:refer-clojure :exclude [name get-in merge])
  (:require [cljs.core.async :refer [<! >! chan put! merge]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defn- clj-val [v]
  (js->clj (.val v) :keywordize-keys true))

;; Make a firebase object ouf the given URL
;;
(defn root [url]
  "Makes a root reference for firebase"
  (js/Firebase. url))

;; A utility function to traverse through korks and get ref to a child object
;; [:hello :world :bye ] refers to hello.world.bye
;;
(defn walk-root [root korks]
  "Takes korks and reduces it to a root on which we can perform direct actions"
  (let [p (if (sequential? korks)
            (apply str (interpose "/" (map clojure.core/name korks)))
            (if korks (clojure.core/name korks)))]
    (if (empty? p)
      root
      (.child root p))))

(defn name [r]
  "Get the name of the given root"
  (.name r))

(defn parent [r]
  "Get the parent of the given root"
  (let [p (.parent r)]
    (if (nil? (js->clj p))
      nil
      p)))

(defn- fb-call!
  "Set the value at the given root"
  ([push-fn root val]
   (let [as-js (clj->js val)]
     (push-fn root as-js)))

  ([push-fn root korks val]
   (fb-call! push-fn (walk-root root korks) val)))

(defn get-in
  "get-in style single shot get function, returns a channel which delivers the value"
  [root ks]
  (let [c (chan)]
    (.once root "value" #(let [v (-> (.val %)
                                     (js->clj :keywordize-keys true)
                                     (clojure.core/get-in (if (sequential? ks) ks [ks])))]
                           (put! c v)))
    c))

;; A function set the value on a root [korks]
;;
(defn set!
  "Set the value at the given root"
  ([root val]
   (fb-call! #(.set %1 %2) root val))

  ([root korks val]
   (fb-call! #(.set %1 %2) root korks val)))

(defn push!
  "Set the value at the given root"
  ([root val]
   (fb-call! #(.push %1 %2) root val))

  ([root korks val]
   (fb-call! #(.push %1 %2) root korks val)))

(defn bind
  "Bind to a certain property under the given root"
  ([root type korks]
   (let [bind-chan (chan)]
     (bind root type korks #(go (>! bind-chan %)))
     bind-chan))

  ([root type korks cb]
   (let [c (walk-root root korks)]
     (.on c (clojure.core/name type)
          #(when-let [v (clj-val %1)]
             (cb {:val v, :name (name %1)}))))))

(defn transact!
  "Use the firebase transaction mechanism to update a value atomically"
  [root korks f & args]
  (let [c (walk-root root korks)]
    (.transaction c #(apply f % args) #() false)))

(defn- fb->chan
  "Given a firebase ref, an event and a transducer, binds and posts to returned channel"
  [fbref event td]
  (let [c (chan 1 td)]
    (.on fbref (clojure.core/name event)
         #(put! c [event %]))
    c))

(defn listen<
  "Listens for events on the given firebase ref"
  [root korks]
  (let [root    (walk-root root korks)
        events  [:child_added :child_removed :child_changed]
        td      (map (fn [[evt snap]]
                       [evt (.name snap) (.val snap)]))
        chans   (map (fn [event]
                       (fb->chan root event td)) events)]
    (merge chans)))
