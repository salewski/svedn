(ns fogus.svedn.core
  (:require [fogus.svedn.q     :as query]
            [fogus.svedn.specs :as specs]
            [clojure.data.csv  :as csv]
            [clojure.java.io   :as io]
            [clojure.edn       :as edn]
            [clojure.string    :as string]
            [clojure.spec      :as s])
  (:refer-clojure :exclude [read]))

(defprotocol SvednReadn
  (-read-svedn [this header-fn conformers validator]))

(defn ^:private nilify [str]
  (if (empty? str) nil str))

(defn ^:private rowify [headers data]
  (apply hash-map (interleave headers (map nilify data))))

(defn ^:private tableify [header-transformer [head & data]]
  (let [headers (map header-transformer head)]
    (set (map #(rowify headers %) data))))

(defn ^:private entityify [conformers thing]
  (reduce (fn [entity [key fun]]
            (if-let [val (get entity key)]
              (update-in entity [key] fun)
              (dissoc entity key)))
          thing
          conformers))

(defn ^:private -read-repr [source]
  (with-open [in-file (io/reader source)]
    (doall
     (csv/read-csv in-file :separator \, :quote \"))))

(defn ^:private -read-svedn-impl [header-fn conformers validator repr]
  (let [result (->> repr 
                    (tableify header-fn)
                    (map #(entityify conformers %))       
                    set)]
    (validator result)))

(extend-protocol SvednReadn
  String
  (-read-svedn [source header-fn conformers validator]
    (-read-svedn-impl header-fn conformers validator (-read-repr source)))

  java.io.Reader
  (-read-svedn [source header-fn conformers validator]
    (-read-svedn-impl header-fn conformers validator (-read-repr source)))

  java.io.PushbackReader
  (-read-svedn [source header-fn conformers validator]
    (-read-svedn-impl header-fn conformers validator (-read-repr source))))

(def ^:private DEFAULT_OPTS {:header-transformer edn/read-string
                              :validator identity})

(defn read [source & {:as opts}]
  (let [config (merge DEFAULT_OPTS opts)]
    (-> source
        (-read-svedn 
         (:header-transformer config)
         (:conformers config)
         (:validator config)))))

(comment

  (->> (read "./samples/books.csv"
             :conformers          
             {:book/genre      edn/read-string
              :personal/rating edn/read-string
              :personal/genre  edn/read-string
              :book/author     specs/parse-one-or-many})
       (query/has-multiple :book/author))

  (s/conform (specs/one-or-many (s/or :key keyword? :integer int?)) "#{:a 1 :b 4}")

)
