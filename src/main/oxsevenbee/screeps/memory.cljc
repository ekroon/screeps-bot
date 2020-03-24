(ns oxsevenbee.screeps.memory
  (:require [com.stuartsierra.component :as component]))

(defprotocol Memory
  (load-memory [this])
  (write-memory [this]))

(defrecord GameMemory [memory initialized]
  Memory
  (load-memory [_]
    (js-delete js/global "Memory") ;; deleting is important! removes property
    (set! js/global.Memory (-> @memory (get-in [:game-memory]))))
  (write-memory [_]
    (let [start (.. js/Game -cpu getUsed)]
      (let [w (t/writer :json-verbose
                        {:handlers (cljs-bean.transit/writer-handlers)})]
        (swap! memory (fn [m] (assoc m :game-memory js/global.Memory)))
        (.set js/RawMemory (t/write w @memory))
        #_(.set js/RawMemory (t/write w {})))
      #_(println "Written memory in:" (- (.. js/Game -cpu getUsed) start) "CPU time")))
  component/Lifecycle
  (start [this]
    (when-not initialized
      (let [start (.. js/Game -cpu getUsed)]
        (let [r (t/reader :json)]
          (reset! memory
                  (update (t/read r (.get js/RawMemory)) :game-memory ->js)))
        (assoc this :initialized true)
        #_(println "Parsed memory in:" (- (.. js/Game -cpu getUsed) start)  "CPU time"))))
  (stop [this] (-> this (assoc :memory (atom)) (assoc :initalized))))

(defn new-game-memory []
  (GameMemory. (atom) false))

(defrecord ReplMemory [memory]
  Memory
  (load-memory [_])
  (write-memory [_]))

(defn new-repl-memory []
  (ReplMemory. (atom {})))
