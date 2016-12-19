(ns svedn.specs-test
  (:require [clojure.test :refer :all]
            [fogus.svedn.specs :as specs]
            [clojure.spec :as s]))

(deftest test-enumeration
  ""
  (is (= :a.c/b
         (s/conform specs/enumeration ":a.c/b")))

  (is (= :clojure.spec/invalid
         (s/conform specs/enumeration ":a"))))

(deftest test-numeric-of
  ""
  (is (= 2.1
         (s/conform (specs/numeric-of float?) 2.1)))


  (is (= 1.2
         (s/conform (specs/numeric-of float?) "1.2")))

  (is (= :clojure.spec/invalid
         (s/conform (specs/numeric-of float?) "a"))))

(deftest test-set-of
  ""
  (is (= #{[:integer 1] [:key :a] [:integer 4] [:key :b]}
         (s/conform (specs/set-of (s/or :key keyword? :integer int?)) "#{:a 1 :b 4}")))

  (is (= #{[:key :a]}
         (s/conform (specs/set-of (s/or :key keyword? :integer int?)) "#{:a}")))

  (is (= #{}
         (s/conform (specs/set-of (s/or :key keyword? :integer int?)) "#{}")))

  (is (= #{:a/b :b/c}
         (s/conform (specs/set-of specs/enumeration) "#{:a/b :b/c}")))

  (is (= :clojure.spec/invalid
         (s/conform (specs/set-of string?) "[\"a\"]"))))

(deftest test-required
  ""
  (is (= :clojure.spec/invalid
         (s/conform (specs/required (specs/set-of string?)) "#{}")))

  (is (= #{"a"}
         (s/conform (specs/required (specs/set-of string?)) "#{\"a\"}"))))

