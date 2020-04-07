(ns oxsevenbee.screeps.timeout-test
  (:require [clojure.test :as t]
            [oxsevenbee.screeps.timeout :as sut]))

(def start-tick 0)

(defn make-timeout-sut [start-tick]
  (let [called (atom 0)
        fun    (fn [] (swap! called inc))
        timeout (sut/make-timeout start-tick)]
    {:called called :fun fun :timeout timeout}))

(t/deftest timeout-call-same-tick
  (let [{:keys [called fun timeout]} (make-timeout-sut start-tick)]
    (sut/setTimeout timeout fun 0)
    (sut/new-tick timeout start-tick)
    (t/is (= 1 @called))))

(t/deftest timeout-call
  (let [{:keys [called fun timeout]} (make-timeout-sut start-tick)]
    (sut/setTimeout timeout fun 1)
    (sut/new-tick timeout (+ start-tick 1))
    (t/is (= 1 @called))))

(t/deftest timeout-call-on-missed-tick
  (let [{:keys [called fun timeout]} (make-timeout-sut start-tick)]
    (sut/setTimeout timeout fun 1)
    (sut/new-tick timeout (+ start-tick 2))
    (t/is (= 1 @called))))

(t/deftest timeout-remove-after-call
  (let [{:keys [called fun timeout]} (make-timeout-sut start-tick)]
    (sut/setTimeout timeout fun 1)
    (sut/new-tick timeout 1)
    (sut/new-tick timeout 2)
    (t/is (= 1 @called))))

(t/deftest timeout-dont-call-before
  (let [{:keys [called fun timeout]} (make-timeout-sut start-tick)]
    (sut/setTimeout timeout fun 5)
    (doseq [t (range 5)]
      (sut/new-tick timeout (+ start-tick t)))
    (t/is (= 0 @called))
    (sut/new-tick timeout (+ start-tick 5))
    (t/is (= 1 @called))))

(t/run-tests)
