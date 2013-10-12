(ns translationfun.routes.home
  (:use translationfun.core
        org.httpkit.server)
  (:require [selmer.parser :refer [render-file]]
            [clojure.data.json :as json]))

(def languages ["French" "German" "Polish" "Danish"
                "Romanian" "Hebrew" "Russian" "Spanish"
                "Czech" "Japanese" "Arabic" "Italian"
                "Eglish" "Finnish" "Thai"])

(defn send-port [request] (str (:server-port request)))

(defn handle-request [channel data]
  (let [{id :id text :text
         start-lang :start-lang
         languages :languages} (json/read-str data :key-fn keyword)]
    (doseq [[lang tr] (translate text start-lang languages)]      
      (send! channel (json/write-str {:text (str lang ":" tr) :id id})))))

(defn ws-handler [request]
  (with-channel request channel
    (on-close channel (fn [status] (println "Channel closed: " status)))
    (on-receive channel (fn [data]
                          (future (handle-request channel data))))))

(defn index [request]
  (render-file "translationfun/views/templates/index.html"
               {:languages languages}))
