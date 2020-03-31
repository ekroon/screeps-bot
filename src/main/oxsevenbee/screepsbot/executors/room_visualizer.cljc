(ns oxsevenbee.screepsbot.executors.room-visualizer
  (:require [cljs-bean.core :refer [bean? bean object ->clj ->js]]
            [cljs-bean.transit]
            [goog.object :as go]
            [oxsevenbee.screepsbot.executors :refer [RoomExecutor]]
            [oxsevenbee.screeps.protocols :as osp]
            [oxsevenbee.screeps.game :as game]
            [oxsevenbee.screeps.spawn :as os-spawn]
            [oxsevenbee.screeps.room :as os-room]
            [cljs.spec.alpha :as s]
            [cljs.spec.test.alpha :as stest]
            [cognitect.transit :as t]
            [integrant.core :as ig]))

(defn should-execute [{:keys [shard room-name]}]
  true)

(defn execute [{:keys []} {:keys [room-name]}]
  (let [visual ^js/RoomVisual (js/RoomVisual. room-name)]
    (.text visual room-name 5 5)))

(defmethod ig/init-key ::executor [_ opts]
  (reify RoomExecutor
    (should-execute [this opts] (should-execute opts))
    (execute [this args] ((partial execute {}) args))))