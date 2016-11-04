(ns fogus.svedn.core
  (:require [fogus.svedn.q     :as query]
            [fogus.svedn.parse :as parse]
            [clojure.data.csv  :as csv]
            [clojure.java.io   :as io]
            [clojure.edn       :as edn]
            [clojure.string    :as string]
            [clojure.spec      :as s])
  (:refer-clojure :exclude [read]))

(defprotocol SvednReadn
  (-read-svedn [this header-fn transformers validator]))

(defn ^:private nilify [str]
  (if (empty? str) nil str))

(defn ^:private rowify [headers data]
  (apply hash-map (interleave headers (map nilify data))))

(defn ^:private tableify [header-transformer [head & data]]
  (let [headers (map header-transformer head)]
    (set (map #(rowify headers %) data))))

(defn ^:private entityify [transformers thing]
  (reduce (fn [entity [key fun]]
            (if-let [val (get entity key)]
              (update-in entity [key] fun)
              (dissoc entity key)))
          thing
          transformers))

(defn ^:private -read-repr [source]
  (with-open [in-file (io/reader source)]
    (doall
     (csv/read-csv in-file :separator \, :quote \"))))

(defn ^:private -read-svedn-impl [header-fn transformers validator repr]
  (let [result (->> repr 
                    (tableify header-fn)
                    (map #(entityify transformers %))       
                    set)]
    (validator result)))

(extend-protocol SvednReadn
  String
  (-read-svedn [source header-fn transformers validator]
    (-read-svedn-impl header-fn transformers validator (-read-repr source)))

  java.io.Reader
  (-read-svedn [source header-fn transformers validator]
    (-read-svedn-impl header-fn transformers validator (-read-repr source)))

  java.io.PushbackReader
  (-read-svedn [source header-fn transformers validator]
    (-read-svedn-impl header-fn transformers validator (-read-repr source))))

(def ^:private DEFAULT_OPTS {:header-transformer edn/read-string
                              :validator identity})

(defn read [source & {:as opts}]
  (let [config (merge DEFAULT_OPTS opts)]
    (-> source
        (-read-svedn 
         (:header-transformer config)
         (:transformers config)
         (:validator config)))))

(comment

  (->> (read "./samples/books.csv"
             :transformers          
             {:book/genre      edn/read-string
              :personal/rating edn/read-string
              :personal/genre  edn/read-string
              :book/author     parse/one-or-many})
       (query/has-multiple :book/author))

  (s/describe (s/coll-of string? :kind set?))
)
