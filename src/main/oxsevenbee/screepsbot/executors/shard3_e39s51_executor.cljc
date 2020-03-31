(ns oxsevenbee.screepsbot.executors.shard3-e39s51-executor
  (:require [cljs-bean.core :refer [bean? bean object ->clj ->js]]
            [cljs-bean.transit]
            [goog.object :as go]
            [oxsevenbee.screepsbot.executors :refer [RoomExecutor]]
            [oxsevenbee.screeps.protocols :as osp]
            [oxsevenbee.screeps.game :as os-game]
            [oxsevenbee.screeps.spawn :as os-spawn]
            [oxsevenbee.screeps.room :as os-room]
            [cljs.spec.alpha :as s]
            [cljs.spec.test.alpha :as stest]
            [cognitect.transit :as t]
            [integrant.core :as ig]))

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
           (if (not= [(.-x pos) (.-y pos)] [32 28])
             (do
               (if (= 0 (.-fatigue creep))
                 (.moveTo creep 32 28)
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

(defn should-execute [{:keys [shard room-name]}]
  (if (and (= (:name shard) "shard3")
           (= room-name "E39S51"))
    :solo
    false))

(defn execute [{:keys [memory game]} {:keys [room-name]}]
  (let [context {:memory memory
                 :game   game}]
    (-> (os-game/spawn (:game context) "Spawn1") (os-spawn/spawn-creep "Harvester1" [js/WORK js/WORK js/CARRY js/MOVE]))
    (manual-shard3-e39s51-controller-upgrade context "Harvester1")))

(defmethod ig/init-key ::executor [_ opts]
  (reify RoomExecutor
    (should-execute [this opts] (should-execute opts))
    (execute [this args] ((partial execute {:memory (get-in opts [:memory :memory])
                                            :game   (get opts :game)}) args))))
