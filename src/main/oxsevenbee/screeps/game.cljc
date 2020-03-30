(ns oxsevenbee.screeps.game
  (:require [integrant.core :as ig]
            [goog.object :as go]
            [oxsevenbee.screeps.spawn :refer [make-SpawnProtocol]]
            [oxsevenbee.screeps.room :refer [make-RoomProtocol]]
            [oxsevenbee.utils :refer [ns-info lift-on lift-as]]))

(defn- ^js/StructureSpawn -get-spawn [_ spawn-name]
  (let [res (make-SpawnProtocol (go/get (.. js/Game -spawns) spawn-name))]
    res))

(defn- ^js/Creep -get-creep [creep-name]
  (go/get (.. js/Game -creeps) creep-name))

(defn- -room-names [_]
  (js-keys (.. js/Game -rooms)))

(defn- ^js/Room -get-room [_ room-name]
  (let [res (make-RoomProtocol (go/get (.. js/Game -rooms) room-name))]
    res))

(lift-as GameProtocol)

(defmethod ig/init-key ::game [_ _]
  (lift-on GameProtocol js/Game))