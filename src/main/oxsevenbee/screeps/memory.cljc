(ns oxsevenbee.screeps.memory
  (:require [integrant.core :as ig]
            [cognitect.transit :as t]
            [cljs-bean.core :refer [->clj ->js]]))

(defn delete-memory-creep [{:keys [memory]} creep-name]
  (swap! memory dissoc :creeps creep-name))

(defn delete-game-memory-creep [{:keys [memory]} creep-name]
  (js-delete (.. ^js/Memory (get @memory :game-memory) -creeps) creep-name))

;; --- Integrant

(defmethod ig/init-key ::hosted-memory [_ _]
  (try
    (let [memory (atom nil)]
      (letfn [(load-memory []
                (js-delete js/global "Memory")              ;; deleting is important! removes property
                (set! js/global.Memory (-> @memory (get-in [:game-memory]))))
              (write-memory []
                (let [start (.. js/Game -cpu getUsed)]
                  (let [w (t/writer :json-verbose
                                    {:handlers (cljs-bean.transit/writer-handlers)})]
                    (swap! memory (fn [m] (assoc m :game-memory js/global.Memory)))
                    (.set js/RawMemory (t/write w @memory))
                    #_(.set js/RawMemory (t/write w {})))
                  #_(println "Written memory in:" (- (.. js/Game -cpu getUsed) start) "CPU time")))
              ]
        (let [start (.. js/Game -cpu getUsed)]
          (let [r (t/reader :json)]
            (reset! memory
                    (update (t/read r (.get js/RawMemory)) :game-memory ->js)))
          #_(println "Parsed memory in:" (- (.. js/Game -cpu getUsed) start) "CPU time"))
        {:memory       memory
         :load-memory  load-memory
         :write-memory write-memory}))
    (catch js/Error e
      (println e))))