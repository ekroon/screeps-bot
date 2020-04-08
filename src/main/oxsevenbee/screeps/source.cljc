(ns oxsevenbee.screeps.source
  (:require [goog.object :as go]
            [cljs-bean.core :refer [->js ->clj]]))

(defprotocol SourceProtocol
  (-id [source]))

(extend-type js/Source
  SourceProtocol
  (-id [source] (.-id source)))

(defn id [source]
  (-id source))