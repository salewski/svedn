(ns svedn.core-test
  (:require [clojure.test :refer :all]
            [fogus.svedn :as svedn]
            [fogus.svedn.specs :as specs]
            [fogus.svedn.q :as query]))

(defn ^:private vals-for [key table]
  (filter identity (map key table)))

(deftest test-conformers
  (let [data (svedn/read "./samples/books.csv"
                         :conformers          
                         {:book/genre      specs/enumeration
                          :personal/rating specs/numeric
                          :personal/genre  specs/enumeration
                          :book/author     (specs/set-of string?)})]
    (is (every? number?  (vals-for :personal/rating data)))
    (is (every? keyword? (vals-for :personal/genre  data)))
    (is (every? keyword? (vals-for :book/genre      data)))
    (is (every? set?     (vals-for :book/author     data)))))

