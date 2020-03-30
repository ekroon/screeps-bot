(ns oxsevenbee.utils
  (:require-macros [oxsevenbee.utils]))

(defprotocol Lifted
  (lifted [this]
          "Returns the object that was lifted using lift-on."))