(ns oxsevenbee.screeps.room
  (:refer-clojure :exclude [-name name])
  (:require [oxsevenbee.screeps.source]
            [oxsevenbee.screeps.constants :as c]
            [oxsevenbee.screepsbot.compile :refer [is-repl-mode-enabled?]]
            [goog.object :as go]
            [cljs-bean.core :refer [->js ->clj]]))

(when (is-repl-mode-enabled?)
  (when (or (not js/global.Room)
            (and js/global.Room (.-dummy js/global.Room)))
    (println "WARN: overriding js/Room")
    (deftype Room [-dummy])
    (set! Room.dummy true)
    (set! js/global.Room Room)))

(defprotocol RoomProtocol
  (-room-area [room])
  (-name [room])
  (-find-in-room [room type]))

(extend-type js/Room
  RoomProtocol
  (-room-area [^js/Room room]
    (->clj (.lookAtArea room 0 0 49 49 true)))
  (-name [^js/Room room]
    (.-name room))
  (-find-in-room [^js/Room room type]
    (.find room type)))

(defn room-area [room] (-room-area room))
(defn name [room] (-name room))
(defn find-in-room [room] (-find-in-room room type))

(defn sources [^js/Room room]
  (-find-in-room room c/find-sources))

