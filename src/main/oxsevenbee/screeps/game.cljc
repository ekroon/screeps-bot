(ns oxsevenbee.screeps.game
  (:require [integrant.core :as ig]
            [goog.object :as go]
            [cljs-bean.core :refer [->clj ->js]]
            [oxsevenbee.screeps.protocols :as osp]
            [oxsevenbee.screeps.spawn :refer [make-SpawnProtocol]]
            [oxsevenbee.screeps.room :refer [make-RoomProtocol]]))

(defprotocol GameHolder
  :extend-via-metadata true
  (game [_]))

(defprotocol GameProtocol
  :extend-via-metadata true
  (shard [game])
  (object-by-id [game id])
  (spawns [game])
  (spawn [game spawn-name])
  (creeps [game])
  (creep [game creep-name])
  (rooms [game]))

(defn- ^js/Game -game ^js/Game [_]
  js/Game)

(defn- -shard [this]
  (let [shard (go/get (game this) "shard")]
    {:name (go/get shard "name")
     :type (go/get shard "type")
     :ptr  (go/get shard "ptr")}))

(defn- -object-by-id [this id]
  (.getObjectById ^js/Game (game this) id))

(defn- -spawns [this]
  (go/get (game this) "spawns"))

(defn- ^js/StructureSpawn -get-spawn [this spawn-name]
  (let [res (make-SpawnProtocol (go/get (spawns this) spawn-name))]
    res))

(defn- -creeps [this]
  (go/get (game this) "creeps"))

(defn- ^js/Creep -creep [this creep-name]
  (go/get (creeps this) creep-name))

(defn- -rooms [this]
  (go/get (game this) "rooms"))

(defn- room-names [this]
  (js-keys (rooms this)))

(defn- room [this room-name]
  (let [res (make-RoomProtocol (go/get (rooms this) room-name))]
    res))

(defmethod ig/init-key ::game [_ _]
  (try
    (with-meta {}
               {`game         -game
                `shard        -shard
                `object-by-id -object-by-id
                `spawns       -spawns
                `spawn        -get-spawn
                `creeps       -creeps
                `creep        -creep
                `rooms        -rooms})
    (catch js/Error e
      (println e))))