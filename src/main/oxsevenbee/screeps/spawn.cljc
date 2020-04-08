(ns oxsevenbee.screeps.spawn
  (:require [oxsevenbee.utils :refer [lifted lift-on lift-as]]
            [goog.object :as go]))

(defn -spawn-creep [spawn creep-name body]
  (.spawnCreep ^js spawn (clj->js body) (name creep-name)))

(lift-as SpawnProtocol)

;
;(defn ->SpawnProtocol [o]
;  (lift-on SpawnProtocol o))