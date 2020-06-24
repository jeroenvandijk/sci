(ns sci.macros
  (:require [cljs.compiler :as comp]
            [cljs.analyzer :as ana]
            [cljs.util :as util]
            [cljs.analyzer.api :as ana-api]))

(alias 'core 'clojure.core)

(defn make-inspectable-1 [x]
  `(aset (.-prototype ~x) "inspect"
     (fn []
       (~'this-as coll#
         (.toString coll#)))))

(defmacro make-inspectable [& xs]
  `(do ~@(map make-inspectable-1 xs)))

(defmacro sci-export [coref]
  (let [{:keys [ns name arglists]} (ana-api/resolve &env coref)
        arglists (cond-> arglists
                   (= (first arglists) 'quote) rest)]
    (letfn [(export-method [arglist]
              (let [c (count arglist)
                    sym (symbol (str ns)
                                (str (core/name name) ".cljs$core$IFn$_invoke$arity$" c))]
                `(js/goog.exportSymbol
                   ~(str sym)
                   ~sym)))]
      `(do
         (js/goog.exportSymbol ~(str coref) ~coref) ~(list 'js* ";")
         ~@(when (and arglists (< 1 (count arglists)))
             (map export-method (remove #(some '#{&} %) arglists)))))))
