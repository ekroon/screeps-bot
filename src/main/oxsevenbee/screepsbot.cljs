(ns oxsevenbee.screepsbot
  (:require [cljs.source-map]
    [oxsevenbee.screepsbot.main :as osm]))

;; clj -m cljs.main --optimizations simple -c oxsevenbee.screepsbot

(defn bot-loop []
  (osm/game-loop))

#_(oset! js/module "exports" "loop" bot-loop)