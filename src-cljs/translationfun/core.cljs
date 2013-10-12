(ns translationfun.core
  (:require [domina :refer [by-id by-class nodes append! value children text]]
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

(defn tcol-id [id]
  (str "tcol" id))

(defn add-result [x]
  (with-alert 
    (let [m (js->clj (JSON/parse (.-message x)) :keywordize-keys true)]
      (append! (by-id (tcol-id (:id m)))
               (crate/html [:p (:text m)])))))

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

(defn make-request []  
  (to-json {:id translation-id
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

(defn create-socket  []
  (with-alert
    (go
     (let [socket (goog.net.WebSocket.)
           handler (goog.events.EventHandler.)
           port (<! (get-port))]
       (.listen handler socket ws-event/MESSAGE add-result) ;; do this BEFORE
       ;; open
       (.listen handler socket ws-event/OPENED #(.send socket (make-request)))
       (.open socket (str "ws://localhost:" port "/ws"))
       socket))))

(defn do-translation []
  (set! translation-id (inc translation-id))
  (append! (by-id "contentBox")
           (crate/html [:div.column {:id (tcol-id translation-id)}]))
  (create-socket))

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
  (listen! (by-id "clickme") :click do-translation))
