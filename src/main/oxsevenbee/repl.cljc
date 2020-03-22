(ns oxsevenbee.repl
  (:require [cljs-bean.core :refer [bean? bean object ->clj ->js]]))

(defn js-obj-without-proto [m]
  (let [obj (clj->js m)]
    (set! js/obj.__proto__ nil)
    (set! js/obj.__proto__ (.-__proto__ #js {}))
    (println (.stringify js/JSON obj))
    obj))

(defn set-memory [path-to v]
  (let [memory (->clj js/Memory)]
    (assoc-in memory path-to v)))

(defn get-memory [path-to]
  (let [memory  (->clj js/Memory)]
    (get-in memory path-to)))

(defn update-mem-test []
  (let [o #js {:creeps {:harvester1 {:working false}}}
        b (->clj o)]
    (bean? (->js (assoc-in b [:creeps :harvester1 :working] true)))))