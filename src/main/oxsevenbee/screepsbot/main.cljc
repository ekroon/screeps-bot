(ns oxsevenbee.screepsbot.main
  (:require [cljs-bean.core :refer [bean? bean object ->clj ->js]]
            [cljs-bean.transit]
            [goog.object :as go]
            [oxsevenbee.screeps.game :refer []]
            [cljs.spec.alpha :as s]
            [cljs.spec.test.alpha :as stest]
            [cognitect.transit :as t]))

(set! *warn-on-infer* true)

;; Spawn
;; Game.spawns['Spawn1'].spawnCreep( [WORK, CARRY, MOVE], 'Harvester1' );

;; Harvest
;; module.exports.loop = function () {
;;    var creep = Game.creeps['Harvester1'];
;;    var sources = creep.room.find(FIND_SOURCES);
;;    if(creep.harvest(sources[0]) == ERR_NOT_IN_RANGE) {
;;        creep.moveTo(sources[0]);
;;    }
;;}

;; Entity Components System
;; Main Entity -> Empire?
;; Empire looks expansion options

(defn ^js/StructSpawn get-spawn [spawn-name]
  (go/get (.. js/Game -spawns) spawn-name))

(def memory (atom nil))

(defn spawn-creep [spawn creep-name body]
  (.spawnCreep (get-spawn spawn) (clj->js body) (name creep-name)))

(defn initialize-memory-creeps []
  (when-not (.-creeps js/Memory)
    (set! (.-creeps js/Memory) #js {})))

(defn initialize-memory-creep [creep-name]
  (go/setIfUndefined (.-creeps js/Memory) creep-name #js {}))

(s/fdef get-creep-memory
        :args (s/cat :creep-name string? :k string?))
(defn get-creep-memory [creep-name k default]
  ;; ->clj default is keywords
  (-> js/Memory ->clj (get-in [:creeps (keyword creep-name) (keyword k)] default)))

(defn set-creep-memory [creep-name k v]
  (set! js/Memory (-> js/Memory ->clj (assoc-in [:creeps (keyword creep-name) (keyword k)] v) ->js)))

(defn ^js/Creep get-creep [creep-name]
  (get-in (->clj js/Game) [:creeps (keyword creep-name)]))

(defn creep-used-capacity [^js/Creep creep]
  (let [store (.-store creep)]
    (str (.getUsedCapacity store) "/" (.getCapacity store))))

; keep controller active for now
(defn harvest
  ([creep-name] (harvest creep-name true))
  ([creep-name first-harvest]
   (when-let [creep (get-creep creep-name)]
     (if-not (get-creep-memory creep-name "working" false)
       (do
         (let [pos (.-pos creep)]
           (if (not= [(.-x pos) (.-y pos)] [33 28])
             (do
               (.moveTo creep 33 28))
             (do
               (.say creep (str "> " (creep-used-capacity creep)))
               (if (> (.getFreeCapacity (.-store creep)) 0)
                 (.harvest creep (.getObjectById js/Game "5bbcaf4b9099fc012e63a6fa"))
                 (do
                   (set-creep-memory creep-name "working" true)
                   (when first-harvest (harvest creep-name false))))))))
       (do
         (.say creep (str "< " (creep-used-capacity creep)))
         (if (> (.getUsedCapacity (.-store creep)) 0)
           (.upgradeController creep (.getObjectById js/Game "5bbcaf4b9099fc012e63a6fb"))
           (do
             (set-creep-memory creep-name "working" false)
             (when first-harvest (harvest creep-name false)))))))))

(defn load-memory []
  (when (nil? @memory)
    (let [start (.. js/Game -cpu getUsed)]
      (let [r (t/reader :json)]
        (reset! memory (t/read r (.get js/RawMemory))))
      #_(println "Parsed memory in:" (- (.. js/Game -cpu getUsed) start)  "CPU time")))
  (js-delete js/global "Memory") ;; deleting is important! removes property
  (set! js/global.Memory (-> @memory (get-in [:raw-memory] {}))))

(defn write-memory []
  (let [start (.. js/Game -cpu getUsed)]
    (let [w (t/writer :json
                      {:handlers (cljs-bean.transit/writer-handlers)})]
      (swap! memory (fn [m] (assoc m :raw-memory js/Memory)))
      (.set js/RawMemory (t/write w @memory)))
    #_(println "Written memory in:" (- (.. js/Game -cpu getUsed) start) "CPU time")))

(defn ^:export game-loop []
  (load-memory)

  (spawn-creep "Spawn1" "Harvester1" #js [js/WORK js/WORK js/CARRY js/MOVE])
  (harvest "Harvester1")

  (write-memory))

#_(set! js/module.exports.loop my-loop)
