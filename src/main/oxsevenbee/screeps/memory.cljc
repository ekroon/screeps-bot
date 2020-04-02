(ns oxsevenbee.screeps.memory
  (:require [integrant.core :as ig]
            [cognitect.transit :as t]
            [cljs-bean.core :refer [->clj ->js]]))


(defn delete-memory-creep [{:keys [memory]} creep-name]
  (swap! memory dissoc :creeps creep-name))

(defn delete-game-memory-creep [{:keys [memory]} creep-name]
  (js-delete (.. ^js/Memory (get @memory :game-memory) -creeps) creep-name))

(defn load-memory [memory])

(defprotocol ServerMemory
  (load-memory [_] "Load memory from server")
  (write-memory [_] "Write memory to server")
  (pre-tick [_] "Run at beginning of the loop")
  (post-tick [_] "Run at the end of the loop"))

(defprotocol Memory)

(defmethod ig/init-key ::hosted-memory [_ _]
  (try
    (let [memory      (atom nil)
          game-memory (volatile! #js {})]
      (let [r (t/reader :json)
            w (t/writer :json-verbose {:handlers (cljs-bean.transit/writer-handlers)})]
        (specify! memory
          ServerMemory
          (load-memory [_]
            (reset! memory (t/read r (.get js/RawMemory))))
          (write-memory [_]
            (.set js/RawMemory (t/write w @memory)))
          (pre-tick [_]
            (js-delete js/global "Memory")
            (set! js/global.Memory @game-memory))
          (post-tick [this]
            (write-memory this)
            (vreset! game-memory js/global.Memory)))
        (load-memory memory)
        (swap! memory #(dissoc %1 :game-memory))            ;; TODO add memory upgrader
        memory))
    (catch js/Error e
      (println e))))