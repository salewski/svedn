(ns fogus.sveden.core
  (:require [clojure.data.csv :as csv]
            [clojure.java.io  :as io]
            [clojure.edn      :as edn]))

(defprotocol SvednReadn
  (read-svedn [this source]))

(defn ^:private nilify [str]
  (if (empty? str) nil str))

(defn ^:private rowify [headers data]
  (apply hash-map (interleave headers (map nilify data))))

(defn ^:private tableify [[head & data]]
  (let [headers (map edn/read-string head)]
    (set (map #(rowify headers %) data))))



(comment

  (def d
    (with-open [in-file (io/reader "./samples/books.csv")]
      (doall
       (csv/read-csv in-file))))

  (type (tableify d))

)
