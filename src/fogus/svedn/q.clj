(ns fogus.svedn.q)

(defn has-multiple [key table]
  (clojure.set/select #(instance? java.util.Set (get % key))
                      table))
