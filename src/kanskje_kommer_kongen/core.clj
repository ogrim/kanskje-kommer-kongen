(ns kanskje-kommer-kongen.core
  (:require [kanskje-kommer-kongen.database :as db]
            [net.cgrand.enlive-html :as e]
            [clj-time.core :as t]
            [clojure.string :as str])
  (:import [java.net URL]
           [java.io InputStreamReader]))

(def ^:private kalender-url "http://www.kongehuset.no/c26946/kalenderpost/liste.html?sortering=dato&&arkiv=&ar=2012")

(defn- get-as [url encoding]
  (-> url URL. .getContent (InputStreamReader. encoding) e/html-resource))

(def ^:private titler
  {"DD.MM. Kongen og Dronningen" [:kongen :dronningen]
   "H.K.H. Kronprinsessen" [:kronprinsessen]
   "DD.KK.HH. Kronprinsen og Kronprinsessen" [:kronprinsen :kronprinsessen]
   "Deres Majesteter Kongen og Dronningen" [:kongen :dronningen]
   "H.M. Kongen" [:kongen]
   "H.K.H. Kronprinsen" [:kronprinsen]
   "H.M. Dronningen" [:dronningen]})

(def ^:private months
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

(defn- intervall [fra til m year]
  (t/interval
   (t/date-time year (get months (keyword m)) (-> fra (str/split #"[.]") first Integer/parseInt))
   (t/date-time year (get months (keyword m)) (-> til (str/split #"[.]") first Integer/parseInt))))

(defn- finn-dato [tekst year]
  (let [datoer (re-seq #"[0-9]{2}[.]?" tekst)]
    (if (and (= (count datoer) 2) (every? (comp (partial = \.) last) datoer))
      (let [[fra til] datoer
            m (re-find #"\w+" (->> til re-pattern (str/split tekst) second))]
        (intervall fra til m year)))))

(defn- finn-klokkeslett [tekst]
  (let [klokkeslett (first (re-seq #"[0-9]{2}?[:.]++[0-9]{2}?" tekst))]
    (if (seq klokkeslett) (->> (str/split klokkeslett #"[:.]") (interpose ":") (apply str)))))

(defn- parser [node]
  (let [[day month year] (map #(Integer/parseInt %)
                              (-> (e/select node [:div.searchEntryDate])
                                  first
                                  :content
                                  first
                                  (str/split #"[.]")))
        tekst (-> (e/select node [:div.searchEntryText]) first :content first)
        tittel (-> (e/select node [:div.searchEntryTitle :b]) first :content first)]
    {:dato (t/date-time year month day)
     :klokkeslett (finn-klokkeslett tekst)
     :dato-intervall (finn-dato tekst year)
     :tittel tittel
     :tekst tekst}))

(defn- sett-inn-nye []
  (->> (e/select (get-as kalender-url "ISO-8859-1") [:div#middleContent :div.searchEntryMiddle])
       (map (comp db/insert-program parser))))

(defn process []
  (let [nye (->> (sett-inn-nye) (remove nil?) count)]
    (cond (= nye 0) "Ingen nye hendelser"
          (= nye 1) "1 ny hendelse"
          :else  (str nye " nye hendelser"))))

(defn- deltakere [{:keys [tekst]}]
  (->> (keys titler)
       (map #(re-seq (re-pattern %) tekst))
       (remove nil?)
       (mapcat #(get titler (first %)))
       distinct))

(defn- free-search [{:keys [tekst]} s]
  (if (re-seq (re-pattern (.toLowerCase s)) (.toLowerCase tekst)) true false))
