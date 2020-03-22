(ns oxsevenbee.screepsbot.main
  (:require [goog.object :as go]
            [oxsevenbee.screeps.game :refer []]))

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

(defn get-creep-memory [creep-name k default]
  (initialize-memory-creeps)
  (initialize-memory-creep creep-name)
  (if (go/containsKey (go/get (.-creeps js/Memory) creep-name) k)
    (go/getValueByKeys (.-creeps js/Memory) creep-name k)
    (do
      (println "returning default value for [creep-name k]" [creep-name k])
      default)))

(defn set-creep-memory [creep-name k v]
  (initialize-memory-creeps)
  (initialize-memory-creep creep-name)
  (let [creep-memory (go/get (.-creeps js/Memory) creep-name)]
    (go/set creep-memory k v)))

(defn ^js/Creep get-creep [creep-name]
  (go/get (.. js/Game -creeps) creep-name))

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
               (.say creep "harvesting")
               (.harvest creep (.getObjectById js/Game "5bbcaf4b9099fc012e63a6fa"))
               (when (<= (.getFreeCapacity (.-store creep)) 0)
                 (set-creep-memory creep-name "working" true)
                 (when first-harvest (harvest creep-name false)))))))
       (do
         (.say creep "upgrading")
         (.upgradeController creep (.getObjectById js/Game "5bbcaf4b9099fc012e63a6fb"))
         (when (<= (.getUsedCapacity (.-store creep)) 0)
           (set-creep-memory creep-name "working" false)
           (when first-harvest (harvest creep-name false))))))))

(defn load-memory []
  (when (nil? @memory)
    (let [start (.. js/Game -cpu getUsed)]
      (reset! memory (.parse js/JSON (.get js/RawMemory)))
      #_(println "Parsed memory in:" (- (.. js/Game -cpu getUsed) start)  "CPU time")))
  (set! js/global.Memory @memory))

(defn write-memory []
  (let [start (.. js/Game -cpu getUsed)]
    (reset! memory js/Memory)
    (.set js/RawMemory (.stringify js/JSON js/Memory))
    #_(println "Written memory in:" (- (.. js/Game -cpu getUsed) start) "CPU time")))

(defn ^:export game-loop []
  (load-memory)

  (spawn-creep "Spawn1" "Harvester1" #js [js/WORK js/WORK js/CARRY js/MOVE])
  (harvest "Harvester1")

  (write-memory))

#_(set! js/module.exports.loop my-loop)
