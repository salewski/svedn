(ns fogus.svedn
  (:require [fogus.tafl.q          :as query]
            [fogus.svedn.conformers :as c]
            [clojure.data.csv       :as csv]
            [clojure.java.io        :as io]
            [clojure.edn            :as edn]
            [clojure.string         :as string]
            [clojure.spec.alpha     :as s]
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

  (def confs {:card/name       string?
              :card/phase      string?
              :card/population c/numeric
              :card/military   c/numeric
              :card/diplomatic c/numeric
              :card/economic   c/numeric})

  (->> (read "./samples/supernova.csv"
             :conformers confs
             :whitelist  (-> confs keys set))
       (query/on-value #(= % "Booster Engines")))

  (defn rank-merge [l r]
    (+ l ))

  (defn spit-html [filename table scores]
    (let []
      (spit filename "<html><head><meta charset=\"UTF-8\"><title>Friend Ranks</title></head><body>")

      (spit filename "<table>" :append true)
      (doseq [[game score] scores]
        (spit filename "<tr>" :append true)
        (spit filename "<td>" :append true)
        (spit filename score :append true)
        (spit filename "</td>" :append true)
        (spit filename "<td>" :append true)
        (spit filename game :append true)
        (spit filename "</td>" :append true)
        (spit filename "</tr>" :append true))
      (spit filename "</table>" :append true)

      (spit filename "</body></html>" :append true)))

  (def C (s/conformer #(if (.startsWith % "18") "18xx" %)))

  (def table (read "./samples/ranks.csv"
                   :conformers {1 C 2 C 3 C 4 C 5 C 6 C 7 C 8 C 9 C 10 C}
                   :whitelist  #{:person/name 1 2 3 4 5 6 7 8 9 10}))

  (defn update-values [m f & args]
    (reduce (fn [r [k v]] 
              (assoc r k (apply f v args))) 
            {} 
            m))

  (->> table
       (map #(dissoc % :person/name))
       (map clojure.set/map-invert)
       (map (fn [m] (update-values m #(- 11 %))))
       (apply merge-with +)
       (#(dissoc % nil))
       seq
       (sort-by second >)
       (spit-html "scores.html" table))

)


(comment
(require '[clojure.set :as set])


(def info
  [{:a 1
    :b 2}
   {:a 1
    :b 4}
   {:a 4
    :b 4}])


(def db (reduce
         (fn [db fact]
           (merge-with into db
                       (set/index [fact] [:entity])
                       (set/index [fact] [:entity :attribute])
                       (set/index [fact] [:entity :attribute :value])
                       (set/index [fact] [:attribute])
                       (set/index [fact] [:attribute :value])
                       (set/index [fact] [:value])))
         {}
         (for [m info
               :let [id (java.util.UUID/randomUUID)]
               [k v] m]
           {:entity id
            :attribute k
            :value v})))

(for [{:keys [entity attribute value]} (get db {:attribute :a})
      {v2 :value} (get db {:entity entity :attribute :b})
      :when (= v2 value)]
  (into {} (map (juxt :attribute :value) (get db {:entity entity}))))

)
