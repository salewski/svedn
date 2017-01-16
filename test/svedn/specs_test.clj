(ns svedn.specs-test
  (:require [clojure.test :refer :all]
            [fogus.svedn.conformers :as c]
            [clojure.spec :as s]))

(deftest test-enumeration
  ""
  (is (= :a.c/b
         (s/conform c/enumeration ":a.c/b")))

  (is (= :clojure.spec/invalid
         (s/conform c/enumeration ":a"))))

(deftest test-numeric-of
  ""
  (is (= 2.1
         (s/conform (c/numeric-of float?) 2.1)))


  (is (= 1.2
         (s/conform (c/numeric-of float?) "1.2")))

  (is (= :clojure.spec/invalid
         (s/conform (c/numeric-of float?) "a"))))

(deftest test-set-of
  ""
  (is (= #{[:integer 1] [:key :a] [:integer 4] [:key :b]}
         (s/conform (c/set-of (s/or :key keyword? :integer int?)) "#{:a 1 :b 4}")))

  (is (= #{[:key :a]}
         (s/conform (c/set-of (s/or :key keyword? :integer int?)) "#{:a}")))

  (is (= #{}
         (s/conform (c/set-of (s/or :key keyword? :integer int?)) "#{}")))

  (is (= #{:a/b :b/c}
         (s/conform (c/set-of c/enumeration) "#{:a/b :b/c}")))

  (is (= :clojure.spec/invalid
         (s/conform (c/set-of string?) "[\"a\"]"))))

(deftest test-required
  ""
  (is (= :clojure.spec/invalid
         (s/conform (c/required (c/set-of string?)) "#{}")))

  (is (= #{"a"}
         (s/conform (c/required (c/set-of string?)) "#{\"a\"}"))))

