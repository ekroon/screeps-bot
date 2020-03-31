(ns oxsevenbee.screepsbot.main
  (:require [cljs-bean.core :refer [bean? bean object ->clj ->js]]
            [cljs-bean.transit]
            [goog.object :as go]
            [integrant.core :as ig]))

(defn game-loop [{:keys [load-memory write-memory]} executors]
  (load-memory)
  (loop [executors executors]
    (when (seq executors)
      ((first executors))
      (recur (rest executors))))
  (write-memory))

(defmethod ig/init-key ::main-loop [_ opts]
  (try
    (partial game-loop (get opts :memory) (get opts :executors))
    (catch js/Error e
      (println e))))
