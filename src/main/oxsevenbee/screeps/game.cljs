(ns oxsevenbee.screeps.game
  (:require [oops.core :refer [oget oset! ocall oapply ocall! oapply!
                               oget+ oset!+ ocall+ oapply+ ocall!+ oapply!+]]
            [cljs-bean.core :refer [bean ->clj ->js]]))
;(defprotocol Game
;  (constructionSites [this])
;  (cpu [this])
;  (creeps [this])
;  )

(defn create-game []
  (reify
    ILookup
    (-lookup [this k] (-lookup this k nil))
    (-lookup [_ k not-found]
      (condp = k
        :constructionSites (oget js/Game :constuctionSites)
        :cpu (oget js/Game :cpu)
        :creeps (bean (oget js/Game :creeps))
        :spawns (bean (oget js/Game :spawns))
        not-found))))

(extend-type js/Creep
  ILookup
  (-lookup
    ([this k] (-lookup this k nil))
    ([this k not-found]
     (condp = k
       :id (oget this :id)
       :name (oget this :name)
       :spawning (oget this :spawning)
       :pos (bean (oget this :pos))
       :store (oget this :store)
       nil))))

;; not allowed
#_(let [property-descriptor (js/Object.getOwnPropertyDescriptor (.-prototype js/Creep) "memory")]
  (js/Object.defineProperty (.-prototype js/Creep) "memory" #js {
                                                                 :get (fn [] (println "from get") (oget property-descriptor "get"))
                                                                 :set (fn [] (println "from set") (oget property-descriptor "set"))
                                                                 }))