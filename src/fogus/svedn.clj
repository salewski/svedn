(ns fogus.svedn
  (:require [fogus.svedn.q          :as query]
            [fogus.svedn.conformers :as c]
            [clojure.data.csv       :as csv]
            [clojure.java.io        :as io]
            [clojure.edn            :as edn]
            [clojure.string         :as string]
            [clojure.spec           :as s]
            fogus.svedn.specs)
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

(defn ^:private process-entity [entity whitelist opts]
  (let [filtered (select-keys entity whitelist)
        proc-meta (fnil with-meta filtered filtered)
        proc-amendments (fnil merge filtered filtered)]
    (if (seq filtered)
      (-> filtered
          (proc-meta (edn/read-string (get entity (:metadata opts) "nil")))
          (proc-amendments (edn/read-string (get entity (:amendments opts) "nil"))))
      nil)))

(defn read [source & {:as opts}]
  (if (not (s/valid? :fogus.svedn.specs/svedn-opts opts))
    (throw (ex-info "Improper options." 
                    {:explaination-map (s/explain-data :fogus.svedn.specs/svedn-opts opts)
                     :explaination-str (s/explain-str :fogus.svedn.specs/svedn-opts opts)
                     :opts opts}))
    (let [config (merge DEFAULT_OPTS opts)       
          preproc (-> source
                      (-read-svedn 
                       (:header-transformer config)
                       (:conformers config)
                       (:validator config)))]
      (->> preproc
           (map #(process-entity % (:whitelist opts) opts))
           (keep identity)
           set))))

(comment
  (def confs {:book/genre      c/enumeration
               :personal/rating c/numeric
               :personal/genre  c/enumeration
               :book/author     (c/set-of string?)})

  (->> (read "./samples/books.csv"
             :conformers confs
             :whitelist  (-> confs keys set (conj :book/title))
             :metadata   :book/meta
             :amendments :book/amendments)    
       (query/on-value #(= % "Magister Ludi")))
) 
