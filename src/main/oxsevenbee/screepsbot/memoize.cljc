(ns oxsevenbee.screepsbot.memoize
  (:require [oxsevenbee.screeps.game :as game]
            [integrant.core :as ig]))

(def lookup-sentinel (js-obj))

(defprotocol CacheKey
  :extend-via-metadata true
  (-cache-key [_ args]))

(extend-type object
  CacheKey
  (-cache-key [_ args] args))

(defn cache-key [obj args]
  (-cache-key obj args))

(defn with-cache-fn [f cache-key-f]
  (vary-meta f assoc `cache-key #(cache-key-f %2)))

(defprotocol GameTickMemoizer
  (-with-tick-memory [this ticks f-var args]))

(defrecord Memoizer [state game]
  GameTickMemoizer
  (-with-tick-memory [_ ticks f-var args]
    (let [ckey         (cache-key f-var args)
          ticks-passed (- (game/time game) (get-in @state [f-var ckey :memoized-tick] 0))
          result       (get-in @state [f-var ckey :result] lookup-sentinel)]
      (if (or (= result lookup-sentinel) (> ticks-passed ticks))
        (let [retval (apply f-var args)]
          (swap! state assoc-in [f-var ckey :result] retval)
          (swap! state assoc-in [f-var ckey :memoized-tick] (game/time game))
          retval)
        result))))

(defn with-tick-memory [memoizer ticks f-var & args]
  (-with-tick-memory memoizer ticks f-var args))

(defmethod ig/init-key ::memoizer [_ {:keys [game]}]
  (->Memoizer (atom {}) game))
