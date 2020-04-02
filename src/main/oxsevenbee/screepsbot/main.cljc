(ns oxsevenbee.screepsbot.main
  (:require [cljs-bean.core :refer [bean? bean object ->clj ->js]]
            [cljs-bean.transit]
            [goog.object :as go]
            [integrant.core :as ig]
            [oxsevenbee.screeps.memory :as m]))

(defn game-loop [{:keys [memory executors]}]
  (m/pre-tick memory)
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
