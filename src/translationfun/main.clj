(ns translationfun.main
  (:use translationfun.routes.home
        org.httpkit.server)
  (:require [compojure.core :refer :all]
            [compojure.route :refer :all])
  (:gen-class))

(defroutes all-routes
  (GET "/" [] index)
  (GET "/ws" [] ws-handler)
  (GET "/send-port" [] send-port) 
  (files "" {:root "resources/public"})) ;; only when using httpkit,
;; jetty doesn't require it

(defn -main [port]
  (println "transfun server up...")
  (run-server #'all-routes {:port (Integer/parseInt port)}))
