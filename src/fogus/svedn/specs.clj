(ns fogus.svedn.specs
  (:require [clojure.string   :as string]
            [clojure.edn      :as edn]
            [clojure.spec     :as s]))

;; TODO: This is fragile
(defn parse-one-or-many [raw]
  (if (string? raw)
    (let [str (string/trim raw)]
      (if (= \# (first str))
        (edn/read-string raw)
        #{(edn/read-string str)}))
    #{}))

(defmacro one-or-more [typer]
  `(s/conformer (fn [raw#]
                  (let [things# (parse-one-or-many raw#)
                        cspec#  (s/coll-of ~typer :kind set?)]
                    (s/conform cspec# things#)))))

(def enumeration? (s/conformer (fn [raw]
                                 (let [enum (if (string? raw) (edn/read-string raw) raw)
                                       val (s/conform keyword? enum)]
                                   (if (= :clojure.spec/invalid val)
                                     val
                                     (if (and (name val) (namespace val))
                                       val
                                       :clojure.spec/invalid))))))

(comment
  (s/conform (one-or-more int?) ":a")

  (s/conform enumeration? ":a.c/b")
)
