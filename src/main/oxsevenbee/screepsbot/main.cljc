(ns oxsevenbee.screepsbot.main
  (:require [cljs-bean.core :refer [bean? bean object ->clj ->js]]
            [cljs-bean.transit]
            [goog.object :as go]
            [oxsevenbee.screeps.game :refer [get-spawn]]
            [oxsevenbee.screeps.spawn :refer [spawn-creep]]
            [cljs.spec.alpha :as s]
            [cljs.spec.test.alpha :as stest]
            [cognitect.transit :as t]
            [integrant.core :as ig]))

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

;(defn ^js/StructSpawn get-spawn [spawn-name]
;  (go/get (.. js/Game -spawns) spawn-name))

;(defn spawn-creep [{:keys [game]} spawn creep-name body]
;  (.spawnCreep js/StructureSpawn (go/get (.-spawns js/Game) spawn) (clj->js body) (name creep-name)))

(defn get-creep-memory [{:keys [memory] :as context} creep-name k default]
  (-> @memory (get-in [:creeps creep-name k] default)))

(defn set-creep-memory [{:keys [memory]} creep-name k v]
  (swap! memory (fn [m] (assoc-in m [:creeps creep-name k] v))))

(defn ^js/Creep get-creep [creep-name]
  (get-in (->clj js/Game) [:creeps (keyword creep-name)]))

(defn creep-used-capacity [^js/Creep creep]
  (let [store (.-store creep)]
    (str (.getUsedCapacity store) "/" (.getCapacity store))))

; keep controller active for now
(defn manual-shard3-e39s51-controller-upgrade
  ([{:keys [memory] :as context} creep-name]
   (manual-shard3-e39s51-controller-upgrade context creep-name true))
  ([{:keys [memory] :as context} creep-name first-harvest]
   (when-let [creep (get-creep creep-name)]
     (if-not (get-creep-memory context creep-name "working" false)
       (do
         (let [pos (.-pos creep)]
           (if (not= [(.-x pos) (.-y pos)] [33 28])
             (do
               (if (= 0 (.-fatigue creep))
                 (.moveTo creep 33 28)
                 (.say creep (str "tired: " (.-fatigue creep)))))
             (do
               (.say creep (str "> " (creep-used-capacity creep)))
               (if (> (.getFreeCapacity (.-store creep)) 0)
                 (.harvest creep (.getObjectById js/Game "5bbcaf4b9099fc012e63a6fa"))
                 (do
                   (set-creep-memory context creep-name "working" true)
                   (when first-harvest (recur context creep-name false))))))))
       (do
         (.say creep (str "< " (creep-used-capacity creep)))
         (if (> (.getUsedCapacity (.-store creep)) 0)
           (.upgradeController creep (.getObjectById js/Game "5bbcaf4b9099fc012e63a6fb"))
           (do
             (set-creep-memory context creep-name "working" false)
             (when first-harvest (recur context creep-name false)))))))))

(defn game-loop [{:keys [memory load-memory write-memory]} game]
  (load-memory)

  (let [context {:memory memory
                 :game   game}]
    (-> (get-spawn (:game context) "Spawn1") (spawn-creep "Harvester1" [js/WORK js/WORK js/CARRY js/MOVE]))
    ;(spawn-creep (:game context) "Spawn1" "Harvester" [js/WORK js/WORK js/CARRY js/MOVE])
    (manual-shard3-e39s51-controller-upgrade context "Harvester1"))

  (write-memory))

;; {:memory {:memory #object[cljs.core.Atom {:val {:game-memory #js {:creeps #js {:Harvester1 #js {:_move #js {:dest #js {:x 33, :y 28, :room E39S51}, :time 16841186, :path 33284, :room E39S51}}}}, :creeps {Harvester1 {working true}}}}], :load-memory #object[Function], :write-memory #object[Function]}, :tick-trigger {:tick-trigger #object[cljs.core.Atom {:val nil}]}}

(defmethod ig/init-key ::main-loop [_ opts]
  (try
    (partial game-loop (get opts :memory) (get opts :game))
    (catch js/Error e
      (println e))))

#_(set! js/module.exports.loop my-loop)
