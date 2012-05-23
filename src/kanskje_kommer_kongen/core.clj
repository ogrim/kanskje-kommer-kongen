(ns kanskje-kommer-kongen.core
  (:require [kanskje-kommer-kongen.database :as db]
            [net.cgrand.enlive-html :as e]
            [clj-time.core :as t]
            [clojure.string :as str])
  (:import [java.net URL]
           [java.io InputStreamReader]))

(def kalender-url "http://www.kongehuset.no/c26946/kalenderpost/liste.html?sortering=dato&&arkiv=&ar=2012")

(defn get-as [url encoding]
  (-> url URL. .getContent (InputStreamReader. encoding) e/html-resource))

(def titler
  ["DD.MM. Kongen og Dronningen"
   "H.K.H. Kronprinsessen"
   "DD.KK.HH. Kronprinsen og Kronprinsessen"
   "Deres Majesteter Kongen og Dronningen"
   "H.M. Kongen"
   "H.K.H. Kronprinsen"])

(def months
  '{:januar 1
    :februar 2
    :april 3
    :mars 4
    :mai 5
    :juni 6
    :juli 7
    :august 8
    :september 9
    :oktober 10
    :november 11
    :desember 12})

(defn intervall [fra til m year]
  (t/interval
   (t/date-time year (get months (keyword m)) (Integer/parseInt (first (str/split fra #"[.]"))))
   (t/date-time year (get months (keyword m)) (Integer/parseInt (first (str/split til #"[.]"))))))

(defn finn-dato [tekst year]
  (let [datoer (re-seq #"[0-9]{2}[.]?" tekst)]
    (if (and (= (count datoer) 2) (every? #(= (last %) \.) datoer))
      (let [[fra til] datoer
            m (re-find #"\w+" (second (str/split tekst (re-pattern til))))]
        (intervall fra til m year)))))

(defn finn-klokkeslett [tekst]
  (let [klokkeslett (first (re-seq #"[0-9]{2}?[:.]++[0-9]{2}?" tekst))]
    (if (seq klokkeslett) (->> (str/split klokkeslett #"[:.]") (interpose ":") (apply str)))))

(defn parser [node]
  (let [[day month year] (map #(Integer/parseInt %)
                              (-> (e/select node [:div.searchEntryDate])
                                  first
                                  :content
                                  first
                                  (str/split #"[.]")
                                  ))
        tekst (-> (e/select node [:div.searchEntryText]) first :content first)
        tittel (-> (e/select node [:div.searchEntryTitle :b]) first :content first)]
    {:dato (t/date-time year month day)
     :klokkeslett (finn-klokkeslett tekst)
     :dato-intervall (finn-dato tekst year)
     :tittel tittel
     :tekst tekst}))

(defn process []
  (->> (e/select (get-as kalender-url "ISO-8859-1") [:div#middleContent :div.searchEntryMiddle])
       (map (comp db/insert-program parser))))
