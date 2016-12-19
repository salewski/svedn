(ns svedn.specs-test
  (:require [clojure.test :refer :all]
            [fogus.svedn.specs :as specs]
            [clojure.spec :as s]))

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
         (s/conform (spec/required (specs/set-of string?)) "#{}")))

  (is (= #{"a"}
         (s/conform (spec/required (specs/set-of string?)) "#{\"a\"}"))))
