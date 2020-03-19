(ns oxsevenbee.screepsbot.main
  (:require [oops.core :refer [oget oset! ocall oapply ocall! oapply!
                               oget+ oset!+ ocall+ oapply+ ocall!+ oapply!+]]
            [cljs-bean.core :refer [bean ->clj ->js]]
            [oxsevenbee.screeps.game :refer [create-game]]
            ))

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

;(doseq [[k v] (seq (js->clj js/Game.rooms))]
;  (js/console.log ">>>>" k))

;(def game (->clj js/Game))
(def game (create-game))
;(def game js/Game)

(def memory (->clj js/Memory))

(defn spawn-creep [spawn creep-name body]
  (ocall (get-in game [:spawns spawn]) :spawnCreep body (name creep-name)))

;; if harvesting
;; else if not empty deliver

(defn set-memory [path-to m]
  (oset! memory path-to (->js m)))

(defn get-memory [path-to]
  (->clj (oget memory path-to)))

; keep controller active for now
(defn harvest [creep-name]
  (when-let [creep (get-in game [:creeps creep-name])]
    (if-not (get-memory [:creeps creep-name :working])
      (do
        (let [{x :x y :y} (:pos creep)]
          (if (not= [x y] [33 28])
            (ocall creep :moveTo 33 28)
            (do
              (ocall creep :say "harvesting")
              (ocall creep :harvest (ocall js/Game :getObjectById "5bbcaf4b9099fc012e63a6fa"))
              (when (<= (ocall (:store creep) :getFreeCapacity) 0)
                (set-memory [:creeps creep-name :working] true)))
            #_(if (<= (ocall (:store creep) :getFreeCapacity) 0)
              (do
                (ocall creep :say "next: upgrade")
                (set-memory [:creeps creep-name :working] true))
              (do
                (ocall creep :say "harvesting")
                (ocall creep :harvest (ocall js/Game :getObjectById "5bbcaf4b9099fc012e63a6fa")))))))
      (do
        (ocall creep :say "upgrading")
        (ocall creep :upgradeController (ocall js/Game :getObjectById "5bbcaf4b9099fc012e63a6fb"))
        (when (<= (ocall (:store creep) :getUsedCapacity) 0)
          (set-memory [:creeps creep-name :working] false)))
      #_(if (<= (ocall (:store creep) :getUsedCapacity) 0)
        (do
          (ocall creep :say "next: harvest")
          (set-memory [:creeps creep-name :working] false))
        (do
          (ocall creep :say "upgrading")
          (ocall creep :upgradeController (ocall js/Game :getObjectById "5bbcaf4b9099fc012e63a6fb")))))))

;(set-memory [:creeps creep-name] {:role :harvester :harvesting true})

(defn ^:export game-loop []
  (js/console.log (spawn-creep :Spawn1 :Harvester1 #js [js/WORK js/WORK js/CARRY js/MOVE]))
  (harvest :Harvester1)
  (println (ocall js/Game [:cpu :getUsed])))

#_(set! js/module.exports.loop my-loop)
