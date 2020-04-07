(ns oxsevenbee.screeps.timeout
  (:require [integrant.core :as ig]
            [oxsevenbee.screeps.game :as game]))

(defprotocol Timeout
  (setTimeout [_ f] [_ f ticks])
  (setInterval [_ f] [_ f ticks])
  (clearTimeout [_ timeout])
  (clearInterval [_ interval]))

(defprotocol Tick
  (new-tick [_ tick]))

(deftype GameTimeout [state]
  Timeout
  (setTimeout [this f] (setTimeout this f 0))
  (setTimeout [this f ticks]
    (let [number       (get @state :next-timeout-number)
          current-tick (get @state :tick)]
      (swap! state #(-> %1
                        (update :next-timeout-number inc)
                        (update :timeouts conj [number [ticks f]])
                        (update :scheduled-timeouts update (+ current-tick ticks) conj number)))))
  (setInterval [this f] (setInterval this f 10))
  (setInterval [this f ticks])
  (clearTimeout [this timeout])
  (clearInterval [this interval])
  Tick
  (new-tick [_ tick]
    (let [old-tick (get @state :tick)
          passed   (- tick old-tick)
          {t-to-run true
           t-rest   false} (group-by (fn [[k v]] (<= k tick)) (get @state :scheduled-timeouts))]
      (doseq [[_ tt] t-to-run
              t      tt]
        (let [[_ f] (get-in @state [:timeouts t])]
          (f)))
      (swap! state (fn [s]
                     (-> s
                         (assoc :tick tick)
                         (assoc :scheduled-timeouts (into (sorted-map) t-rest))
                         (update :timeouts #(select-keys %1 (map (comp first second) t-rest)))))))))

(defn make-timeout [tick] (->GameTimeout (atom {:tick                 tick
                                                :next-timeout-number  1
                                                :next-interval-number 1
                                                :timeouts             {}
                                                :intervals            {}
                                                :scheduled-timeouts   (sorted-map)
                                                :scheduled-intervals  (sorted-map)})))

(defmethod ig/init-key ::timeout [_ {:keys [game]}]
  (make-timeout (game/time game)))