(ns fogus.svedn.parse
  (:require [clojure.string   :as string]
            [clojure.edn      :as edn]
            [clojure.spec     :as s]))

(defn one-or-many [raw]
  (let [str (string/trim raw)]
    (if (= \# (first str))
      (edn/read-string raw)
      #{str})))
