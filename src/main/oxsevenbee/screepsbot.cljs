(ns oxsevenbee.screepsbot
  (:require [oops.core :refer [oget oset! ocall oapply ocall! oapply!
                               oget+ oset!+ ocall+ oapply+ ocall!+ oapply!+]]
            [oxsevenbee.screepsbot.main :as osm]))

;; clj -m cljs.main --optimizations simple -c oxsevenbee.screepsbot

(defn bot-loop []
  (osm/game-loop))

#_(oset! js/module "exports" "loop" bot-loop)