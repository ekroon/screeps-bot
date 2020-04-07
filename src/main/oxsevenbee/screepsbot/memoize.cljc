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

#_(defn cache-key [obj args]
  (-cache-key obj args))

#_(defn with-cache-fn [f cache-key-f]
  (vary-meta f assoc `cache-key #(cache-key-f %2)))

(defn cache-index [obj]
  (get (meta obj) ::cache-index))

(defn cache-key [obj args]
  ((or (get (meta obj) ::cache-fn) identity) args))

(defn with-cache-fn [f cache-key-f]
  (vary-meta f assoc ::cache-fn cache-key-f))

(defprotocol GameTickMemoizer
  (-with-tick-memory [this ticks f-var args]))

(defrecord Memoizer [state game]
  GameTickMemoizer
  (-with-tick-memory [_ ticks f args]
    (if-let [cindex (cache-index f)]
      (let [ckey         (cache-key f args)
            ticks-passed (- (game/time game) (get-in @state [cindex ckey :memoized-tick] 0))
            result       (get-in @state [cindex ckey :result] lookup-sentinel)]
        (if (or (= result lookup-sentinel) (> ticks-passed ticks))
          (let [retval (apply f args)]
            (swap! state assoc-in [cindex ckey :result] retval)
            (swap! state assoc-in [cindex ckey :memoized-tick] (game/time game))
            retval)
          result))
      #_else
      (do (println "No ::cache-index meta for memoized function:" (meta f))
          (apply f args)))))

(defn with-tick-memory [memoizer ticks f-var & args]
  (-with-tick-memory memoizer ticks f-var args))

(defmethod ig/init-key ::memoizer [_ {:keys [game]}]
  (->Memoizer (atom {}) game))
