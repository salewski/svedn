(ns fogus.svedn.specs
  (:require [clojure.spec :as s]))

(s/def ::conformers-map map?)
(s/def ::conformers ::conformers-map)
(s/def ::whitelist  (s/coll-of (s/or :kw keyword? :num number?) :distinct true :into #{}))
(s/def ::metadata keyword?)
(s/def ::amendments keyword?)

(s/def ::svedn-opts (s/keys :req-un [::conformers ::whitelist]
                            :opt-un [::metadata ::amendments]))

