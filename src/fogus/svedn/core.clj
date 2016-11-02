(ns fogus.sveden.core
  (:require [fogus.sveden.q   :as query]
            [clojure.data.csv :as csv]
            [clojure.java.io  :as io]
            [clojure.edn      :as edn]
            [clojure.string   :as string]))

(defprotocol SvednReadn
  (-read-svedn [this transformers]))

(defn ^:private nilify [str]
  (if (empty? str) nil str))

(defn ^:private rowify [headers data]
  (apply hash-map (interleave headers (map nilify data))))

(defn ^:private tableify [[head & data]]
  (let [headers (map edn/read-string head)]
    (set (map #(rowify headers %) data))))

(defn ^:private entityify [transformers thing]
  (reduce (fn [entity [key fun]]
            (if-let [val (get entity key)]
              (update-in entity [key] fun)
              (dissoc entity key)))
          thing
          transformers))

(defn ^:private read-one-or-many [raw]
  (let [str (string/trim raw)]
    (if (= \# (first str))
      (edn/read-string raw)
      str)))

(defn ^:private -read-repr [source]
  (with-open [in-file (io/reader source)]
    (doall
     (csv/read-csv in-file :separator \, :quote \"))))

(defn ^:private -read-svedn-impl [transformers repr]
  (->> repr 
       tableify
       (map #(entityify transformers %))       
       set))

(extend-protocol SvednReadn
  String
  (-read-svedn [source transformers]
    (-read-svedn-impl transformers (-read-repr source)))

  java.io.Reader
  (-read-svedn [source transformers]
    (-read-svedn-impl transformers (-read-repr source)))

  java.io.PushbackReader
  (-read-svedn [source transformers]
    (-read-svedn-impl transformers (-read-repr source))))

(comment

  (-> "./samples/books.csv"
      (-read-svedn {:book/genre      edn/read-string
                    :personal/rating edn/read-string
                    :personal/genre  edn/read-string
                    :book/author     read-one-or-many}) 
      (->> (query/has-multiple :book/author)))

)