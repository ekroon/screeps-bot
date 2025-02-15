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

(defn ^js/Creep get-creep [{:keys [game]} creep-name]
  (os-game/creep game creep-name))

(defn creep-used-capacity [^js/Creep creep]
  (let [store (.-store creep)]
    (str (.getUsedCapacity store) "/" (.getCapacity store))))

; keep controller active for now
(defn manual-shard3-e39s51-controller-upgrade
  ([{:keys [memory] :as context} creep-name pos]
   (manual-shard3-e39s51-controller-upgrade context creep-name pos true))
  ([{:keys [memory] :as context} creep-name pos first-harvest]
   (when-let [creep (get-creep context creep-name)]
     (if-not (get-creep-memory context creep-name "working" false)
       (do
         (let [creep-pos (.-pos creep)]
           (if (not= [(.-x creep-pos) (.-y creep-pos)] pos)
             (do
               (if (= 0 (.-fatigue creep))
                 (let [[x y] pos]
                   (.moveTo creep x y))
                 (.say creep (str "tired: " (.-fatigue creep)))))
             (if (> (.getFreeCapacity (.-store creep)) 0)
               (do
                 (.say creep (str "⛏"))
                 (.harvest creep (.getObjectById js/Game "5bbcaf4b9099fc012e63a6fa")))
               (do
                 (set-creep-memory context creep-name "working" true)
                 (when first-harvest (recur context creep-name pos false)))))))
       (if (> (.getUsedCapacity (.-store creep)) 0)
         (do
           (.say creep (str "⏫"))
           (.upgradeController creep (.getObjectById js/Game "5bbcaf4b9099fc012e63a6fb")))
         (do
           (set-creep-memory context creep-name "working" false)
           (when first-harvest (recur context creep-name pos false))))))))

(defn should-execute [{:keys [shard room-name]}]
  (if (and (= (:name shard) "shard3")
           (= room-name "E39S51"))
    :solo
    false))

(defn upgrade [context creep-name pos]
  (-> (os-game/spawn (:game context) "Spawn1") (os-spawn/spawn-creep creep-name [js/WORK js/WORK js/CARRY js/MOVE]))
  (manual-shard3-e39s51-controller-upgrade context creep-name pos))

(defn execute [{:keys [memory game]} {:keys [room-name]}]
  (let [context {:memory memory
                 :game   game}]
    (upgrade context "shard3-e39s51-1" [32 28])
    (upgrade context "shard3-e39s51-2" [33 28])
    (upgrade context "shard3-e39s51-3" [34 28])
    ))

(defmethod ig/init-key ::executor [_ opts]
  (reify RoomExecutor
    (should-execute [this opts] (should-execute opts))
    (execute [this args] ((partial execute opts) args))))
