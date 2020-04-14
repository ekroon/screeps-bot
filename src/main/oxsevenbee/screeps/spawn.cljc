(ns oxsevenbee.screeps.spawn
  (:require [goog.object :as go]
            [oxsevenbee.screepsbot.compile :refer [is-repl-mode-enabled?]]))

(when (is-repl-mode-enabled?)
  (when (or (not js/global.StructureSpawn)
            (and js/global.StructureSpawn (.-dummy js/global.StructureSpawn)))
    (println "WARN: overriding js/StructureSpawn")
    (deftype StructureSpawn [-dummy])
    (set! StructureSpawn.dummy true)
    (set! js/global.StructureSpawn StructureSpawn)))

(defprotocol SpawnProtocol
  (-spawn-creep [spawn creep-name body]))

(extend-type js/StructureSpawn
  SpawnProtocol
  (-spawn-creep [^js/StructureSpawn spawn creep-name body]
    (.spawnCreep spawn (clj->js body) creep-name )))

(defn spawn-creep [spawn creep-name body]
  (-spawn-creep spawn creep-name body))