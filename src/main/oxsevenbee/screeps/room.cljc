(ns oxsevenbee.screeps.room
  (:require [oxsevenbee.utils :refer [lifted lift-on lift-as]]
            [goog.object :as go]
            [cljs-bean.core :refer [->js ->clj]]))

(defn -room-area [^js/Room room]
  (->clj (.lookAtArea room 0 0 49 49 true)))

(lift-as RoomProtocol)