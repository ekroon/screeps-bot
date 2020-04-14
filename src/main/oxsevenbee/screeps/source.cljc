(ns oxsevenbee.screeps.source
  (:require [goog.object :as go]
            [cljs-bean.core :refer [->js ->clj]]
            [oxsevenbee.screepsbot.compile :refer [is-repl-mode-enabled?]]))

(when (is-repl-mode-enabled?)
  (when (or (not js/global.Source)
            (and js/global.Source (.-dummy js/global.Source)))
    (println "WARN: overriding js/Source")
    (deftype Source [-dummy])
    (set! Source.dummy true)
    (set! js/global.Source Source)))

(defprotocol SourceProtocol
  (-id [source]))

(extend-type js/Source
  SourceProtocol
  (-id [source] (.-id source)))

(defn id [source]
  (-id source))