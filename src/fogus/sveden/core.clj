(ns fogus.sveden.core
  (:require [clojure.data.csv :as csv]
            [clojure.java.io  :as io]
            [clojure.edn      :as edn]))

(defprotocol SvednReadn
  (read-svedn [this source]))

(defn ^:private nilify [str]
  (if (empty? str)
    nil
    str))

(defn ^:private entityify [[head & data]]
  (let [headers (map edn/read-string head)]
    (map #(apply hash-map (interleave headers (map nilify %))) data)))



(comment

  (def d
    (with-open [in-file (io/reader "./samples/books.csv")]
      (doall
       (csv/read-csv in-file))))

  (first (entityify d))

)
