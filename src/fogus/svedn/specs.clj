(ns fogus.svedn.specs
  (:require [clojure.string   :as string]
            [clojure.edn      :as edn]
            [clojure.spec     :as s]))

(defn parse-one-or-many [raw]
  (if (string? raw)
    (let [str (string/trim raw)
          val (edn/read-string str)]
      (if (set? val)
        val
        (if (symbol? val)
          #{str}
          #{val})))))

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
  (parse-one-or-many "a")

  (s/conform (one-or-more string?) "a")

  (s/conform enumeration? ":a.c/b")
)
