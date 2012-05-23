(ns kanskje-kommer-kongen.core-test
  (:use clojure.test
        kanskje-kommer-kongen.core
        kanskje-kommer-kongen.test-data)
  (:require [clj-time.core :as t]))



(deftest datoer
  (testing "Finn datoer i tekst"
    (are [node result] (= node result)
         (finn-dato "H.K.H. Kronprinsessen deltar på the Young Global Leaders' Seattle Summit (21. - 23. mai)." 2012)
         (t/interval (t/date-time 2012 5 21) (t/date-time 2012 5 23))
         (finn-dato "Fylkestur til Møre og Romsdal: DD.KK.HH. Kronprinsen og Kronprinsessen ankommer Geiranger i Stranda kommune og reiser til utsiktspunktet Flydalsjuvet (09.00)." 2012)
         nil
         (finn-dato "DD.KK.HH. Kronprinsen og Kronprinsessen besøker Møre og Romsdal 24. - 26. mai." 2012)
         (t/interval (t/date-time 2012 5 24) (t/date-time 2012 5 26)))))

(deftest klokkeslett
  (testing "Finn klokkeslett i tekst"
    (are [node result] (= node result)
         (finn-klokkeslett "Fylkestur til Møre og Romsdal: DD.KK.HH. Kronprinsen og Kronprinsessen ankommer Geiranger i Stranda kommune og reiser til utsiktspunktet Flydalsjuvet (09.00).")
         "09:00"
         (finn-klokkeslett "Fylkestur til Møre og Romsdal: DD.KK.HH. Kronprinsen og Kronprinsessen besøker Hadartun sykehjem (18:35).")
         "18:35")))
