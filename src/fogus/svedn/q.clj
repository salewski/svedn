(ns fogus.svedn.q)

(defn has-multiple [key table]
  (clojure.set/select (fn [entity] 
                        (let [cell (get entity key)] 
                          (and (instance? java.util.Set cell)
                               (< 1 (count cell)))))
                      table))

(defn has-invalid [key table]
  (clojure.set/select (fn [entity] 
                        (let [cell (get entity key)] 
                          (= :clojure.spec/invalid cell)))
                      table))
