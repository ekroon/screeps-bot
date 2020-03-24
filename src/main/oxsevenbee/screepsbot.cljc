(ns oxsevenbee.screepsbot
  (:require [goog.object :as go]
            [cljs-bean.core :refer [->clj]]
            [cljs.pprint :as pprint]
            [oxsevenbee.screepsbot.main :as osm]))



(defn bot-loop []
  (osm/game-loop))

(defn print-constants []
  (let [globals (filter #(re-matches #"[A-Z]{1}[A-Z_]*" %) (js-keys js/global))]
    (into {} (map (fn [v] [(keyword v) (go/get js/global v)]) globals))
    #_(doseq [g globals] (println (type g) g (go/get js/global g)))))

(set! js/global.printConstants print-constants)