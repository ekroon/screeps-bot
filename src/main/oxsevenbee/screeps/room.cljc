(ns oxsevenbee.screeps.room
  (:refer-clojure :exclude [-name name])
  (:require [oxsevenbee.screeps.source]
            [oxsevenbee.screeps.constants :as c]
            [goog.object :as go]
            [cljs-bean.core :refer [->js ->clj]]))

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