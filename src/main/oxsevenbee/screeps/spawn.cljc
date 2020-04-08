(ns oxsevenbee.screeps.spawn
  (:require [goog.object :as go]))

(defprotocol SpawnProtocol
  (-spawn-creep [spawn creep-name body]))

(extend-type js/StructureSpawn
  SpawnProtocol
  (-spawn-creep [^js/StructureSpawn spawn creep-name body]
    (.spawnCreep spawn (clj->js body) creep-name )))

(defn spawn-creep [spawn creep-name body]
  (-spawn-creep spawn creep-name body))