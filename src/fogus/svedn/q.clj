(ns fogus.svedn.q)

(defn has-multiple [key table]
  (clojure.set/select (fn [entity] 
                        (let [cell (get entity key)] 
                          (and (instance? java.util.Set cell)
                               (< 1 (count cell)))))
                      table))

(defn has-invalid 
  [key table]
   (clojure.set/select (fn [entity] 
                         (let [cell (get entity key)] 
                           (= :clojure.spec/invalid cell)))
                       table))

(defn ^:private keys-with-val [val]
  (fn [entry]
    (reduce-kv (fn [acc k v]
                 (if (= v val)
                   (conj acc k)
                   acc))
               #{}
               entry)))

;; TODO: make a 3-arity version that takes a key also
(defn on-value [val table]
  (->> (seq table)
       (map (keys-with-val val))
       (map (fn [entry ks]
              (when (seq ks) entry)) 
            table)
       (filter identity)
       set))

