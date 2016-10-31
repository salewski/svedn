(ns fogus.sveden.core
  (:require [clojure.data.csv :as csv]
            [clojure.java.io  :as io]))


(comment

  (with-open [in-file (io/reader "./samples/books.csv")]
    (doall
     (csv/read-csv in-file)))


)
