(ns oxsevenbee.screepsbot.executors.room-visualizer
  (:require [cljs-bean.core :refer [bean? bean object ->clj ->js]]
            [cljs-bean.transit]
            [goog.object :as go]
            [oxsevenbee.screepsbot.memoize :as s-memoize]
            [oxsevenbee.screepsbot.executors :refer [RoomExecutor]]
            [oxsevenbee.screeps.protocols :as osp]
            [oxsevenbee.screeps.game :as game]
            [oxsevenbee.screeps.spawn :as os-spawn]
            [oxsevenbee.screeps.room :as os-room]
            [oxsevenbee.utils :refer [lifted]]
            [cljs.spec.alpha :as s]
            [cljs.spec.test.alpha :as stest]
            [cognitect.transit :as t]
            [integrant.core :as ig]))

(defn extension-ratio [{:keys [game]} room-name]
  (let [room           ^js/Room (lifted (game/room game room-name))
        rcl            (or (go/getValueByKeys room "controller" "level") 0)
        max-extensions (go/getValueByKeys js/CONTROLLER_STRUCTURES "extension" rcl)
        extensions     (count (filter (comp #(= js/STRUCTURE_EXTENSION %)
                                            #(go/get % "structureType"))
                                      (.find room js/FIND_MY_STRUCTURES)))]
    [extensions max-extensions]))

(defn should-execute [{:keys [shard room-name]}]
  true)

(defn execute [{:keys [game memoizer] :as context} {:keys [room-name]}]
  (let [visual   ^js/RoomVisual (js/RoomVisual. room-name)
        ex-ratio (s-memoize/with-tick-memory
                   memoizer 100
                   (s-memoize/with-cache-fn #'extension-ratio rest)
                   context room-name)]
    (.text visual (pr-str ex-ratio) 5 5)))

(defmethod ig/init-key ::executor [_ context]
  (let [-execute (partial execute context)]
    (reify RoomExecutor
      (should-execute [this opts] (should-execute context))
      (execute [this args] (-execute args)))))