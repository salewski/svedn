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

(defmacro one-or-many [typer]
  `(s/conformer (fn [raw#]
                  (let [things# (parse-one-or-many raw#)
                        cspec#  (s/coll-of ~typer :kind set?)]
                    (s/conform cspec# things#)))))

(comment
  (s/conform (one-or-many int?) "1")
)
