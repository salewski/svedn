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

(defmacro set-of [typer]
  `(s/conformer (fn [raw#]
                  (let [things# (parse-one-or-many raw#)
                        cspec#  (s/coll-of ~typer :kind set?)]
                    (s/conform cspec# things#)))))

(def enumeration (s/conformer (fn [raw]
                                 (let [enum (if (string? raw) (edn/read-string raw) raw)
                                       val (s/conform keyword? enum)]
                                   (if (= :clojure.spec/invalid val)
                                     val
                                     (if (and (name val) (namespace val))
                                       val
                                       :clojure.spec/invalid))))))

(defmacro numeric-of [typer]
  `(s/conformer (fn [raw#]
                  (let [thing# (if (string? raw#) (edn/read-string raw#) raw#)]
                    (s/conform (s/and number? ~typer) thing#)))))

(def numeric
  (s/conformer (fn [raw]
                 (let [thing (if (string? raw) (edn/read-string raw) raw)]
                   (s/conform number? thing)))))

(defn required [conf]
  (s/conformer (fn [thing]
                 (let [val (s/conform conf thing)]
                   (if (and (not= :clojure.spec/invalid val) (empty? val))
                     :clojure.spec/invalid
                     val)))))

(comment
  (s/conform enumeration ":a.c/b")

  (s/conform (s/or :int int? :ratio ratio?) 1/2)

  (s/conform (numeric-of float?) 2.1)
)
