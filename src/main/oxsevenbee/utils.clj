(ns oxsevenbee.utils
  (:require [cljs.analyzer.api :as api]))

(defmacro ns-info []
  (let [res (api/ns-interns (symbol (str *ns*)))]
    res))

(defmacro lift-as
  "Lift the functions in the current namespace, which names are prefixed
  with the - character, into a protocol with the given name, where the
  prefix is stripped from the protocol function names.

  For example:

  (defn -incr
    \"Incrementer\"
    ([this] (-incr this 1))
    ([this n] (+ this n)))

  (lift-as Incrementer)

  The lift-as macro above would be evaluated to:

  (defprotocol Incrementer
    (incr [this] \"Incrementer\")
    (incr [this n] \"Incrementer\")"
  [name]
  `(do
     (defprotocol ~name
       ~@(for [[s v] (api/ns-interns (symbol (str *ns*)))
               :let [fname (str s)]
               :when (and (= (first fname) \-) (not (:protocols v)))
               arglist (:method-params v)]
           (list (symbol (subs fname 1))
                 arglist
                 (:doc v))))
     (def ~(symbol (str "make-" name))
       (with-meta (fn [~'o] (lift-on ~name ~'o))
                  {:generated true}))))


(defmacro lift-on
  "Lift the functions in the current namespace, with names prefixed with
  the - character, into a protocol implementation with the given name,
  where the prefix is stripped from the protocol function names. The
  protocol implementation calls the prefixed functions, receiving the
  given obj as its first parameter.

  For example:

  (defn -incr
    \"Incrementer\"
    ([this] (-incr this 1))
    ([this n] (+ this n)))

  (lift-as Incrementer)

  (def i (lift-on Incrementer 5))

  The lift-on expression above would be evaluated to:

  (def i
    (let [G__5123 5]
      (reify
        Incrementer
        (incr [this] (-incr G__5123))
        (incr [this n] (-incr G__5123 n))

        Lifted
        (lifted [_] G__5123))

  Thus one could now call:

  (incr i 10)
  ;=> 15

  (lifted i)
  ;=> 5"
  [protocol obj]
  (let [objsym (gensym)]
    `(let [~objsym ~obj]
       (reify ~protocol
         ~@(for [[s v] (api/ns-interns (symbol (str *ns*)))
                 :let [fname (str s)]
                 :when (and (= (first fname) \-) (not (:protocols v)))
                 arglist (:method-params v)]
             (list (symbol (subs fname 1))
                   arglist
                   (concat [s objsym] (rest arglist))))
         Lifted
         (lifted [~'_] ~objsym)))))