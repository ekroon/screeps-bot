(ns oxsevenbee.screepsbot
  (:require [goog.object :as go]
            [cljs-bean.core :refer [->clj]]
            [cljs.pprint :as pprint]
            [oxsevenbee.screepsbot.main :as osm]
            [oxsevenbee.screeps.memory :as os-mem]
            [oxsevenbee.screeps.game :as os-game]
            [oxsevenbee.screeps.timeout :as timeout]
            [oxsevenbee.screepsbot.memoize :as memoize]
            [oxsevenbee.screepsbot.executors :as executors]
            [oxsevenbee.screepsbot.executors.room-executor :as room-executor]
            [oxsevenbee.screepsbot.executors.room-visualizer :as room-visualizer]
            [oxsevenbee.screepsbot.executors.shard3-e39s51-executor :as shard3-e39s51-executor]
            [integrant.core :as ig]))

(set! *warn-on-infer* true)

(def tick-handler (volatile! nil))

(defmethod ig/init-key ::tick-handler [_ {:keys [handler]}]
  (vreset! tick-handler handler))

(def hosted-system
  {::os-mem/hosted-memory            {}
   ::os-game/game                    {}
   ::memoize/memoizer                {:game      (ig/ref ::os-game/game)}
   ::timeout/timeout                 {:game      (ig/ref ::os-game/game)}
   ::shard3-e39s51-executor/executor {:memory    (ig/ref ::os-mem/hosted-memory)
                                      :game      (ig/ref ::os-game/game)}
   ::room-visualizer/executor        {:game      (ig/ref ::os-game/game)
                                      :memoizer  (ig/ref ::memoize/memoizer)}
   ::room-executor/room-executor     {:game      (ig/ref ::os-game/game)
                                      :memory    (ig/ref ::os-mem/hosted-memory)
                                      :executors [(ig/ref ::shard3-e39s51-executor/executor)
                                                  (ig/ref ::room-visualizer/executor)]}
   ::tick-handler                    {:handler (ig/ref ::osm/main-loop)}
   ::osm/main-loop                   {:memory    (ig/ref ::os-mem/hosted-memory)
                                      :executors (ig/refset ::executors/executor)
                                      :game      (ig/ref ::os-game/game)
                                      :timeout   (ig/ref ::timeout/timeout)}})

(def system (delay (ig/init hosted-system)))

(defn bot-loop []
  @system
  (if-let [handler @tick-handler]
    (handler)
    (println "No tick handler registered"))
  #_(osm/game-loop))

(defn print-constants []
  (let [globals (filter #(re-matches #"[A-Z]{1}[A-Z_]*" %) (js-keys js/global))]
    (into {} (map (fn [v] [(keyword v) (go/get js/global v)]) globals))
    #_(doseq [g globals] (println (type g) g (go/get js/global g)))))

(set! js/global.printConstants print-constants)