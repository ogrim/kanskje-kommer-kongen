(defproject kanskje-kommer-kongen "0.1.0-SNAPSHOT"
  :description "Hvor er kongen?"
  :url "https://github.com/ogrim/kanskje-kommer-kongen"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/java.jdbc "0.2.1"]
                 [clj-time "0.4.2"]
                 [org.clojars.ogrim/korma "0.3.0-beta10"]
                 [org.xerial/sqlite-jdbc "3.7.2"]
                 [org.clojars.doo/postal "1.8-SNAPSHOT"]
                 [overtone/at-at "1.0.0"]
                 [clj-logging-config "1.9.7"]
                 [enlive "1.0.0"]
                 [net.cgrand/moustache "1.1.0"]
                 [ring/ring-core "1.1.0"]
                 [ring/ring-servlet "1.1.0"]
                 [ring/ring-jetty-adapter "1.1.0"]]
  :main kanskje-kommer-kongen.app)
