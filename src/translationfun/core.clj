(ns translationfun.core
  (:require [clojure.data.json :as json])
  (:import (com.memetix.mst.translate Translate)
           (com.memetix.mst.language Language)))

(def languages-bing [Language/FRENCH Language/GERMAN Language/POLISH Language/DANISH Language/ROMANIAN Language/HEBREW Language/RUSSIAN Language/SPANISH Language/CZECH Language/JAPANESE Language/ARABIC Language/ITALIAN Language/ENGLISH Language/FINNISH Language/THAI])

(defn languages []
  (map #(clojure.string/capitalize (.name %)) languages-bing))

(defn add-start-lang [languages start-lang]
  (cons start-lang (conj languages start-lang)))

(defn set-up-credentials []
    (let [{client-id :client-id client-secret :client-secret}
        (json/read-str (slurp "props.json") :key-fn keyword)]
      (Translate/setClientId client-id)
      (Translate/setClientSecret client-secret)))

(defn language-number [language]
  (first (filter #(= (.toUpperCase language) (.name %)) languages-bing)))

(defn shuffle-languages [languages start-lang]
  (let [shuffled (shuffle (filter #(not= % start-lang) languages))]
    (add-start-lang shuffled start-lang)))

(defn translate-all [s languages]
  (set-up-credentials)
  (reductions (fn [x [c n]] (Translate/execute x c n)) s
              (partition 2 1 (map language-number languages))))

(defn translate [s start-lang languages]
  (if-not (nil? s)
    (let [shuffled (shuffle-languages languages start-lang)]
      (map vector shuffled (translate-all s shuffled)))))
