(ns oxsevenbee.screepsbot.executors.room-executor
  (:require [integrant.core :as ig]
            [cljs-bean.core :refer [->clj]]
            [oxsevenbee.screepsbot.executors :as executors]
            [oxsevenbee.screeps.protocols :as osp]
            [oxsevenbee.screeps.game :as game]
            [oxsevenbee.screeps.room :as room]
            [oxsevenbee.screeps.source :as source]))

(defn room-sources [{:keys [memory] :as context} room]
  (when-not (get-in @memory [:room (room/name room) :sources])
    (swap! memory
           (fn [m] (assoc-in m [:room (room/name room) :sources]
                             (into {}
                                   (map #(vector (source/id %) {}) (room/sources room))))))))

(defn execute-room [{:keys [memory game] :as context} room-name]
  (let [room (game/room game room-name)]
    (room-sources context room)))

(defn execute [{:keys [memory game executors] :as context}]
  (let [names (game/room-names game)]
    (doseq [room-name names]
      (do
        (let [enabled (filter #(executors/should-execute % {:shard     (game/shard game)
                                                            :room-name room-name}) executors)]
          (doseq [executor enabled]
            (executors/execute executor {:room-name room-name})))
        (execute-room context room-name)))))

(derive ::room-executor ::executors/executor)

(defmethod ig/init-key ::room-executor [_ {:keys [memory game executors]}]
  (partial execute {:memory    (:memory memory)
                    :game      game
                    :executors executors}))