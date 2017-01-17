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
  (let [data0 (svedn/read "./samples/books.csv")
        data1 (svedn/read "./samples/books.csv"
                          :conformers CONF
                          :whitelist  #{:book/author})
        data (svedn/read "./samples/books.csv"
                         :conformers CONF
                         :whitelist  (set (keys CONF)))]
    (is (empty? data0))
    (is (not (empty? data)))
    (is (= #{:book/author}
           (->> data1 (mapcat keys) set)))
    (is (= #{:personal/genre :book/author :book/genre :personal/rating}
           (->> data (mapcat keys) set)))))

(deftest test-conformers
  (let [data (svedn/read "./samples/books.csv"
                         :conformers CONF
                         :whitelist  (set (keys CONF)))]
    (is (absolutely-every? number?  (vals-for :personal/rating data)))
    (is (absolutely-every? keyword? (vals-for :personal/genre  data)))
    (is (absolutely-every? keyword? (vals-for :book/genre      data)))
    (is (absolutely-every? set?     (vals-for :book/author     data)))))

