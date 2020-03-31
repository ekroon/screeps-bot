(ns oxsevenbee.screeps.room
  (:refer-clojure :exclude [-name name])
  (:require [oxsevenbee.utils :refer [lifted lift-on lift-as]]
            [oxsevenbee.screeps.source :refer [make-SourceProtocol]]
            [goog.object :as go]
            [cljs-bean.core :refer [->js ->clj]]))

(defn -room-area [^js/Room room]
  (->clj (.lookAtArea room 0 0 49 49 true)))

(defn -name [^js/Room room]
  (.-name room))

(defn -find-in-room [^js/Room room type]
  (.find room type))

(defn -sources [^js/Room room]
  (map make-SourceProtocol (-find-in-room room js/FIND_SOURCES)))

(lift-as RoomProtocol)