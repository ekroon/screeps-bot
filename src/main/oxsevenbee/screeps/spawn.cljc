(ns oxsevenbee.screeps.spawn
  (:require [oxsevenbee.utils :refer [lift-on lift-as]]
            [goog.object :as go]))

(defn -spawn-creep [^js/StructSpawn spawn creep-name body]
  (.spawnCreep spawn (clj->js body) (name creep-name)))

(lift-as SpawnProtocol)

;
;(defn ->SpawnProtocol [o]
;  (lift-on SpawnProtocol o))