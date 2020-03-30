(ns oxsevenbee.screeps.game
  (:require [integrant.core :as ig]
            [goog.object :as go]
            [oxsevenbee.screeps.spawn :refer [make-SpawnProtocol]]
            [oxsevenbee.utils :refer [ns-info lift-on lift-as]]))

(defn ^js/StructureSpawn -get-spawn [_ spawn-name]
  (make-SpawnProtocol (go/get (.. js/Game -spawns) spawn-name)))

(defn ^js/Creep -get-creep [creep-name]
  (go/get (.. js/Game -creeps) creep-name))

(lift-as GameProtocol)

(defmethod ig/init-key ::game [_ _]
  (lift-on GameProtocol js/Game))