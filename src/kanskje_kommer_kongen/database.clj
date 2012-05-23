(ns kanskje-kommer-kongen.database
  (:require [clojure.java.jdbc :as sql]
            [clj-time [core :as t] [coerce :as c]])
  (:use [korma core db]))

(def dbspec
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "kongen.db"})

(defdb db dbspec)

(defn create-tables []
  (do
    (sql/create-table "program"
;                      [:id :int ]
                      [:tittel :text]
                      [:tekst :text]
                      [:dato :datetime]
                      [:klokkeslett :text]
                      [:fra :datetime]
                      [:til :datetime]
                      ["PRIMARY KEY" "(tittel, dato)"])
    (sql/do-commands "CREATE INDEX idx_program_tittel ON program(tittel)"
                     "CREATE INDEX idx_program_dato ON program(dato)")))

(defn invoke-with-connection [f]
  (sql/with-connection dbspec (sql/transaction (f))))

(try (invoke-with-connection create-tables) (catch Exception _ nil))

(defn timestamp []
  (java.sql.Timestamp. (.getTime (java.util.Date.))))

(comment (join query my-ent (and (= :k1 :s.k1)
                         (= :k2 :s.k2))))

(defentity program
  (table :program)
  (entity-fields :tittel :tekst :dato :klokkeslett :fra :til))

(comment (defn insert-program [{:keys [dato klokkeslett dato-intervall tittel tekst]}]
   (let [fra (if (nil? dato-intervall) nil (c/to-timestamp (t/start dato-intervall)))
         til (if (nil? dato-intervall) nil (c/to-timestamp (t/end dato-intervall)))]

     (let [s (sql-only  (insert program (values {:tittel tittel
                                                 :tekst tekst
                                                 :dato (c/to-timestamp dato)
                                                 :klokkeslett klokkeslett
                                                 :fra fra
                                                 :til til})))
           z{:tittel tittel
             :tekst tekst
             :dato (c/to-timestamp dato)
             :klokkeslett klokkeslett
             :fra fra
             :til til} ] [s z])
     )))

(defn insert-program [{:keys [dato klokkeslett dato-intervall tittel tekst]}]
  (let [fra (if (nil? dato-intervall) nil (c/to-timestamp (t/start dato-intervall)))
        til (if (nil? dato-intervall) nil (c/to-timestamp (t/end dato-intervall)))]
    (insert program (values {:tittel tittel
                             :tekst tekst
                             :dato (c/to-timestamp dato)
                             :klokkeslett klokkeslett
                             :fra fra
                             :til til}))))

(comment (defn get-page [url]
   (let [[query] (select websites (where {:url url}))]
     (if (empty? query)
       (do (persist-page url) (get-page url))
       {:html (read-string (:html query))
        :status (:status query)}))))
