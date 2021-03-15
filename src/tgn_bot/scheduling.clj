(ns tgn-bot.scheduling
  (:require [tgn-bot.acceptance :as acceptance]
            [chime.core :as chime]
            [clojure.tools.logging :as log]
            java-time))

(def standard-task-execution-time
  (->
    (java-time/local-time 9 0)
    (java-time/zoned-date-time (java-time/zone-id "Europe/Amsterdam"))
    java-time/instant))

#_(defn keep-alive-task [time]
    (log/info "Sending keep-alive message."))

(defn daily-tasks [time]
  (log/info "Executing daily tasks.")
  (acceptance/remind-silent-users)
  (acceptance/kick-old-users)
  #_(daily-event-reminder))

(defn monthly-tasks [time]
  (log/info "Executing monthly tasks.")
  #_(monthly-event-reminder))

(defn schedule-tasks []
  #_(chime/chime-at
      (chime/periodic-seq
        (Instant/now)
        (Duration/ofMinutes 10))
      keep-alive-task)
  (chime/chime-at
    (->>
      (chime/periodic-seq
        standard-task-execution-time
        (java-time/period 1 :days))
      (filter #(java-time/after? % (java-time/instant))))
    daily-tasks)
  (chime/chime-at
    (->>
      (chime/periodic-seq
        standard-task-execution-time
        (java-time/period 1 :days))
      (partition-by #(java-time/month (java-time/local-date % "Europe/Amsterdam")))
      (map last)
      (filter #(java-time/after? % (java-time/instant))))
    monthly-tasks))