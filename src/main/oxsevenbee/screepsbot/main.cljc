(ns oxsevenbee.screepsbot.main
  (:require [cljs-bean.core :refer [bean? bean object ->clj ->js]]
            [cljs-bean.transit]
            [goog.object :as go]
            [integrant.core :as ig]
            [oxsevenbee.screeps.memory :as m]
            [oxsevenbee.screeps.game :as game]
            [oxsevenbee.screeps.timeout :as t]))

(defn game-loop [{:keys [memory executors game timeout]}]
  (m/pre-tick memory)
  (try
    (t/new-tick timeout (game/time game))
    (catch js/Error e
      (game/notify game (str "Error in timeout loop" e))))
  (loop [executors executors]
    (when (seq executors)
      ((first executors))
      (recur (rest executors))))
  (m/post-tick memory))

(defmethod ig/init-key ::main-loop [_ opts]
  (try
    (partial game-loop opts)
    (catch js/Error e
      (println e))))
