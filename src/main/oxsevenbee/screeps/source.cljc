(ns oxsevenbee.screeps.source
  (:require [oxsevenbee.utils :refer [lifted lift-on lift-as]]
            [goog.object :as go]
            [cljs-bean.core :refer [->js ->clj]]))

(defn -id [^js/Source source]
  (.-id source))

(lift-as SourceProtocol)