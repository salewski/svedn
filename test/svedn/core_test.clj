(ns svedn.core-test
  (:require [clojure.test :refer :all]
            [fogus.svedn :as svedn]
            [fogus.svedn.conformers :as c]
            [fogus.svedn.q :as query]))

(defn ^:private vals-for [key table]
  (filter identity (map key table)))

(defn ^:private absolutely-every? [pred coll]
  (and (seq coll)
       (every? pred coll)))

(def CONF {:book/genre      c/enumeration
           :personal/rating c/numeric
           :personal/genre  c/enumeration
           :book/author     (c/set-of string?)})

(deftest test-whitelist
  (let [data (svedn/read "./samples/books.csv")]
    (is (= #{} data))))

(deftest test-conformers
  (let [data (svedn/read "./samples/books.csv"
                         :conformers CONF
                         :whitelist  (set (keys CONF)))]
    (is (absolutely-every? number?  (vals-for :personal/rating data)))
    (is (absolutely-every? keyword? (vals-for :personal/genre  data)))
    (is (absolutely-every? keyword? (vals-for :book/genre      data)))
    (is (absolutely-every? set?     (vals-for :book/author     data)))))

