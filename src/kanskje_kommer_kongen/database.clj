(ns kanskje-kommer-kongen.database
  (:require [clojure.java.jdbc :as sql]
            [clj-time [core :as t] [coerce :as c] [local :as l]])
  (:use [korma core db])
  (:import [org.joda.time ReadableDateTime]))

(def ^:private dbspec
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "kongen.db"})

(defdb db dbspec)

(defn- create-tables []
  (do
    (sql/create-table "program"
                      [:nth :int]
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
  (entity-fields :nth :tittel :tekst :dato :klokkeslett :fra :til))

(defn- next-nth []
  (-> (select program
        (fields :nth)
        (order :nth :DESC)
        (limit 1))
      first :nth inc))

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
      (insert program (values {:nth (next-nth)
                               :tittel tittel
                               :tekst tekst
                               :dato sql-dato
                               :klokkeslett klokkeslett
                               :fra fra
                               :til til})))))

(defn- local-date []
  (let [dt (l/local-now)]
    (t/date-time (t/year dt) (t/month dt) (t/day dt))))

(defn program-dato
  ([] (program-dato (local-date)))
  ([#^ReadableDateTime dato] (select program (where (= :dato (c/to-timestamp dato)))))
  ([y m d] (program-dato (t/date-time y m d))))

(defn program-fra
  ([] (program-fra (local-date)))
  ([#^ReadableDateTime dato] (select program (where (>= :dato (c/to-timestamp dato)))))
  ([y m d] (program-fra (t/date-time y m d))))

(defn program-fra-til
  ([#^ReadableDateTime fra #^ReadableDateTime til]
     (select program (where (and (>= :dato (c/to-timestamp fra))
                                 (<= :dato (c/to-timestamp til))))))
  ([fra-y fra-m fra-d til-y til-m til-d]
     (program-fra-til (t/date-time fra-y fra-m fra-d)
                      (t/date-time til-y til-m til-d))))
