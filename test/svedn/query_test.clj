(ns svedn.query-test
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

(deftest test-on-value
  (let [data (svedn/read "./samples/books.csv"
                         :conformers CONF
                         :whitelist  (-> CONF keys set (conj :book/title))
                         :metadata   :book/meta
                         :amendments :book/amendments)]
    (is (not= #{} (query/on-value #(= % "Magister Ludi") data)))
    (is (not= #{} (query/on-value :book/title #(= % "House of Leaves") data)))
    (is (not= #{} (query/on-value (query/partial-enum :fiction.philosophy) data)))
    (is (= #{} (query/on-value #(= % :clojure.spec/invalid) data)))))

(deftest test-partial-enum
  (let [philos? (query/partial-enum :philosophy)
        f-philos? (query/partial-enum :fiction.philosophy)]
    (is (philos? :fiction.philosophy))
    (is (philos? :philosophy/existentialism))
    (is (f-philos? :fiction.philosophy/slice-of-life))
    (is (f-philos? :fiction.philosophy))
    (is (not (philos? :fiction.found-drama)))))
