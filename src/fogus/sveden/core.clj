(ns fogus.sveden.core
  (:require [clojure.data.csv :as csv]
            [clojure.java.io  :as io]
            [clojure.edn      :as edn]))

(defn ^:private entityify [[head & data]]
  (let [headers (map edn/read-string head)]
    (map #(apply hash-map (interleave headers %)) data)))

(comment

  (def d
    (with-open [in-file (io/reader "./samples/books.csv")]
      (doall
       (csv/read-csv in-file))))

(entityify d)



)
