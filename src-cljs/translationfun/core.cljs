(ns translationfun.core
  (:require [domina :refer [by-id by-class nodes append! value children text destroy-children!]]
            [domina.events :refer [listen!]]
            [goog.net.WebSocket :as socket]
            [goog.events.EventHandler :as event]
            [goog.net.WebSocket.EventType :as ws-event]
            [goog.net.XhrIo :as xhr]
            [crate.core :as crate]
            [cljs.core.async :as async :refer [<! >! chan close!]])
  (:require-macros
   [translationfun.macros :refer [with-alert]]
   [cljs.core.async.macros :refer [go]]))

(def translation-id 0)
(def translations {})
(def curr-start-idx 0)
(def translations-num 3)

(defn tcol-id [id]
  (str "tcol" (mod (- id curr-start-idx) translations-num)))

(defn shown? [id]
  (and (>= id curr-start-idx) (<= id (+ curr-start-idx translations-num))))

(defn append-to-col [col-id x]
  (append! (by-id (tcol-id col-id))
           (crate/html [:p x])))

(defn redraw-all []
  (with-alert
    (doseq [k (range curr-start-idx (+ curr-start-idx translations-num))]
      (destroy-children! (by-id (tcol-id k)))
      (doseq [x (get translations k)]
        (append-to-col k x)))))

(defn add-result [x]
  (with-alert
    (let [m (js->clj (JSON/parse (.-message x)) :keywordize-keys true)
          {id :id text :text} m
          result (str id " " text)]
      (set! translations (assoc translations
                         id
                         (conj (get translations id [])
                               result)))
      (if (shown? id) 
        (append-to-col id result)))))

(defn parenthesize [s]
  (if (or (keyword? s) (string? s))
    (str "\"" (name s) "\"")
    s))

(defn to-json [m]
  (str "{" (apply str 
                  (map (fn [[k v]] (str (parenthesize k) ": " (parenthesize v) " ")) m)) "}"))

(defn checked-languages []
  (apply vector (map #(.-name %)
                     (filter #(.-checked %)
                             (nodes (by-class "checker"))))))

(defn make-request [id]
  (to-json {:id id
            :text (value (by-id "text"))
            :languages (checked-languages)
            :start-lang (value (by-id "from"))}))

(defn get-port []
  (with-alert
    (let [ch (chan 1)]
         (xhr/send "send-port"
                   (fn [event]
                     (let [res (-> event .-target .getResponseText)]
                       (go (>! ch res)
                           (close! ch)))))
         ch)))

(defn create-socket  [id]
  (with-alert
    (go
     (let [socket (goog.net.WebSocket.)
           handler (goog.events.EventHandler.)
           port (<! (get-port))]
       (.listen handler socket ws-event/MESSAGE add-result) ;; do this BEFORE
       ;; open
       (.listen handler socket ws-event/OPENED #(.send socket (make-request id)))
       (.open socket (str "ws://localhost:" port "/ws"))
       socket))))

(defn change-page [f]
  (let [new-idx (f curr-start-idx)]
    (when (and (>= new-idx 0) (<= new-idx (dec translation-id)))
      (set! curr-start-idx new-idx)
      (redraw-all))))

(defn do-translation []
  (with-alert
    (let [new-translation-id (inc translation-id)]
      (if (not (shown? new-translation-id))
        (change-page inc))    
      (create-socket translation-id)
      (set! translation-id new-translation-id))))

(defn start-language? [s]
  (= s (value (by-id "from"))))

(defn make-checkbox [s]
  [:td
   [:input.checker {:type "checkbox" :id s :name s
                    :checked (not (start-language? s))}]
   [:label {:for s}] s])

(defn draw-languages []
  (set! (.-value (by-id "from")) "English")
  (doseq [x (partition 5 5 nil (sort
                                (map text (children (by-id "from")))))]
    (append! (by-id "thru")
             (crate/html [:tr (map make-checkbox x)]))))

(defn ^:export init []
  (draw-languages)
  (listen! (by-id "clickme") :click do-translation)
  (listen! (by-id "next") :click (partial change-page inc))
  (listen! (by-id "back") :click (partial change-page dec)))
