(ns fogus.svedn
  (:require [fogus.svedn.q          :as query]
            [fogus.svedn.conformers :as c]
            [clojure.data.csv       :as csv]
            [clojure.java.io        :as io]
            [clojure.edn            :as edn]
            [clojure.string         :as string]
            [clojure.spec           :as s])
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
  (reduce (fn [entity [key confrm]]
            (if-let [val (get entity key)]
              (update-in entity [key] #(s/conform confrm %))
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

(defn ^:private process-column [source data f]
  (if data
    (f source data)
    source))

(defn read [source & {:as opts}]
  (let [config (merge DEFAULT_OPTS opts)
        preproc (-> source
                    (-read-svedn 
                     (:header-transformer config)
                     (:conformers config)
                     (:validator config)))
        whitelist (:whitelist opts)]
    (->> preproc
         (map (fn [entry]
                (let [filtered (select-keys entry whitelist)]
                  (if (seq filtered)
                    (-> filtered
                        (process-column (get entry (:metadata opts)) #(with-meta %1 (edn/read-string %2)))
                        (process-column (get entry (:amendments opts)) #(merge %1 (edn/read-string %2))))
                    nil))))
         (keep identity)
         set)))

(comment
  (let [confs {:book/genre      c/enumeration
               :personal/rating c/numeric
               :personal/genre  c/enumeration
               :book/author     (c/set-of string?)}]
    
    (->> (read "./samples/books.csv"
               :conformers confs
               :whitelist  (-> confs keys set (conj :book/title))
               :metadata   :book/meta
               :amendments :book/amendments)
;;      (query/has-multiple :book/author)
      (query/on-value #(= % "Magister Ludi"))
      ))

  (->> (read "./samples/books.csv"
             :conformers          
             {:book/genre      c/enumeration
              :personal/rating c/numeric
              :personal/genre  c/enumeration
              :book/author     (c/required (c/set-of string?))})
       (query/on-value (query/partial-enum :fiction.philosophy))
       ;;(query/on-value #(= % "Grendel"))
       ;;(query/on-value #(= % :clojure.spec/invalid))
  )

  (->> (read "./samples/euros.csv"
             :conformers          
             {:game/category   c/enumeration
              :published/year  c/numeric
              :bgg/id          c/numeric
              :meta/note       string?
              :game/tag        (c/set-of c/enumeration)
              :game/designer   (c/required (c/set-of string?))})
       ;;(query/on-value (query/partial-enum :post-euro))
       ;;(query/on-value #(= % "Five Tribes"))
       (query/on-value :game/tag #(= % :tag/euro-abstract))
       ;;(query/on-value #(= % :clojure.spec/invalid))
  )
) 
