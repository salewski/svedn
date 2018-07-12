(defproject fogus/svedn "0.2.0-SNAPSHOT"
  :description "Tools for working with CSV/EDN hybrid data."
  :url "http://www.github.com/fogus/svedn"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/data.csv "0.1.3"]
                 [fogus/tafl "0.1.0"]]

  :profiles {:debug {}
             :dev {:resource-paths ["samples"]
                   :dependencies [[com.datomic/datomic-free "0.9.5404"]]}
             :uberjar {:aot :all}
             :repl {}}
  :repositories [["snapshots" "https://oss.sonatype.org/content/repositories/snapshots/"]]
  :deploy-repositories [["clojars" {:sign-releases false}]])
