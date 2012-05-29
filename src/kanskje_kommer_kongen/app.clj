(ns kanskje-kommer-kongen.app
  (:use [kanskje-kommer-kongen.core]
        [overtone.at-at]
        [clj-logging-config.log4j]
        [clojure.tools.logging]))

(def the-pool (mk-pool))

(set-logger! :pattern "%d - %m%n"
             :level :debug
             :out (org.apache.log4j.DailyRollingFileAppender.
                   (org.apache.log4j.EnhancedPatternLayout.
                    org.apache.log4j.EnhancedPatternLayout/TTCC_CONVERSION_PATTERN)
                   "logs/kongen.log"
                   "'.'yyyy-MM"))

(def tfn #(println "Blast from the past!"))

;; (at (+ 1000 (now)) #(println "Blast from the past!") the-pool)


;; (after 1000 tfn the-pool)
;; (every 1000 tfn the-pool)
;; (stop 6 the-pool)
;; the-pool

;; (stop-and-reset-pool! the-pool)
;; (after 1000 #(.thread ))

;; (every 100 #(do (println "sleeping") (Thread/sleep 5000) (println "woke up")) the-pool)

(defn seconds [s]
  (* s 1000))

(defn minutes [m]
  (* m 60 1000))

(defn hours [h]
  (* h 60 60 1000))
