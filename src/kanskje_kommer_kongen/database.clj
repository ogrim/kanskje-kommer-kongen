(ns kanskje-kommer-kongen.database
  (:require [clojure.java.jdbc :as sql]
            [clj-time [core :as t] [coerce :as c]])
  (:use [korma core db]))

(def ^:private dbspec
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "kongen.db"})

(defdb db dbspec)

(defn- create-tables []
  (do
    (sql/create-table "program"
                      [:tittel :text]
                      [:tekst :text]
                      [:dato :datetime]
                      [:klokkeslett :text]
                      [:fra :datetime]
                      [:til :datetime]
                      ["PRIMARY KEY" "(tittel, dato, klokkeslett)"])
    (sql/do-commands "CREATE INDEX idx_program_tittel ON program(tittel)"
                     "CREATE INDEX idx_program_dato ON program(dato)"
                     "CREATE INDEX idx_program_klokkeslett ON program(klokkeslett)")))

(defn- invoke-with-connection [f]
  (sql/with-connection dbspec (sql/transaction (f))))

(try (invoke-with-connection create-tables) (catch Exception _ nil))

(defentity program
  (table :program)
  (entity-fields :tittel :tekst :dato :klokkeslett :fra :til))

(defn- can-insert? [tittel dato klokkeslett]
  (empty? (select program
            (where {:tittel tittel
                    :dato dato
                    :klokkeslett klokkeslett}))))

(defn insert-program [{:keys [dato klokkeslett dato-intervall tittel tekst]}]
  (let [fra (if (nil? dato-intervall) nil (c/to-timestamp (t/start dato-intervall)))
        til (if (nil? dato-intervall) nil (c/to-timestamp (t/end dato-intervall)))
        sql-dato (c/to-timestamp dato)]
    (if (can-insert? tittel sql-dato klokkeslett)
      (insert program (values {:tittel tittel
                               :tekst tekst
                               :dato sql-dato
                               :klokkeslett klokkeslett
                               :fra fra
                               :til til})))))
